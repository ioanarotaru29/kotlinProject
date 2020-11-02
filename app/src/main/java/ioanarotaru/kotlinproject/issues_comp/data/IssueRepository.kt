package ioanarotaru.kotlinproject.issues_comp.data

import android.util.Log
import ioanarotaru.kotlinproject.core.TAG
import retrofit2.Response
import ro.ubbcluj.cs.ilazar.myapp2.todo.data.remote.IssueApi

object IssueRepository {
    private var cachedIssues: MutableList<Issue>? = null;

    suspend fun loadAll(): List<Issue> {
        Log.i(TAG, "loadAll")
        if (cachedIssues != null) {
            return cachedIssues as List<Issue>;
        }
        cachedIssues = mutableListOf()
        val issues = IssueApi.service.find()
        cachedIssues?.addAll(issues)
        return cachedIssues as List<Issue>
    }

    suspend fun load(issueId: String): Issue {
        Log.i(TAG, "load")
        val issue = cachedIssues?.find { it.id == issueId }
        if (issue != null) {
            return issue
        }
        return IssueApi.service.read(issueId)
    }

    suspend fun save(issue: Issue): Issue {
        Log.i(TAG, "save")
        val createdIssue = IssueApi.service.create(issue)
        cachedIssues?.add(createdIssue)
        return createdIssue
    }

    suspend fun update(issue: Issue): Issue {
        Log.i(TAG, "update")
        val updatedIssue = IssueApi.service.update(issue.id, issue)
        val index = cachedIssues?.indexOfFirst { it.id == issue.id }
        if (index != null) {
            cachedIssues?.set(index, updatedIssue)
        }
        return updatedIssue
    }

    suspend fun delete(issue: Issue): Response<Void> {
        Log.i(TAG, "delete")
        val deletedIssue = IssueApi.service.delete(issue.id)
        val index = cachedIssues?.indexOfFirst { it.id == issue.id }
        if (index != null) {
            cachedIssues?.removeAt(index)
        }
        return deletedIssue
    }
}