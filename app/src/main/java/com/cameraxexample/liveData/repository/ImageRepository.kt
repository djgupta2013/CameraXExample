package com.cameraxexample.liveData.repository

import com.cameraxexample.database.dao.ImageDao
import com.cameraxexample.database.table.ImagePathTable
import kotlinx.coroutines.flow.Flow

class ImageRepository(private val imageDao: ImageDao) {

    val getAllImage: Flow<List<ImagePathTable>> = imageDao.getAllImage()

    suspend fun insertImagePath(imagePathTable: ImagePathTable) {
        imageDao.insertImage(imagePathTable)
    }

    suspend fun deleteSingleImage(id: Int) {
        imageDao.deleteImage(id)
    }
}