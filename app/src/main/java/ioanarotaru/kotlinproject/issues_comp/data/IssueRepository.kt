package ioanarotaru.kotlinproject.issues_comp.data

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import ioanarotaru.kotlinproject.core.TAG
import ioanarotaru.kotlinproject.core.Result
import ioanarotaru.kotlinproject.issues_comp.data.local.IssueDao
import ioanarotaru.kotlinproject.issues_comp.data.local.IssuesDatabase
import kotlinx.coroutines.GlobalScope
import retrofit2.Response
import ro.ubbcluj.cs.ilazar.myapp2.todo.data.remote.IssueApi

class IssueRepository(private val issueDao: IssueDao) {
    val issues = issueDao.getAll()

    suspend fun refresh(): Result<Boolean> {
        try {
            val issues = IssueApi.service.find()
            for (issue in issues) {
                issueDao.insert(issue)
            }
            return Result.Success(true)
        } catch(e: Exception) {
            return Result.Error(e)
        }
    }

    fun getById(issueId: String): LiveData<Issue> {
        return issueDao.getById(issueId)
    }

    suspend fun save(issue: Issue): Result<Issue> {
        try {
            val createdIssue = IssueApi.service.create(issue)
            issueDao.insert(createdIssue)
            return Result.Success(createdIssue)
        } catch(e: Exception) {
            return Result.Error(e)
        }
    }

    suspend fun update(issue: Issue): Result<Issue> {
        try {
            val updatedIssue = IssueApi.service.update(issue._id, issue)
            issueDao.update(updatedIssue)
            return Result.Success(updatedIssue)
        } catch(e: Exception) {
            return Result.Error(e)
        }
    }

    suspend fun delete(issue: Issue): Result<Issue> {
        try {
            val deletedIssue = IssueApi.service.delete(issue._id)
            issueDao.delete(issue)
            return Result.Success(issue)
        } catch(e: Exception) {
            return Result.Error(e)
        }
    }

    companion object{
        suspend fun deleteAll(context : Context) {
            IssuesDatabase.getDatabase(context, GlobalScope).issueDao().deleteAll()
        }
    }
}