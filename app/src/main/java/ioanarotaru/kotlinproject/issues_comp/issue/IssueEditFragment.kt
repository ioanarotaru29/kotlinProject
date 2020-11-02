package ioanarotaru.kotlinproject.issues_comp.issue

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import ioanarotaru.kotlinproject.R
import ioanarotaru.kotlinproject.core.TAG
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_issue_edit.*
import androidx.lifecycle.observe

class IssueEditFragment: Fragment() {
    companion object {
        const val ISSUE_ID = "ISSUE_ID"
    }

    private lateinit var viewModel: IssueEditViewModel
    private var issueId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.v(TAG, "onCreate")
        arguments?.let {
            if (it.containsKey(ISSUE_ID)) {
                issueId = it.getString(ISSUE_ID).toString()
            }
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.v(TAG, "onCreateView")
        return inflater.inflate(R.layout.fragment_issue_edit, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.v(TAG, "onViewCreated")
        issue_title.setText(issueId)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Log.v(TAG, "onActivityCreated")
        setupViewModel()
        fabSave.setOnClickListener {
            Log.v(TAG, "save issue")
            viewModel.saveOrUpdateIssue(issue_title.text.toString(),issue_description.text.toString(),issue_state.text.toString())
        }
        fabDelete.setOnClickListener {
            Log.v(TAG, "delete issue")
            viewModel.deleteIssue()
        }

    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this).get(IssueEditViewModel::class.java)
        viewModel.issue.observe(viewLifecycleOwner) { issue ->
            Log.v(TAG, "update issues")
            issue_title.setText(issue.title)
            issue_description.setText(issue.description)
            issue_state.setText(issue.state)
        }
        viewModel.fetching.observe(viewLifecycleOwner) { fetching ->
            Log.v(TAG, "update fetching")
            progress.visibility = if (fetching) View.VISIBLE else View.GONE
        }
        viewModel.fetchingError.observe(viewLifecycleOwner) { exception ->
            if (exception != null) {
                Log.v(TAG, "update fetching error")
                val message = "Fetching exception ${exception.message}"
                val parentActivity = activity?.parent
                if (parentActivity != null) {
                    Toast.makeText(parentActivity, message, Toast.LENGTH_SHORT).show()
                }
            }
        }
        viewModel.completed.observe(viewLifecycleOwner, Observer { completed ->
            if (completed) {
                Log.v(TAG, "completed, navigate back")
                findNavController().navigateUp()
            }
        })
        val id = issueId
        if (id != null) {
            viewModel.loadIssue(id)
        }
    }
}
