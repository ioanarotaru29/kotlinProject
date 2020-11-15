package ioanarotaru.kotlinproject.issues_comp.issue

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
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
        issueRepository = IssueRepository(issueDao)
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
                    mutableException.value = result.exception
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