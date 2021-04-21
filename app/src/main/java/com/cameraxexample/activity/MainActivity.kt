package com.cameraxexample.activity

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.GridLayoutManager
import com.cameraxexample.R
import com.cameraxexample.adapter.AllImageAdapter
import com.cameraxexample.databinding.ActivityMainBinding
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var outputDirectory: File
    private var adapter: AllImageAdapter? = null
    private var imageList = ArrayList<File>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        setRecyclerView()
        outputDirectory = getOutputDirectory()
        val files: Array<File> = outputDirectory.listFiles()!!
        imageList.addAll(files)
        imageList.reverse()
        Log.e("files", files.toString())
        for(i in files.indices){
            //imageList.add(files[i])
        }
        adapter?.notifyDataSetChanged()
        binding.fab.setOnClickListener{
            startActivity(Intent(this, CameraClickActivity::class.java))
        }

    }

    private fun setRecyclerView() {
        adapter = AllImageAdapter(this,imageList)
        binding.rvAllImages.layoutManager = GridLayoutManager(
            this, 4,
            GridLayoutManager.VERTICAL, false
        )
        binding.rvAllImages.adapter = adapter

        adapter?.setOnItemClickListener(object: AllImageAdapter.OnItemClickListener{
            override fun onItemClick(position: Int, view: View) {
                val intent = Intent(Intent.ACTION_VIEW)
                    .setDataAndType(
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            FileProvider.getUriForFile(this@MainActivity, this@MainActivity.packageName.toString() + ".provider", imageList[position])
                        }else {
                            Uri.fromFile(imageList[position])
                        }, "image/*"
                    ).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                try {
                    startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    Toast.makeText(this@MainActivity, "No Application available to view media", Toast.LENGTH_SHORT
                    ).show()
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