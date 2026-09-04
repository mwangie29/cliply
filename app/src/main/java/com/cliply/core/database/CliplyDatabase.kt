package com.cliply.core.database

import androidx.room.*
import com.cliply.domain.model.*
import com.cliply.domain.repository.DownloadRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Entity(tableName = "download_jobs")
data class DownloadJobEntity(
    @PrimaryKey val id: String,
    val sourceUrl: String,
    val platform: String,
    val title: String?,
    val thumbnailUrl: String?,
    val status: String,
    val mimeType: String?,
    val totalBytes: Long,
    val downloadedBytes: Long,
    val speedBytesPerSecond: Long,
    val progressPercent: Int,
    val temporaryFilePath: String?,
    val mediaStoreUri: String?,
    val createdAtEpochMs: Long,
    val startedAtEpochMs: Long?,
    val completedAtEpochMs: Long?,
    val errorMessage: String?
)

@Dao
interface DownloadJobDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(job: DownloadJobEntity)
    @Query("SELECT * FROM download_jobs WHERE id = :id") suspend fun get(id: String): DownloadJobEntity?
    @Query("SELECT * FROM download_jobs WHERE id = :id") fun observe(id: String): Flow<DownloadJobEntity?>
    @Query("SELECT * FROM download_jobs ORDER BY createdAtEpochMs DESC") fun history(): Flow<List<DownloadJobEntity>>
    @Update suspend fun update(job: DownloadJobEntity)
    @Query("DELETE FROM download_jobs WHERE id = :id") suspend fun delete(id: String)
}

@Database(entities = [DownloadJobEntity::class], version = 2, exportSchema = false)
abstract class CliplyDatabase : RoomDatabase() { abstract fun downloadJobDao(): DownloadJobDao }

class DownloadRepositoryImpl(private val dao: DownloadJobDao) : DownloadRepository {
    private fun DownloadJob.toEntity() = DownloadJobEntity(id, sourceUrl, platform.name, title, thumbnailUrl, status.name, mimeType, totalBytes, downloadedBytes, speedBytesPerSecond, progressPercent, temporaryFilePath, mediaStoreUri, createdAt.toEpochMilli(), startedAt?.toEpochMilli(), completedAt?.toEpochMilli(), errorMessage)
    private fun DownloadJobEntity.toDomain() = DownloadJob(id, sourceUrl, runCatching { Platform.valueOf(platform) }.getOrDefault(Platform.UNKNOWN), title, thumbnailUrl, runCatching { DownloadStatus.valueOf(status) }.getOrDefault(DownloadStatus.CREATED), mimeType = mimeType, totalBytes = totalBytes, downloadedBytes = downloadedBytes, speedBytesPerSecond = speedBytesPerSecond, progressPercent = progressPercent, temporaryFilePath = temporaryFilePath, mediaStoreUri = mediaStoreUri, createdAt = java.time.Instant.ofEpochMilli(createdAtEpochMs), startedAt = startedAtEpochMs?.let(java.time.Instant::ofEpochMilli), completedAt = completedAtEpochMs?.let(java.time.Instant::ofEpochMilli), errorMessage = errorMessage)
    override suspend fun insert(job: DownloadJob) = dao.insert(job.toEntity())
    override suspend fun get(id: String) = dao.get(id)?.toDomain()
    override fun observe(id: String) = dao.observe(id).map { it?.toDomain() }
    override fun history() = dao.history().map { list -> list.map { it.toDomain() } }
    override suspend fun update(job: DownloadJob) = dao.update(job.toEntity())
    override suspend fun delete(id: String) = dao.delete(id)
}
