/*
 * Copyright (C) 2019 The Android Open Source Project
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

package com.example.android.architecture.blueprints.todoapp.di

import androidx.room.Room
import com.example.android.architecture.blueprints.todoapp.addedittask.AddEditTaskViewModel
import com.example.android.architecture.blueprints.todoapp.data.source.DefaultTasksRepository
import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository
import com.example.android.architecture.blueprints.todoapp.data.source.local.TasksLocalDataSource
import com.example.android.architecture.blueprints.todoapp.data.source.local.ToDoDatabase
import com.example.android.architecture.blueprints.todoapp.data.source.remote.TasksRemoteDataSource
import com.example.android.architecture.blueprints.todoapp.statistics.StatisticsViewModel
import com.example.android.architecture.blueprints.todoapp.taskdetail.TaskDetailViewModel
import com.example.android.architecture.blueprints.todoapp.tasks.TasksViewModel
import kotlinx.coroutines.Dispatchers
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.experimental.dsl.viewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.experimental.builder.single

/**
 * Module to tell Hilt how to provide instances of types that cannot be constructor-injected.
 *
 * As these types are scoped to the application lifecycle using @Singleton, they're installed
 * in Hilt's ApplicationComponent.
 */
// @Module
// @InstallIn(ApplicationComponent::class)
// object AppModule {
//
//     @Qualifier
//     @Retention(RUNTIME)
//     annotation class RemoteTasksDataSource
//
//     @Qualifier
//     @Retention(RUNTIME)
//     annotation class LocalTasksDataSource
//
//     @Singleton
//     @RemoteTasksDataSource
//     @Provides
//     fun provideTasksRemoteDataSource(): TasksDataSource {
//         return TasksRemoteDataSource
//     }
//
//     @Singleton
//     @LocalTasksDataSource
//     @Provides
//     fun provideTasksLocalDataSource(
//         database: ToDoDatabase,
//         ioDispatcher: CoroutineDispatcher
//     ): TasksDataSource {
//         return TasksLocalDataSource(
//             database.taskDao(), ioDispatcher
//         )
//     }
//
//     @Singleton
//     @Provides
//     fun provideDataBase(@ApplicationContext context: Context): ToDoDatabase {
//         return Room.databaseBuilder(
//             context.applicationContext,
//             ToDoDatabase::class.java,
//             "Tasks.db"
//         ).build()
//     }
//
//     @Singleton
//     @Provides
//     fun provideIoDispatcher() = Dispatchers.IO
// }

val appModule = module {
    viewModel<AddEditTaskViewModel>()
    viewModel<StatisticsViewModel>()
    viewModel<TaskDetailViewModel>()
    viewModel { params -> TasksViewModel(get(), params.get()) } //Better in 3.x

    // TasksRemoteDataSource
    single<TasksDataSource>(named("TasksRemoteDataSource")) { TasksRemoteDataSource }

    // LocalTasksDataSource
    single<TasksLocalDataSource>(named("LocalTasksDataSource")) bind TasksDataSource::class

    single {
        Room.databaseBuilder(
            androidContext().applicationContext,
            ToDoDatabase::class.java,
            "Tasks.db"
        ).build()
    }
    single { get<ToDoDatabase>().taskDao() }

    single { Dispatchers.IO }
}

/**
 * The binding for TasksRepository is on its own module so that we can replace it easily in tests.
 */
val tasksRepositoryModule = module {
    single<TasksRepository> {
        DefaultTasksRepository(
            get(named("TasksRemoteDataSource")),
            get(named("LocalTasksDataSource")),
            get()
        )
    }
}

val allModules = appModule + tasksRepositoryModule

// @Module
// @InstallIn(ApplicationComponent::class)
// object TasksRepositoryModule {
//
//     @Singleton
//     @Provides
//     fun provideTasksRepository(
//         @AppModule.RemoteTasksDataSource remoteTasksDataSource: TasksDataSource,
//         @AppModule.LocalTasksDataSource localTasksDataSource: TasksDataSource,
//         ioDispatcher: CoroutineDispatcher
//     ): TasksRepository {
//         return DefaultTasksRepository(
//             remoteTasksDataSource, localTasksDataSource, ioDispatcher
//         )
//     }
// }
