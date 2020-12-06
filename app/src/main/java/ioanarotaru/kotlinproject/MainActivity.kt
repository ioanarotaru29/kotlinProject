package ioanarotaru.kotlinproject

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
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.observe
import ioanarotaru.kotlinproject.auth.data.AuthRepository
import ioanarotaru.kotlinproject.auth.data.TokenHolder
import ioanarotaru.kotlinproject.auth.data.User
import ioanarotaru.kotlinproject.core.ConnectivityLiveData
import ioanarotaru.kotlinproject.core.TAG
import ioanarotaru.kotlinproject.core.sp
import ioanarotaru.kotlinproject.issues_comp.data.IssueRepository
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {
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
            if(it)
                findViewById<TextView>(R.id.textViewState).setText("Connected");
            else
                findViewById<TextView>(R.id.textViewState).setText("Disconnected");


        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onStart() {
        super.onStart()
        connectivityManager.registerDefaultNetworkCallback(networkCallback)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onStop() {
        super.onStop()
        connectivityManager.unregisterNetworkCallback(networkCallback)
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
}