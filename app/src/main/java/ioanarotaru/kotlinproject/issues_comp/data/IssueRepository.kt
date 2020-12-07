package ioanarotaru.kotlinproject.issues_comp.data

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.util.Log
import androidx.core.content.ContextCompat.getSystemService
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import ioanarotaru.kotlinproject.core.ConnectivityLiveData
import ioanarotaru.kotlinproject.core.TAG
import ioanarotaru.kotlinproject.core.Result
import ioanarotaru.kotlinproject.issues_comp.data.local.IssueDao
import ioanarotaru.kotlinproject.issues_comp.data.local.IssuesDatabase
import kotlinx.coroutines.GlobalScope
import retrofit2.HttpException
import retrofit2.Response
import ro.ubbcluj.cs.ilazar.myapp2.todo.data.remote.IssueApi
import java.net.SocketTimeoutException

class IssueRepository(private val issueDao: IssueDao, private val connectivityManager: ConnectivityManager) {
    val issues = issueDao.getAll()
    var savedLocally = 1;


    fun isOnline(): Boolean {
        val networkInfo: NetworkInfo? = connectivityManager.activeNetworkInfo
        return networkInfo?.isConnected == true
    }

    suspend fun refresh(): Result<Boolean> {
        if(!isOnline()){
            return Result.Success(true);
        }
        try {
            val issues = IssueApi.service.find()
            issueDao.deleteAll()
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
        if(!isOnline()){
            val createdIssue = issue;
            createdIssue._id = "saved"+savedLocally;
            savedLocally += 1;
            issueDao.insert(createdIssue);
            return Result.Error(Exception("Saved locally"));
        }
        try {
            val createdIssue = IssueApi.service.create(issue)
            issueDao.insert(createdIssue)
            return Result.Success(createdIssue)
        }
        catch (e: SocketTimeoutException){
            val createdIssue = issue;
            createdIssue._id = "saved"+savedLocally;
            savedLocally += 1;
            issueDao.insert(createdIssue);
            return Result.Error(Exception("Saved locally"));
        }
        catch(e: Exception) {
            return Result.Error(e)
        }
    }

    suspend fun update(issue: Issue): Result<Issue> {
        if(!isOnline()){
            issueDao.update(issue);
            return Result.Error(Exception("Updated locally"));
        }
        try {
            val updatedIssue = IssueApi.service.update(issue._id, issue)
            issueDao.update(updatedIssue)
            return Result.Success(updatedIssue)
        } catch (e: SocketTimeoutException){
            issueDao.update(issue);
            return Result.Error(Exception("Updated locally"));
        }
        catch(e: Exception) {
            return Result.Error(e)
        }
    }

    suspend fun delete(issue: Issue): Result<Issue> {
        if(!isOnline()){
            issueDao.delete(issue)
            return Result.Error(Exception("Deleted locally"));
        }
        try {
            val deletedIssue = IssueApi.service.delete(issue._id)
            issueDao.delete(issue)
            return Result.Success(issue)
        } catch (e: SocketTimeoutException){
            issueDao.delete(issue)
            return Result.Error(Exception("Deleted locally"));
        }
        catch(e: Exception) {
            return Result.Error(e)
        }
    }

    companion object{
        suspend fun deleteAll(context : Context) {
            IssuesDatabase.getDatabase(context, GlobalScope).issueDao().deleteAll()
        }
    }
}