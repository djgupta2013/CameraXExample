package com.cameraxexample.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.cameraxexample.R
import com.cameraxexample.databinding.ImageListBinding
import java.io.File

class AllImageAdapter(private val context: Context, private val imageList: ArrayList<File>) :
    RecyclerView.Adapter<AllImageAdapter.MyViewHolder>() {
    private var listener: OnItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.image_list, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        //holder.binding.ivImage.setImageURI(Uri.parse((imageList[position]).path))
        Glide.with(context)
            .load(imageList[position])
            .into(holder.binding.ivImage)
        holder.binding.ivImage.setOnClickListener {
            listener?.onItemClick(position,it)
        }
    }

    override fun getItemCount(): Int {
        return imageList.size
    }

    inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var binding: ImageListBinding = DataBindingUtil.bind(view)!!
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }

    interface OnItemClickListener {
        fun onItemClick(position: Int, view: View)
    }
}