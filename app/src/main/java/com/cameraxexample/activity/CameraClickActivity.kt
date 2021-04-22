package com.cameraxexample.activity

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.cameraxexample.R
import com.cameraxexample.UserApplication
import com.cameraxexample.database.table.ImagePathTable
import com.cameraxexample.databinding.ActivityCameraClickBinding
import com.cameraxexample.liveData.viewModel.ImageViewModel
import com.cameraxexample.liveData.viewModelFactory.ImageViewModelFactory
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraClickActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCameraClickBinding
    private var imageCapture: ImageCapture? = null
    private lateinit var outputDirectory: File
    private lateinit var cameraExecutor: ExecutorService
    var isCameraRotate = true
    private val imageViewModel: ImageViewModel by viewModels {
        ImageViewModelFactory((application as UserApplication).imageRepository)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        );
        binding = DataBindingUtil.setContentView(this, R.layout.activity_camera_click)
        // Request camera permissions
        // Hide status bar
        //window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }

        // Set up the listener for take photo button
        binding.cameraCaptureButton.setOnClickListener { takePhoto() }

        binding.cameraRotate.setOnClickListener {
            isCameraRotate = !isCameraRotate
            startCamera()
            if (!isCameraRotate) {
                binding.cameraRotate.animate().rotation(90f).setDuration(300).start()
            } else {
                binding.cameraRotate.animate().rotation(0f).setDuration(300).start()
            }
        }

        binding.btnCancel.setOnClickListener {
            binding.frameLayout.visibility = View.GONE
        }

        binding.btnSave.setOnClickListener {
            binding.frameLayout.visibility = View.GONE
        }

        outputDirectory = getOutputDirectory()

        cameraExecutor = Executors.newSingleThreadExecutor()

        val files: Array<File> = outputDirectory.listFiles()!!

        Log.e("files", files.toString())

        for (i in files.indices) {
           // Log.e("image path", files[i].path)
        }
    }


    private fun takePhoto() {
        // Get a stable reference of the modifiable image capture use case
        val imageCapture = imageCapture ?: return

        // Create time-stamped output file to hold the image
        val photoFile = File(
            outputDirectory,
            SimpleDateFormat(
                FILENAME_FORMAT, Locale.US
            ).format(System.currentTimeMillis()) + ".jpg"
        )

        // Create output options object which contains file + metadata
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        // Set up image capture listener, which is triggered after photo has
        // been taken
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(photoFile)
                    val msg = "Photo capture succeeded: $savedUri"

                    Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                    Log.e(TAG, msg)
                    //setImage(photoFile)
                    showImagePreview(photoFile)
                }
            })
    }

    private fun showImagePreview(photoFile: File) {
        val builder = AlertDialog.Builder(this)

        val view: View = LayoutInflater.from(this)
            .inflate(R.layout.image_preview, null)

        builder.setView(view)
        val btnCancel = view.findViewById<Button>(R.id.btnCancel)
        val btnSave = view.findViewById<Button>(R.id.btnSave)
        val ivImage = view.findViewById<ImageView>(R.id.ivImage)
        val dialog = builder.show()
        dialog.setCancelable(false)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        Glide.with(this).load(photoFile.absoluteFile)
            .into(ivImage)
        btnCancel.setOnClickListener {
            if (photoFile.delete())
                Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }
        btnSave.setOnClickListener {
            val c: Date = Calendar.getInstance().time
            val df = SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault())
            val formattedDate= df.format(c)
            imageViewModel.insertImagePath(ImagePathTable(photoFile.absoluteFile.toString(),formattedDate))
            dialog.dismiss()
        }
    }

    private fun setImage(photoFile: File) {

        binding.frameLayout.visibility = View.VISIBLE
        val image = BitmapFactory.decodeFile(photoFile.name)
        Glide.with(this).load(photoFile.absoluteFile)
            .into(binding.ivImage)

    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener(Runnable {
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
                }

            // Select back camera as a default
            val cameraSelector = if (isCameraRotate) {
                CameraSelector.DEFAULT_BACK_CAMERA
            } else {
                CameraSelector.DEFAULT_FRONT_CAMERA
            }
            imageCapture = ImageCapture.Builder()
                .setTargetResolution(Size(600, 800))
                .build()

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture
                )


            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun getOutputDirectory(): File {
        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists())
            mediaDir else filesDir
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray
    ) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(
                    this,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    companion object {
        private const val TAG = "CameraXBasic"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }
}