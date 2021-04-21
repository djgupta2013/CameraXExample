package com.cameraxexample

import android.app.Application
import com.cameraxexample.database.db.MyRoomDatabase
import com.cameraxexample.liveData.repository.ImageRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class UserApplication : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob())
    private val database by lazy { MyRoomDatabase.getDatabase(this, applicationScope) }
    val imageRepository by lazy {
        ImageRepository(database.imageDao())
    }
}