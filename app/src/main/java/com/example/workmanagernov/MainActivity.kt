package com.example.workmanagernov

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.workmanagernov.WorkManager.UploadWorker
import com.example.workmanagernov.ui.theme.WorkManagerNovTheme
import com.example.workmanagernov.util.Constants

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WorkManagerNovTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    ShowScreen()
                }
            }
        }
    }
}

@Composable
fun ShowScreen() {
    val context = LocalContext.current
    var selectImage by remember { mutableStateOf<Uri?>(null) }
    val launcher = rememberLauncherForActivityResult(contract =
    ActivityResultContracts.GetContent()) { uri: Uri? ->
        selectImage = uri
    }
    Scaffold(
        content = {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                DisplayImage(selectImage, context)
                Row() {
                    Button(
                        onClick = {
                            launcher.launch("image/*")
                        },
                    ) {
                        Text(text = "Select A Photo")
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    selectImage?.let {
                        Button(
                            onClick = {
                                val imageData = workDataOf(Constants.KEY_IMAGE_URI to selectImage.toString())
                                val uploadWorkRequest = OneTimeWorkRequestBuilder<UploadWorker>()
                                    .setInputData(imageData)
                                    .build()
                                WorkManager.getInstance(context).enqueue(uploadWorkRequest)
                            },
                        ) {
                            Text(text = "Upload A Photo")
                        }
                    }
                }
            }
        },
    )
}

@Composable
fun DisplayImage(
    selectImages: Uri?,
    context: Context
) {
    val bitmap =  remember {
        mutableStateOf<Bitmap?>(null)
    }
    selectImages?.let {
        if (Build.VERSION.SDK_INT < 28) {
            bitmap.value = MediaStore.Images
                .Media.getBitmap(context.contentResolver,it)

        } else {
            val source = ImageDecoder
                .createSource(context.contentResolver,it)
            bitmap.value = ImageDecoder.decodeBitmap(source)
        }

        bitmap.value?.let {  btm ->
            Image(bitmap = btm.asImageBitmap(),
                contentDescription =null,
                modifier = Modifier.size(400.dp))
        }
    }
}
