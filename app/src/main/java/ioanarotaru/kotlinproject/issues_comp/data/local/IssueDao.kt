package ioanarotaru.kotlinproject.issues_comp.data.local

import androidx.lifecycle.LiveData
import androidx.room.*
import ioanarotaru.kotlinproject.issues_comp.data.Issue

@Dao
interface IssueDao {
    @Query("SELECT * from issues")
    fun getAll(): LiveData<List<Issue>>

    @Query("SELECT * from issues")
    fun getAllRealData(): List<Issue>

    @Query("SELECT * FROM issues WHERE _id=:id ")
    fun getById(id: String): LiveData<Issue>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(issue: Issue)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(issue: Issue)

    @Delete
    suspend fun delete(issue: Issue)

    @Query("DELETE FROM issues")
    suspend fun deleteAll()
}