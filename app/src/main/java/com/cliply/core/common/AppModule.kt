package com.cliply.core.common

import android.content.Context
import androidx.room.Room
import com.cliply.core.database.*
import com.cliply.core.storage.SettingsRepository
import com.cliply.domain.repository.DownloadRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides @Singleton fun database(@ApplicationContext context: Context): CliplyDatabase = Room.databaseBuilder(context, CliplyDatabase::class.java, "cliply.db").build()
    @Provides fun dao(db: CliplyDatabase): DownloadJobDao = db.downloadJobDao()
    @Provides @Singleton fun repository(dao: DownloadJobDao): DownloadRepository = DownloadRepositoryImpl(dao)
    @Provides @Singleton fun settings(@ApplicationContext context: Context) = SettingsRepository(context)
}
