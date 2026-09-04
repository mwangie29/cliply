package com.cliply.core.common

import android.content.Context
import androidx.room.Room
import com.cliply.core.database.*
import com.cliply.core.storage.SettingsRepository
import com.cliply.domain.repository.DownloadRepository
import com.cliply.domain.usecase.CreateDownloadJobUseCase
import com.cliply.download.engine.*
import com.cliply.download.notification.CliplyNotificationManager
import com.cliply.download.scheduler.TransferExecutor
import com.cliply.download.scheduler.TransferExecutorFactory
import com.cliply.download.storage.MediaStorePublisher
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides @Singleton fun database(@ApplicationContext context: Context): CliplyDatabase = Room.databaseBuilder(context, CliplyDatabase::class.java, "cliply.db").fallbackToDestructiveMigration().build()
    @Provides fun dao(db: CliplyDatabase): DownloadJobDao = db.downloadJobDao()
    @Provides @Singleton fun repository(dao: DownloadJobDao): DownloadRepository = DownloadRepositoryImpl(dao)
    @Provides @Singleton fun settings(@ApplicationContext context: Context) = SettingsRepository(context)
    @Provides @Singleton fun httpClient() = OkHttpClient()
    @Provides @Singleton fun transfer(client: OkHttpClient): DownloadTransferManager = OkHttpDownloadTransferManager(client)
    @Provides @Singleton fun testProvider(): TestMediaProvider = ControlledTestMediaProvider()
    @Provides @Singleton fun notifications(@ApplicationContext context: Context) = CliplyNotificationManager(context)
    @Provides @Singleton fun publisher(@ApplicationContext context: Context) = MediaStorePublisher(context.contentResolver)
    @Provides @Singleton fun executor(@ApplicationContext context: Context): TransferExecutor = TransferExecutorFactory(context).create()
    @Provides fun createDownloadJob(repository: DownloadRepository) = CreateDownloadJobUseCase(repository)
}
