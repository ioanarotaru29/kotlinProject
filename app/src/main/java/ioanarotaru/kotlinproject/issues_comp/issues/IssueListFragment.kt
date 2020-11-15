package ioanarotaru.kotlinproject.issues_comp.issues

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import ioanarotaru.kotlinproject.R
import ioanarotaru.kotlinproject.auth.data.AuthRepository
import ioanarotaru.kotlinproject.core.TAG
import kotlinx.android.synthetic.main.fragment_issue_list.*

class IssueListFragment: Fragment() {
    private lateinit var issueListAdapter: IssueListAdapter
    private lateinit var issuesModel: IssueListViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.v(TAG, "onCreate")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_issue_list, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Log.v(TAG, "onActivityCreated")
        if (!AuthRepository.isLoggedIn) {
            findNavController().navigate(R.id.fragment_login)
            return;
        }
        setupIssueList()
        fab.setOnClickListener {
            Log.v(TAG, "add new issue")
            findNavController().navigate(R.id.fragment_issue_edit)
        }
    }

    private fun setupIssueList() {
        issueListAdapter = IssueListAdapter(this)
        issue_list.adapter = issueListAdapter
        issuesModel = ViewModelProvider(this).get(IssueListViewModel::class.java)
        issuesModel.issues.observe(viewLifecycleOwner) { issues ->
            Log.v(TAG, "update issues")
            issueListAdapter.issues = issues
        }
        issuesModel.loading.observe(viewLifecycleOwner) { loading ->
            Log.i(TAG, "update loading")
            progress.visibility = if (loading) View.VISIBLE else View.GONE
        }
        issuesModel.loadingError.observe(viewLifecycleOwner) { exception ->
            if (exception != null) {
                Log.i(TAG, "update loading error")
                val message = "Loading exception ${exception.message}"
                Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
            }
        }
        issuesModel.refresh()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.v(TAG, "onDestroy")
    }
}