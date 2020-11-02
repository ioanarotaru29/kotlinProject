package ioanarotaru.kotlinproject.issues_comp.issues

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ioanarotaru.kotlinproject.core.TAG
import ioanarotaru.kotlinproject.issues_comp.data.Issue
import ioanarotaru.kotlinproject.issues_comp.data.IssueRepository
import kotlinx.coroutines.launch

class IssueListViewModel : ViewModel() {
    private val mutableIssues = MutableLiveData<List<Issue>>().apply { value = emptyList() }
    private val mutableLoading = MutableLiveData<Boolean>().apply { value = false }
    private val mutableException = MutableLiveData<Exception>().apply { value = null }

    val issues: LiveData<List<Issue>> = mutableIssues
    val loading: LiveData<Boolean> = mutableLoading
    val loadingError: LiveData<Exception> = mutableException

    fun createIssue(position: Int): Unit {
        val list = mutableListOf<Issue>()
        list.addAll(mutableIssues.value!!)
        list.add(Issue(position.toString(), "Issue " + position,"",""))
        mutableIssues.value = list
    }

    fun loadIssues() {
        viewModelScope.launch {
            Log.v(TAG, "loadIssues...");
            mutableLoading.value = true
            mutableException.value = null
            try {
                mutableIssues.value = IssueRepository.loadAll()
                Log.d(TAG, "loadIssues succeeded");
                mutableLoading.value = false
            } catch (e: Exception) {
                Log.w(TAG, "loadIssues failed", e);
                mutableException.value = e
                mutableLoading.value = false
            }
        }
    }
}