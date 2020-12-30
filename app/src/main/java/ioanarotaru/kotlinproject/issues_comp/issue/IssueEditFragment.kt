package ioanarotaru.kotlinproject.issues_comp.issue

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import ioanarotaru.kotlinproject.R
import ioanarotaru.kotlinproject.core.TAG
import kotlinx.android.synthetic.main.fragment_issue_edit.*
import androidx.lifecycle.observe
import ioanarotaru.kotlinproject.MapsActivity
import ioanarotaru.kotlinproject.issues_comp.data.Issue
import ioanarotaru.kotlinproject.utils.RealPathUtil
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class IssueEditFragment: Fragment() {
    companion object {
        const val ISSUE_ID = "ISSUE_ID"
    }

    private val REQUEST_PERMISSION = 10
    private val REQUEST_IMAGE_CAPTURE = 1
    private val REQUEST_PICK_IMAGE = 2
    private val REQUEST_LOCATION = 3

    lateinit var currentPhotoPath: String

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
        btTakePhoto.setOnClickListener { openCamera() }
        btOpenGallery.setOnClickListener { openGallery() }
        btOpenMap.setOnClickListener { openMap() }
    }

    override fun onResume() {
        super.onResume()
        checkCameraPermission()
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
            issue = Issue("", "","","", null, 0.0, 0.0)
        } else {
            viewModel.getIssueById(id).observe(viewLifecycleOwner) {
                Log.v(TAG, "update issues")
                if (it != null) {
                    issue = it
                    issue_title.setText(it.title)
                    issue_description.setText(it.description)
                    issue_state.setText(it.state)
                    if(it.photoPath != null){
                        ivImage.setImageURI(Uri.fromFile(File(it.photoPath)))
                    }
                    if(it.latitude == null)
                        issue?.latitude = 0.0
                    if(it.longitude == null)
                        issue?.longitude = 0.0
                }
            }
        }
    }

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this.requireContext(), Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this.requireActivity(),
                arrayOf(Manifest.permission.CAMERA),
                REQUEST_PERMISSION)
        }
    }

    private fun openCamera() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { intent ->
            intent.resolveActivity(this.requireActivity().packageManager)?.also {
                val photoFile: File? = try {
                    createCapturedPhoto()
                } catch (ex: IOException) {
                    null
                }
                Log.d("MainActivity", "photofile $photoFile");
                photoFile?.also {
                    val photoURI = FileProvider.getUriForFile(
                        this.requireContext(),
                        "ioanarotaru.kotlinproject.fileprovider",
                        it
                    )
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
                }
            }
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_PICK_IMAGE)
    }

    private fun openMap() {
        startActivityForResult(Intent(this.requireContext(), MapsActivity::class.java).also { intent ->
            intent.putExtra("LONGITUDE_VALUE", issue?.longitude)
            intent.putExtra("LATITUDE_VALUE", issue?.latitude)
        }, REQUEST_LOCATION)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == AppCompatActivity.RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE) {
                val uri = Uri.parse(currentPhotoPath)
                issue?.photoPath = currentPhotoPath
                ivImage.setImageURI(uri)
            }
            else if (requestCode == REQUEST_PICK_IMAGE) {
                val uri = data?.getData()
                issue?.photoPath = uri?.let { RealPathUtil.getRealPath(this.requireContext(), it) }
                ivImage.setImageURI(uri)
            }
            else if (requestCode == REQUEST_LOCATION){
                Log.d(TAG, "Returned from location");
                var extras = data?.extras
                issue?.latitude = extras?.get("LATITUDE_VALUE") as Double
                issue?.longitude = extras?.get("LONGITUDE_VALUE") as Double
                Log.d(TAG,"Location result: Lat -> ${issue?.latitude} Long -> ${issue?.longitude}")
            }
        }
    }

    @Throws(IOException::class)
    private fun createCapturedPhoto(): File {
        val timestamp: String = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(Date())
        val storageDir = this.requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("PHOTO_${timestamp}",".jpg", storageDir).apply {
            currentPhotoPath = absolutePath
        }
    }
}
