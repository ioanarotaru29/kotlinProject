package ioanarotaru.kotlinproject

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.observe
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import ioanarotaru.kotlinproject.auth.data.AuthRepository
import ioanarotaru.kotlinproject.auth.data.TokenHolder
import ioanarotaru.kotlinproject.auth.data.User
import ioanarotaru.kotlinproject.core.ConnectivityLiveData
import ioanarotaru.kotlinproject.core.RemoteDataSource
import ioanarotaru.kotlinproject.core.TAG
import ioanarotaru.kotlinproject.core.sp
import ioanarotaru.kotlinproject.issues_comp.data.Issue
import ioanarotaru.kotlinproject.issues_comp.data.IssueRepository
import ioanarotaru.kotlinproject.issues_comp.data.Worker
import ioanarotaru.kotlinproject.issues_comp.data.local.IssuesDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject


class MainActivity : AppCompatActivity() {
    private var isActive = false;
    private lateinit var connectivityManager: ConnectivityManager
    private lateinit var connectivityLiveData: ConnectivityLiveData


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))

        Log.i(TAG, "onCreate")
        sp = getSharedPreferences("login", Context.MODE_PRIVATE);
        if(sp?.contains("token") == true && sp?.contains("username") == true && sp?.contains("password") == true){
            var username = sp?.getString("username","").toString()
            var password = sp?.getString("password", "").toString()
            var tokenHolder = TokenHolder(sp?.getString("token","").toString())
            AuthRepository.setLoggedInUser(User(username,password), tokenHolder)
        }

        connectivityManager = getSystemService(android.net.ConnectivityManager::class.java)
        connectivityLiveData = ConnectivityLiveData(connectivityManager)
        connectivityLiveData.observe(this) {
            Log.d(TAG, "connectivityLiveData $it")
            if(it){
                findViewById<TextView>(R.id.textViewState).setText("Connected");
                startWorker();
            }
            else
                findViewById<TextView>(R.id.textViewState).setText("Disconnected");


        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onStart() {
        super.onStart()
        connectivityManager.registerDefaultNetworkCallback(networkCallback)
        isActive = true
        CoroutineScope(Dispatchers.Main).launch { collectEvents() }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onStop() {
        super.onStop()
        connectivityManager.unregisterNetworkCallback(networkCallback)
        isActive = false
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_logout -> {
                AuthRepository.logout()
                GlobalScope.launch {
                    IssueRepository.deleteAll(this@MainActivity)
                }
                finish();
                overridePendingTransition( 0, 0);
                startActivity(intent);
                overridePendingTransition( 0, 0);
                return true;
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    val networkCallback = @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            Log.d(TAG, "The default network is now: " + network)
        }

        override fun onLost(network: Network) {
            Log.d(
                TAG,
                "The application no longer has a default network. The last default network was " + network
            )
        }

        override fun onCapabilitiesChanged(
            network: Network,
            networkCapabilities: NetworkCapabilities
        ) {
            Log.d(TAG, "The default network changed capabilities: " + networkCapabilities)
        }

        override fun onLinkPropertiesChanged(network: Network, linkProperties: LinkProperties) {
            Log.d(TAG, "The default network changed link properties: " + linkProperties)
        }
    }

    private fun startWorker() {
        // setup WorkRequest
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val myWork = OneTimeWorkRequest.Builder(Worker::class.java)
            .setConstraints(constraints)
            .build()
        val workId = myWork.id
        WorkManager.getInstance(this).apply {
            // enqueue Work
            enqueue(myWork)
            // observe work status
            getWorkInfoByIdLiveData(workId)
                .observe(this@MainActivity) { status ->
                    val isFinished = status?.state?.isFinished
                    Log.d(TAG, "Job $workId; finished: $isFinished")
                }
        }
        Toast.makeText(this, "Job $workId enqueued", Toast.LENGTH_SHORT).show()
    }

    private suspend fun collectEvents() {
        while (isActive) {
            val event = RemoteDataSource.eventChannel.receive()
            Log.d("MainActivity", "received $event")

            val issueDao = IssuesDatabase.getDatabase(applicationContext, GlobalScope).issueDao()

            val obj = JSONObject(event);
            val type = obj.getString("type");
            val json_issue = obj.getJSONObject("payload");
            val issue = Issue(json_issue.getString("_id"),json_issue.getString("title"),json_issue.getString("description"),json_issue.getString("state"));
            if(type == "saved"){
                issueDao.insert(issue)
            }
            if(type == "updated"){
                issueDao.update(issue)
            }
            if(type == "deleted"){
                issueDao.delete(issue)
            }
        }
    }
}