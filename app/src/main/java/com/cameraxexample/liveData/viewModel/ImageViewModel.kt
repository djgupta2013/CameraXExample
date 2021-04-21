package com.cameraxexample.liveData.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.cameraxexample.database.table.ImagePathTable
import com.cameraxexample.liveData.repository.ImageRepository
import kotlinx.coroutines.launch

class ImageViewModel(private val imageRepository: ImageRepository): ViewModel() {

    val getAllImage: LiveData<List<ImagePathTable>> = imageRepository.getAllImage.asLiveData()

    fun insertImagePath(imagePathTable: ImagePathTable) = viewModelScope.launch{
        imageRepository.insertImagePath(imagePathTable)
    }

    fun deleteSingleImage(id: Int) = viewModelScope.launch {
        imageRepository.deleteSingleImage(id)
    }
}