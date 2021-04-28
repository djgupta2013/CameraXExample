package com.cameraxexample.database.table

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "image_path_table")
class ImagePathTable(
    @ColumnInfo(name = "image_path") val image_path: String,
    @ColumnInfo(name = "date") val date: String,
    @ColumnInfo(name = "isSelected") var isSelected: Boolean
) {
    @PrimaryKey(autoGenerate = true)
    var id = 0
}