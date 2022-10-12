package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.content.res.Resources
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.*
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.BuildConfig
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import org.koin.android.ext.android.inject
import java.util.*
import kotlin.concurrent.timerTask

class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding
    private lateinit var mMap: GoogleMap
    private val REQUEST_LOCATION_PERMISSION = 1
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var homeLatLng: LatLng
    private lateinit var selectedPoi: PointOfInterest
    private val runningQOrLater = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
    private lateinit var ctx: Context

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)
        //  setDisplayHomeAsUpEnabled(true)

        (childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?)?.getMapAsync(this)
        fusedLocationClient =
            activity?.let { LocationServices.getFusedLocationProviderClient(it) }!!

        binding.SaveBTN.setOnClickListener {
            onLocationSelected(selectedPoi)
        }


        return binding.root
    }

    private fun onLocationSelected(poi: PointOfInterest) {
        binding.viewModel!!.latitude.value = poi.latLng.latitude
        binding.viewModel!!.longitude.value = poi.latLng.longitude
        binding.viewModel!!.selectedPOI.value = poi
        binding.viewModel!!.reminderSelectedLocationStr.value = poi.name


        Timer().schedule(timerTask {
            findNavController().popBackStack()
        }, 350)
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        // Change the map type based on the user's selection.
        R.id.normal_map -> {
            mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            mMap.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            mMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            mMap.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        setOnMapClick(mMap)
        setMapStyle(mMap)
        //enableMyLocation()
        checkPermissionsAndGetUserLocation()
        //for test purpose only
        setOnMapLongClick(mMap)
    }

    private fun moveCameraToLocation(location: Location?) {
        homeLatLng = if(location!=null){
            LatLng(location.latitude, location.longitude)
        }else{
            LatLng(29.9825327, 31.1436696)
        }

        val zoomLevel = 16f
        addHomeMarker(homeLatLng)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(homeLatLng, zoomLevel))


    }

    private fun addHomeMarker(latLng: LatLng) {
        mMap.addMarker(MarkerOptions().apply {
            position(latLng)
            title("Home")
        })

    }

    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        Log.e(TAG,"dada")

        mMap.isMyLocationEnabled = true
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                // Got last known location. In some rare situations this can be null.
                moveCameraToLocation(location)

            }
    }


   /* override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            REQUEST_LOCATION_PERMISSION -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    Log.e(TAG, "Permission is granted.")
                    enableMyLocation()
                } else {
                    Log.e(TAG, "Permission is not granted.")

                }
                return
            }
        }
    }*/

    private fun setMapStyle(map: GoogleMap) {
        try {
            // Customize the styling of the base map using a JSON object defined
            // in a raw resource file.
            val success = map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    context,
                    R.raw.map_style
                )
            )

            if (!success) {
                Log.e(TAG, "Style parsing failed.")
            }
        } catch (e: Resources.NotFoundException) {
            Log.e(TAG, "Can't find style. Error: ", e)
        }
    }

    private fun setOnMapClick(map: GoogleMap) {

        map.setOnPoiClickListener { poi ->
            map.clear()
            addHomeMarker(homeLatLng)
            val poiMarker = map.addMarker(
                MarkerOptions()
                    .position(poi.latLng)
                    .title(poi.name)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
            )
            poiMarker?.showInfoWindow()
            selectedPoi = poi

            binding.SaveBTN.visibility = View.VISIBLE
        }
    }

    private fun setOnMapLongClick(map: GoogleMap) {

        map.setOnMapLongClickListener {
            map.clear()
            map.addMarker(
                MarkerOptions()
                    .position(it)
                    .title(getString(R.string.dropped_pin))
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
            )
            binding.SaveBTN.visibility = View.VISIBLE

            val fakePoiForTest =
                PointOfInterest(LatLng(it.latitude, it.longitude), "ForTest", "Fake Location")
            selectedPoi = fakePoiForTest

        }
    }
    override fun onAttach(context: Context) {
        super.onAttach(context)
        ctx = context
    }

    override fun onResume() {
        super.onResume()
        ctx = requireContext()
    }






//region permission check

    private fun checkPermissionsAndGetUserLocation() {
        if (foregroundLocationPermissionApproved()) {
            checkDeviceLocationSettingsAndGetUserLocation()
        } else {
            requestForegroundLocationPermissions()
        }
    }

    private fun checkDeviceLocationSettingsAndGetUserLocation(resolve:Boolean = true) {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_LOW_POWER
        }
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val settingsClient = LocationServices.getSettingsClient(ctx)
        val locationSettingsResponseTask =
            settingsClient.checkLocationSettings(builder.build())
        locationSettingsResponseTask.addOnFailureListener { exception ->
            if (exception is ResolvableApiException && resolve){
                try {
                    //for some Reason this line didn't work ( addOnCompleteListener didn't work )
                    //exception.startResolutionForResult(activity as Activity,REQUEST_TURN_DEVICE_LOCATION_ON)

                    //had to use this https://stackoverflow.com/a/50341594/9511299
                    startIntentSenderForResult(
                        exception.resolution.intentSender,
                        REQUEST_TURN_DEVICE_LOCATION_ON,
                        null,
                        0, 0, 0,
                        null
                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                    Log.d(TAG, "Error getting location settings resolution: " + sendEx.message)
                }
            } else {
                Snackbar.make(
                    binding.root,
                    R.string.location_required_error, Snackbar.LENGTH_INDEFINITE
                ).setAction(android.R.string.ok) {
                    checkDeviceLocationSettingsAndGetUserLocation()
                }.show()
            }
        }
        locationSettingsResponseTask.addOnCompleteListener {

            if ( it.isSuccessful ) {
                enableMyLocation()

            }
        }
    }

    @TargetApi(29)
    private fun foregroundLocationPermissionApproved(): Boolean {
        return (PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
            ctx,
            Manifest.permission.ACCESS_FINE_LOCATION
        ))
    }

    @TargetApi(29)
    private fun requestForegroundLocationPermissions() {
        if (foregroundLocationPermissionApproved())
            return
        val permissionsArray = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)

        requestPermissions(
            permissionsArray,
            REQUEST_LOCATION_PERMISSION
        )
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_TURN_DEVICE_LOCATION_ON) {
            checkDeviceLocationSettingsAndGetUserLocation(false)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        Log.d(TAG, "onRequestPermissionResult")

        if (requestCode == REQUEST_LOCATION_PERMISSION && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            checkDeviceLocationSettingsAndGetUserLocation()
        }else{
            Snackbar.make(
                binding.root,
                R.string.permission_denied_explanation,
                Snackbar.LENGTH_INDEFINITE
            )
                .setAction(R.string.settings) {
                    startActivity(Intent().apply {
                        action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    })
                }.show()
        }
    }
    //endregion


}

const val GEOFENCE_RADIUS_IN_METERS = 100f
private const val TAG = "SelectLocationFragment"
private const val REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE = 33
private const val REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE = 34
const val REQUEST_TURN_DEVICE_LOCATION_ON = 29
private const val LOCATION_PERMISSION_INDEX = 0
private const val BACKGROUND_LOCATION_PERMISSION_INDEX = 1