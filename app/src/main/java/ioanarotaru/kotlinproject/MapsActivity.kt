package ioanarotaru.kotlinproject

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import ioanarotaru.kotlinproject.core.TAG

class MapsActivity: AppCompatActivity(), GoogleMap.OnMyLocationButtonClickListener,
    GoogleMap.OnMyLocationClickListener, GoogleMap.OnMapLongClickListener, GoogleMap.OnMarkerClickListener, OnMapReadyCallback,
    ActivityCompat.OnRequestPermissionsResultCallback {

    private var permissionDenied = false
    private lateinit var mMap: GoogleMap
    private lateinit var marker: Marker

    private lateinit var latLng: LatLng

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        latLng = LatLng(intent.extras?.get("LATITUDE_VALUE") as Double, intent.extras?.get("LONGITUDE_VALUE") as Double)
        Log.d(TAG,"Received location: ${latLng.latitude} ${latLng.longitude}")

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        googleMap.setOnMyLocationButtonClickListener(this)
        googleMap.setOnMyLocationClickListener(this)
        googleMap.setOnMapLongClickListener(this)
        googleMap.setOnMarkerClickListener(this)
        enableMyLocation()

        if(this::latLng.isInitialized)
            marker = googleMap.addMarker(MarkerOptions().position(latLng).title("Issue Location"))
    }

    private fun enableMyLocation() {
        if (!::mMap.isInitialized) return
        // [START maps_check_location_permission]
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            mMap.isMyLocationEnabled = true
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return
        }
        if (isPermissionGranted(
                permissions,
                grantResults,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        ) {
            enableMyLocation()
        } else {
            permissionDenied = true
        }
    }

    companion object {
        /**
         * Request code for location permission request.
         *
         * @see .onRequestPermissionsResult
         */
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
        fun isPermissionGranted(
            grantPermissions: Array<String>, grantResults: IntArray,
            permission: String
        ): Boolean {
            for (i in grantPermissions.indices) {
                if (permission == grantPermissions[i]) {
                    return grantResults[i] == PackageManager.PERMISSION_GRANTED
                }
            }
            return false
        }
    }

    override fun onMyLocationButtonClick(): Boolean {
        return false
    }

    override fun onMapLongClick(point: LatLng) {
        if(this::marker.isInitialized)
            marker.remove()
        latLng = point
        marker = mMap.addMarker(MarkerOptions().position(point).title("Issue Location"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(point))
    }

    override fun onMyLocationClick(location: Location) {
        Log.d(TAG, "Location: ${location.latitude} ${location.longitude}")
        if(this::marker.isInitialized)
            marker.remove()
        latLng = LatLng(location.latitude, location.longitude)
        marker = mMap.addMarker(MarkerOptions().position(latLng).title("Issue Location"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))
    }

    override fun onMarkerClick(markerClicked: Marker?): Boolean {
        Log.w(TAG,"On marker clicked")
        setResult(Activity.RESULT_OK, Intent().putExtra("LATITUDE_VALUE",latLng.latitude).putExtra("LONGITUDE_VALUE",latLng.longitude))
        finish()
        return false
    }

}