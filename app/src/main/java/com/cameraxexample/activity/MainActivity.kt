package com.cameraxexample.activity

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.GridLayoutManager
import com.cameraxexample.R
import com.cameraxexample.UserApplication
import com.cameraxexample.adapter.AllImageAdapter
import com.cameraxexample.database.db.MyRoomDatabase
import com.cameraxexample.database.table.ImagePathTable
import com.cameraxexample.databinding.ActivityMainBinding
import com.cameraxexample.liveData.viewModel.ImageViewModel
import com.cameraxexample.liveData.viewModelFactory.ImageViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var outputDirectory: File
    private var adapter: AllImageAdapter? = null
    private var imageList = ArrayList<ImagePathTable>()
    private var imageSelected = false
    private val imageViewModel: ImageViewModel by viewModels {
        ImageViewModelFactory((application as UserApplication).imageRepository)
    }

    /*private val applicationScope = CoroutineScope(SupervisorJob())
    private val database by lazy { MyRoomDatabase.getDatabase(this, applicationScope) }*/
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            binding.rvAllImages.layoutManager = GridLayoutManager(
                this, 7,
                GridLayoutManager.VERTICAL, false
            )
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            binding.rvAllImages.layoutManager = GridLayoutManager(
                this, 4,
                GridLayoutManager.VERTICAL, false
            )
        }

        //val list = database.imageDao().getAllImage()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        setRecyclerView()
        outputDirectory = getOutputDirectory()
        val files: Array<File> = outputDirectory.listFiles()!!
        imageViewModel.getAllImage.observe(this) { images ->
            images.apply {
                adapter?.submitList(images)

                adapter?.currentList?.let { imageList.addAll(it) }
                for (image in imageList) {
                    Log.e("image", image.id.toString())
                }
                //adapter?.onCurrentListChanged()
            }
        }

        adapter?.notifyDataSetChanged()
        binding.fab.setOnClickListener {
            if (!imageSelected) {
                startActivity(Intent(this, CameraClickActivity::class.java))
            }else{
                showImageDeleteAlert()
            }
        }
    }

    private fun showImageDeleteAlert() {
        val builder = AlertDialog.Builder(this)

        val view: View = LayoutInflater.from(this)
            .inflate(R.layout.image_delete_alert, null)

        builder.setView(view)
        val dialog = builder.show()
        //dialog.setCancelable(false)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    private fun setRecyclerView() {
        adapter = AllImageAdapter(this)
        binding.rvAllImages.layoutManager = GridLayoutManager(
            this, 4,
            GridLayoutManager.VERTICAL, false
        )
        binding.rvAllImages.adapter = adapter

        adapter?.setOnItemClickListener(object : AllImageAdapter.OnItemClickListener {
            override fun onItemClick(position: Int, view: View, model: ImagePathTable) {

                val intent = Intent(Intent.ACTION_VIEW)
                    .setDataAndType(
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            FileProvider.getUriForFile(
                                this@MainActivity,
                                this@MainActivity.packageName.toString() + ".provider",
                                File(model.image_path)
                            )
                        } else {
                            Uri.fromFile(File(model.image_path))
                        }, "image/*"
                    ).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                try {
                    startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    Toast.makeText(
                        this@MainActivity,
                        "No Application available to view media",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            @SuppressLint("UseCompatLoadingForDrawables")
            override fun imageSelected(selected: Boolean) {
                imageSelected = selected
                if (imageSelected) {
                    binding.fab.setImageDrawable(getDrawable(R.drawable.ic_delete_24))
                } else {
                    binding.fab.setImageDrawable(getDrawable(R.drawable.ic_photo_camera_24))
                }
            }
        })
    }

    private fun getOutputDirectory(): File {
        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists())
            mediaDir else filesDir
    }
}