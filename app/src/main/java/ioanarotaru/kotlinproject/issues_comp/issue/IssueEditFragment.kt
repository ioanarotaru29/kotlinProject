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
import ioanarotaru.kotlinproject.issues_comp.data.Issue

class IssueEditFragment: Fragment() {
    companion object {
        const val ISSUE_ID = "ISSUE_ID"
    }

    private lateinit var viewModel: IssueEditViewModel
    private var issueId: String? = null
    private var issue: Issue? = null

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


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Log.v(TAG, "onActivityCreated")
        setupViewModel()
        fabSave.setOnClickListener {
            Log.v(TAG, "save issue")
            val i = issue
            if (i != null){
                i.title = issue_title.text.toString()
                i.description = issue_description.text.toString()
                i.state = issue_state.text.toString()
                viewModel.saveOrUpdateIssue(i)
            }
        }
        fabDelete.setOnClickListener {
            Log.v(TAG, "delete issue")
            val i = issue
            if(i != null){
                i.title = issue_title.text.toString()
                i.description = issue_description.text.toString()
                i.state = issue_state.text.toString()
                viewModel.deleteIssue(i)
            }
        }

    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this).get(IssueEditViewModel::class.java)
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
        viewModel.completed.observe(viewLifecycleOwner) { completed ->
            if (completed) {
                Log.v(TAG, "completed, navigate back")
                findNavController().popBackStack()
            }
        }
        val id = issueId
        if (id == null) {
            issue = Issue("", "","","")
        } else {
            viewModel.getIssueById(id).observe(viewLifecycleOwner) {
                Log.v(TAG, "update issues")
                if (it != null) {
                    issue = it
                    issue_title.setText(it.title)
                    issue_description.setText(it.description)
                    issue_state.setText(it.state)
                }
            }
        }
    }
}
