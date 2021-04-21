package com.cameraxexample.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.cameraxexample.database.table.ImagePathTable
import kotlinx.coroutines.flow.Flow

@Dao
interface ImageDao {
    @Query("SELECT * FROM image_path_table ORDER BY id DESC")
    fun getAllImage(): Flow<List<ImagePathTable>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertImage(transactionTable: ImagePathTable)

    @Query("DELETE from image_path_table where id =:id")
    fun deleteImage(id: Int)
}