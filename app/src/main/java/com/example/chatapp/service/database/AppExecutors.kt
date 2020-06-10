package com.example.chatapp.service.database

import android.os.Handler
import android.os.Looper
import androidx.annotation.NonNull
import java.util.concurrent.Executor
import java.util.concurrent.Executors

/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */



/**
 * Global executor pools for the whole application.
 *
 *
 * Grouping tasks like this avoids the effects of task starvation (e.g. disk reads don't wait behind
 * webservice requests).
 */
class AppExecutors private constructor(
    private val diskIO: Executor,
    private val networkIO: Executor,
    private val mainThread: Executor
) {
    fun diskIO(): Executor {
        return diskIO
    }

    fun mainThread(): Executor {
        return mainThread
    }

    fun networkIO(): Executor {
        return networkIO
    }

    private class MainThreadExecutor : Executor {
        private val mainThreadHandler =
            Handler(Looper.getMainLooper())

        override fun execute(@NonNull command: Runnable) {
            mainThreadHandler.post(command)
        }
    }

    companion object {
        // For Singleton instantiation
        private val LOCK = Any()
        private var sInstance: AppExecutors? = null
        val instance: AppExecutors?
            get() {
                if (sInstance == null) {
                    synchronized(LOCK) {
                        sInstance = AppExecutors(
                            Executors.newSingleThreadExecutor(),
                            Executors.newFixedThreadPool(3),
                            MainThreadExecutor()
                        )
                    }
                }
                return sInstance
            }
    }

}