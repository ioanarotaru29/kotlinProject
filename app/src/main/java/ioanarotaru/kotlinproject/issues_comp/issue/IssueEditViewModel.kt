package ioanarotaru.kotlinproject.issues_comp.issue

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ioanarotaru.kotlinproject.core.TAG
import ioanarotaru.kotlinproject.issues_comp.data.Issue
import ioanarotaru.kotlinproject.issues_comp.data.IssueRepository
import kotlinx.coroutines.launch

class IssueEditViewModel: ViewModel() {
    private val mutableIssue = MutableLiveData<Issue>().apply { value = Issue("", "","","") }
    private val mutableFetching = MutableLiveData<Boolean>().apply { value = false }
    private val mutableCompleted = MutableLiveData<Boolean>().apply { value = false }
    private val mutableException = MutableLiveData<Exception>().apply { value = null }

    val issue: LiveData<Issue> = mutableIssue
    val fetching: LiveData<Boolean> = mutableFetching
    val fetchingError: LiveData<Exception> = mutableException
    val completed: LiveData<Boolean> = mutableCompleted

    fun loadIssue(issueId: String) {
        viewModelScope.launch {
            Log.i(TAG, "loadIssue...")
            mutableFetching.value = true
            mutableException.value = null
            try {
                mutableIssue.value = IssueRepository.load(issueId)
                Log.i(TAG, "loadIssue succeeded")
                mutableFetching.value = false
            } catch (e: Exception) {
                Log.w(TAG, "loadIssue failed", e)
                mutableException.value = e
                mutableFetching.value = false
            }
        }
    }

    fun saveOrUpdateIssue(title: String, description: String, state: String) {
        viewModelScope.launch {
            Log.i(TAG, "saveOrUpdateIssue...");
            val issue = mutableIssue.value ?: return@launch
            issue.title = title
            issue.description = description
            issue.state = state
            mutableFetching.value = true
            mutableException.value = null
            try {
                if (issue.id.isNotEmpty()) {
                    mutableIssue.value = IssueRepository.update(issue)
                } else {
                    mutableIssue.value = IssueRepository.save(issue)
                }
                Log.i(TAG, "saveOrUpdateIssue succeeded");
                mutableCompleted.value = true
                mutableFetching.value = false
            } catch (e: Exception) {
                Log.w(TAG, "saveOrUpdateIssue failed", e);
                mutableException.value = e
                mutableFetching.value = false
            }
        }
    }

    fun deleteIssue() {
        viewModelScope.launch {
            Log.i(TAG, "saveOrUpdateIssue...");
            val issue = mutableIssue.value ?: return@launch
            mutableFetching.value = true
            mutableException.value = null
            try {
                mutableIssue.value = Issue("","","","")
                IssueRepository.delete(issue)
                Log.i(TAG, "deleteIssue succeeded");
                mutableCompleted.value = true
                mutableFetching.value = false
            } catch (e: Exception) {
                Log.w(TAG, "deleteIssue failed", e);
                mutableException.value = e
                mutableFetching.value = false
            }
        }
    }
}