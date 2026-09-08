package com.cliply.core.database

import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.cliply.domain.model.*
import com.cliply.domain.repository.DownloadRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Entity(tableName = "download_jobs")
data class DownloadJobEntity(
    @PrimaryKey val id: String,
    val sourceUrl: String,
    val resolutionSource: String,
    val providerResolutionId: String?,
    val providerAccountReference: String?,
    val resolutionExpiresAtEpochMs: Long?,
    val platform: String,
    val title: String?,
    val thumbnailUrl: String?,
    val status: String,
    val mimeType: String?,
    val width: Int?,
    val height: Int?,
    val durationMs: Long?,
    val totalBytes: Long,
    val downloadedBytes: Long,
    val speedBytesPerSecond: Long,
    val progressPercent: Int,
    val temporaryFilePath: String?,
    val mediaStoreUri: String?,
    val createdAtEpochMs: Long,
    val startedAtEpochMs: Long?,
    val completedAtEpochMs: Long?,
    val errorCode: String?,
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

@Database(entities = [DownloadJobEntity::class], version = 4, exportSchema = false)
abstract class CliplyDatabase : RoomDatabase() { abstract fun downloadJobDao(): DownloadJobDao }

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE download_jobs ADD COLUMN mimeType TEXT")
        db.execSQL("ALTER TABLE download_jobs ADD COLUMN speedBytesPerSecond INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE download_jobs ADD COLUMN temporaryFilePath TEXT")
        db.execSQL("ALTER TABLE download_jobs ADD COLUMN mediaStoreUri TEXT")
        db.execSQL("ALTER TABLE download_jobs ADD COLUMN startedAtEpochMs INTEGER")
        db.execSQL("ALTER TABLE download_jobs ADD COLUMN completedAtEpochMs INTEGER")
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE download_jobs ADD COLUMN width INTEGER")
        db.execSQL("ALTER TABLE download_jobs ADD COLUMN height INTEGER")
        db.execSQL("ALTER TABLE download_jobs ADD COLUMN durationMs INTEGER")
        db.execSQL("ALTER TABLE download_jobs ADD COLUMN errorCode TEXT")
    }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE download_jobs ADD COLUMN resolutionSource TEXT NOT NULL DEFAULT 'DIRECT_MEDIA'")
        db.execSQL("ALTER TABLE download_jobs ADD COLUMN providerResolutionId TEXT")
        db.execSQL("ALTER TABLE download_jobs ADD COLUMN providerAccountReference TEXT")
        db.execSQL("ALTER TABLE download_jobs ADD COLUMN resolutionExpiresAtEpochMs INTEGER")
    }
}

class DownloadRepositoryImpl(private val dao: DownloadJobDao) : DownloadRepository {
    private fun DownloadJob.toEntity() = DownloadJobEntity(id, sourceUrl, resolutionSource.name, providerResolutionId, providerAccountReference, resolutionExpiresAt?.toEpochMilli(), platform.name, title, thumbnailUrl, status.name, mimeType, width, height, durationMs, totalBytes, downloadedBytes, speedBytesPerSecond, progressPercent, temporaryFilePath, mediaStoreUri, createdAt.toEpochMilli(), startedAt?.toEpochMilli(), completedAt?.toEpochMilli(), errorCode, errorMessage)
    private fun DownloadJobEntity.toDomain() = DownloadJob(id, sourceUrl, runCatching { ResolutionSource.valueOf(resolutionSource) }.getOrDefault(ResolutionSource.DIRECT_MEDIA), providerResolutionId, providerAccountReference, resolutionExpiresAtEpochMs?.let(java.time.Instant::ofEpochMilli), runCatching { Platform.valueOf(platform) }.getOrDefault(Platform.UNKNOWN), title, thumbnailUrl, runCatching { DownloadStatus.valueOf(status) }.getOrDefault(DownloadStatus.CREATED), mimeType = mimeType, width = width, height = height, durationMs = durationMs, totalBytes = totalBytes, downloadedBytes = downloadedBytes, speedBytesPerSecond = speedBytesPerSecond, progressPercent = progressPercent, temporaryFilePath = temporaryFilePath, mediaStoreUri = mediaStoreUri, createdAt = java.time.Instant.ofEpochMilli(createdAtEpochMs), startedAt = startedAtEpochMs?.let(java.time.Instant::ofEpochMilli), completedAt = completedAtEpochMs?.let(java.time.Instant::ofEpochMilli), errorCode = errorCode, errorMessage = errorMessage)
    override suspend fun insert(job: DownloadJob) = dao.insert(job.toEntity())
    override suspend fun get(id: String) = dao.get(id)?.toDomain()
    override fun observe(id: String) = dao.observe(id).map { it?.toDomain() }
    override fun history() = dao.history().map { list -> list.map { it.toDomain() } }
    override suspend fun update(job: DownloadJob) = dao.update(job.toEntity())
    override suspend fun delete(id: String) = dao.delete(id)
}
