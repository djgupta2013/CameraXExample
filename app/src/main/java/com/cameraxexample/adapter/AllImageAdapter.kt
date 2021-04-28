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
import java.util.ArrayList

class AllImageAdapter(private val context: Context) :
    ListAdapter<ImagePathTable, AllImageAdapter.MyViewHolder>(
        ImageComparator()
    ) {
    private var listener: OnItemClickListener? = null
    private var imageCounter = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.image_list, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val model = getItem(position)
        holder.binding.apply {
            Glide.with(context)
                .load(model.image_path)
                .into(ivImage)
           ivImage.setOnClickListener {
                listener?.onItemClick(position, it, model)
            }
            ivImage.setOnLongClickListener {
                frameLayout.visibility = View.VISIBLE
                model.isSelected = !model.isSelected
                checkbox.isChecked = model.isSelected
                if(model.isSelected){
                    imageCounter++
                }else{
                    if(imageCounter != 0){
                        imageCounter--
                    }
                }
                notifyDataSetChanged()
                true
            }
            checkbox.setOnClickListener {
                model.isSelected = !model.isSelected
                checkbox.isChecked = model.isSelected
                if(model.isSelected){
                    imageCounter++
                }else{
                    if(imageCounter != 0){
                        imageCounter--
                    }
                }
                notifyDataSetChanged()
            }
            if(imageCounter == 0){
                listener?.imageSelected(false)
                frameLayout.visibility = View.GONE
            }else{
                listener?.imageSelected(true)
                frameLayout.visibility = View.VISIBLE
            }
        }
    }

    inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var binding: ImageListBinding = DataBindingUtil.bind(view)!!
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }

    interface OnItemClickListener {
        fun onItemClick(position: Int, view: View,model: ImagePathTable)
        fun imageSelected(selected: Boolean)
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