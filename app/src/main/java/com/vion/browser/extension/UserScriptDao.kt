/*
 * Copyright 2026 Vion Browser Contributors. GPL v3.
 */
package com.vion.browser.extension

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface UserScriptDao {
    @Query("SELECT * FROM user_scripts ORDER BY name ASC")
    fun getAll(): LiveData<List<UserScript>>

    @Query("SELECT * FROM user_scripts WHERE enabled = 1")
    suspend fun getEnabled(): List<UserScript>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(script: UserScript): Long

    @Update
    suspend fun update(script: UserScript)

    @Delete
    suspend fun delete(script: UserScript)
}
