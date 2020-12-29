package ioanarotaru.kotlinproject.issues_comp.issues

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import ioanarotaru.kotlinproject.R
import ioanarotaru.kotlinproject.core.TAG
import ioanarotaru.kotlinproject.issues_comp.data.Issue
import ioanarotaru.kotlinproject.issues_comp.issue.IssueEditFragment
import kotlinx.android.synthetic.main.view_issue.view.*
import java.io.File

class IssueListAdapter (
    private val fragment: Fragment
) : RecyclerView.Adapter<IssueListAdapter.ViewHolder>() {
    var issues = emptyList<Issue>()
        set(value) {
            field = value
            notifyDataSetChanged();
        }

    private var onIssueClick: View.OnClickListener;

    init {
        onIssueClick = View.OnClickListener { view ->
            val issue = view.tag as Issue
            fragment.findNavController().navigate(R.id.fragment_issue_edit, Bundle().apply {
                putString(IssueEditFragment.ISSUE_ID, issue._id)
            })
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.view_issue, parent, false)
        Log.v(TAG, "onCreateViewHolder")
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Log.v(TAG, "onBindViewHolder $position")
        val issue = issues[position]
        holder.itemView.tag = issue
        holder.titleView.text = issue.title
        holder.descriptionView.text = issue.description
        holder.stateView.text = issue.state
        if(issue.photoPath != null && issue.photoPath != "")
            holder.imageView.setImageURI(Uri.fromFile(File(issue.photoPath)))
        holder.itemView.setOnClickListener(onIssueClick)
    }

    override fun getItemCount() = issues.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titleView: TextView = view.title
        val descriptionView: TextView = view.description
        val stateView: TextView = view.state
        val imageView: ImageView = view.image
    }
}