package com.suyash.mockcivilaviationexam.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.suyash.mockcivilaviationexam.data.local.entities.CacheMetadata

@Dao
interface CacheMetadataDao {
    @Query("SELECT * FROM cache_metadata WHERE cacheKey = :key")
    suspend fun getMetadata(key: String): CacheMetadata?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(metadata: CacheMetadata)

    @Query("DELETE FROM cache_metadata WHERE cacheKey = :key")
    suspend fun delete(key: String)

    @Query("DELETE FROM cache_metadata")
    suspend fun deleteAll()
}
