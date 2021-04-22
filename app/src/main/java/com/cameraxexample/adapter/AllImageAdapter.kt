package com.cameraxexample.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.cameraxexample.R
import com.cameraxexample.database.table.ImagePathTable
import com.cameraxexample.databinding.ImageListBinding

class AllImageAdapter(private val context: Context) :
    ListAdapter<ImagePathTable, AllImageAdapter.MyViewHolder>(
        ImageComparator()
    ) {
    private var listener: OnItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.image_list, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        //holder.binding.ivImage.setImageURI(Uri.parse((imageList[position]).path))
        val model = getItem(position)
        Glide.with(context)
            .load(model.image_path)
            .into(holder.binding.ivImage)
        holder.binding.ivImage.setOnClickListener {
            listener?.onItemClick(position,it)
        }
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

    class ImageComparator : DiffUtil.ItemCallback<ImagePathTable>() {
        override fun areItemsTheSame(
            oldItem: ImagePathTable,
            newItem: ImagePathTable
        ): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(
            oldItem: ImagePathTable,
            newItem: ImagePathTable
        ): Boolean {
            return oldItem.id == newItem.id
        }

    }
}