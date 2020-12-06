package ioanarotaru.kotlinproject.issues_comp.data

import android.content.Context
import android.net.ConnectivityManager
import android.util.Log
import androidx.lifecycle.viewModelScope
import androidx.work.Worker
import androidx.work.WorkerParameters
import ioanarotaru.kotlinproject.core.TAG
import ioanarotaru.kotlinproject.issues_comp.data.local.IssuesDatabase
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import ro.ubbcluj.cs.ilazar.myapp2.todo.data.remote.IssueApi
import java.lang.Exception
import java.util.concurrent.TimeUnit

class Worker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {
    override fun doWork(): Result {
        // perform long running operation
        val issueDao = IssuesDatabase.getDatabase(applicationContext, GlobalScope).issueDao()
        val issueRepository = IssueRepository(issueDao,
            applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        )
        GlobalScope.launch {
            Log.w(TAG,"In worker corutine");
            val issues = issueDao.getAllRealData();
            for (issue in issues) {
                if (issue._id.contains("saved"))
                    try{
                        IssueApi.service.create(issue)
                    }
                    catch (e: Exception){
                        e.message?.let { Log.w(TAG, it) };
                    }
                else
                    try{
                        IssueApi.service.update(issue._id, issue)
                    }
                    catch (e: Exception){
                        e.message?.let { Log.w(TAG, it) }
                    }
            }
        }
        return Result.success()
    }
}