package ioanarotaru.kotlinproject.issues_comp.issues

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.util.Log
import androidx.lifecycle.*
import ioanarotaru.kotlinproject.core.TAG
import ioanarotaru.kotlinproject.core.Result
import ioanarotaru.kotlinproject.issues_comp.data.Issue
import ioanarotaru.kotlinproject.issues_comp.data.IssueRepository
import ioanarotaru.kotlinproject.issues_comp.data.local.IssuesDatabase
import kotlinx.coroutines.launch

class IssueListViewModel(application: Application) : AndroidViewModel(application) {
    private val mutableLoading = MutableLiveData<Boolean>().apply { value = false }
    private val mutableException = MutableLiveData<Exception>().apply { value = null }

    val issues: LiveData<List<Issue>>
    val loading: LiveData<Boolean> = mutableLoading
    val loadingError: LiveData<Exception> = mutableException

    val issueRepository: IssueRepository

    init {
        val issueDao = IssuesDatabase.getDatabase(application, viewModelScope).issueDao()
        issueRepository = IssueRepository(issueDao,
            application.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        )
        issues = issueRepository.issues

    }

    fun refresh() {
        viewModelScope.launch {
            Log.v(TAG, "refresh...");
            mutableLoading.value = true
            mutableException.value = null
            when (val result = issueRepository.refresh()) {
                is Result.Success -> {
                    Log.d(TAG, "refresh succeeded");
                }
                is Result.Error -> {
                    Log.w(TAG, "refresh failed", result.exception);
                    mutableException.value = Exception("Using local storage");
                }
            }
            mutableLoading.value = false
        }
    }
}