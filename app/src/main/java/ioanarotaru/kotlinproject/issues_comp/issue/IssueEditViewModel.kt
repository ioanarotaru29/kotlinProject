package ioanarotaru.kotlinproject.issues_comp.issue

import android.app.Application
import android.net.ConnectivityManager
import android.net.Network
import android.util.Log
import androidx.core.content.ContextCompat.getSystemService
import androidx.lifecycle.*
import android.content.Context
import android.net.LinkProperties
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.observe
import ioanarotaru.kotlinproject.core.ConnectivityLiveData
import ioanarotaru.kotlinproject.core.TAG
import ioanarotaru.kotlinproject.core.Result
import ioanarotaru.kotlinproject.issues_comp.data.Issue
import ioanarotaru.kotlinproject.issues_comp.data.IssueRepository
import ioanarotaru.kotlinproject.issues_comp.data.local.IssuesDatabase
import kotlinx.coroutines.launch

class IssueEditViewModel(application: Application) : AndroidViewModel(application) {
    private val mutableFetching = MutableLiveData<Boolean>().apply { value = false }
    private val mutableCompleted = MutableLiveData<Boolean>().apply { value = false }
    private val mutableException = MutableLiveData<Exception>().apply { value = null }

    val fetching: LiveData<Boolean> = mutableFetching
    val fetchingError: LiveData<Exception> = mutableException
    val completed: LiveData<Boolean> = mutableCompleted

    val issueRepository: IssueRepository

    init {
        val issueDao = IssuesDatabase.getDatabase(application, viewModelScope).issueDao()
        issueRepository = IssueRepository(issueDao,
            application.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        );
    }

    fun getIssueById(issueId: String): LiveData<Issue> {
        Log.v(TAG, "getItemById...")
        return issueRepository.getById(issueId)
    }

    fun saveOrUpdateIssue(issue: Issue) {
        viewModelScope.launch {
            Log.i(TAG, "saveOrUpdateIssue...");
            mutableFetching.value = true
            mutableException.value = null
            val result: Result<Issue>
            if (issue._id.isNotEmpty()) {
                result = issueRepository.update(issue)
            } else {
                result = issueRepository.save(issue)
            }
            when(result) {
                is Result.Success -> {
                    Log.d(TAG, "saveOrUpdateItem succeeded");
                }
                is Result.Error -> {
                    Log.w(TAG, "saveOrUpdateItem failed", result.exception);
                    mutableException.value = Exception("Saved or updated locally");
                }
            }
            mutableCompleted.value = true
            mutableFetching.value = false
        }
    }

    fun deleteIssue(issue: Issue) {
        viewModelScope.launch {
            Log.i(TAG, "deleteIssue...");
            mutableFetching.value = true
            mutableException.value = null
            issueRepository.delete(issue)
            mutableCompleted.value = true
            mutableFetching.value = false
        }
    }
}