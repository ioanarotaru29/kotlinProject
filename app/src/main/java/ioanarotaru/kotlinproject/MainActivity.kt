package ioanarotaru.kotlinproject

import android.app.Application
import android.content.Context
import android.os.Bundle
import android.util.Log
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.viewModelScope
import androidx.navigation.Navigation
import androidx.navigation.fragment.FragmentNavigator
import androidx.room.RoomDatabase
import ioanarotaru.kotlinproject.auth.data.AuthRepository
import ioanarotaru.kotlinproject.auth.data.TokenHolder
import ioanarotaru.kotlinproject.auth.data.User
import ioanarotaru.kotlinproject.core.Api
import ioanarotaru.kotlinproject.core.TAG
import ioanarotaru.kotlinproject.core.sp
import ioanarotaru.kotlinproject.issues_comp.data.IssueRepository
import ioanarotaru.kotlinproject.issues_comp.data.local.IssuesDatabase
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {


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
}