package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.IntentSender
import android.content.res.Resources
import android.os.Bundle
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.PointOfInterest
import com.google.maps.android.ktx.addMarker
import com.google.maps.android.ktx.awaitMap
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.Permission
import com.udacity.project4.utils.PermissionManager
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import kotlinx.coroutines.launch
import logcat.logcat
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class SelectLocationFragment : BaseFragment() {

    override val _viewModel by sharedViewModel<SaveReminderViewModel>()
    private lateinit var binding: FragmentSelectLocationBinding

    private lateinit var map: GoogleMap

    private var mapMarker: Marker? = null

    private val permissionManager = PermissionManager.from(this)

    @SuppressLint("MissingPermission")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)

        viewLifecycleOwner.lifecycleScope.launch {
            _viewModel.showLoading.postValue(true)

            // initialize map
            val mapFragment: SupportMapFragment =
                childFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment
            map = mapFragment.awaitMap()

            _viewModel.showLoading.postValue(false)
            setMapStyle()
            setupListeners()
            setupObservers()

            // we need foreground location permissions first
            // for basic functionality
            requestForegroundPermissions()
        }

        return binding.root
    }

    private fun setupListeners() {
        map.setOnPoiClickListener { poi ->
            _viewModel.onPoiSelected(poi)
        }
        binding.saveButton.setOnClickListener {
            requestBackgroundPermissions()
        }
    }

    private fun setupObservers() {
        _viewModel.selectedPOI.observe(viewLifecycleOwner) { poi ->
            poi?.let {
                addMarker(it)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.normal_map -> {
            map.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            map.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            map.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            map.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    private fun setMapStyle() {
        try {
            val success = map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    requireActivity(),
                    R.raw.map_style
                )
            )
            if (!success) {
                logcat { "Style parsing failed" }
            }
        } catch (e: Resources.NotFoundException) {
            logcat { "Can't find style. Error: $e.asLog()" }
        }
    }

    @TargetApi(29)
    private fun requestForegroundPermissions() {
        permissionManager
            .request(Permission.ForegroundLocation)
            .rationale(getString(R.string.permission_denied_explanation))
            .checkPermission { granted ->
                if (granted) {
                    checkDeviceLocationSettings()
                } else {
                    _viewModel.showSnackBarInt.value = R.string.location_required_error
                }
            }
    }

    @TargetApi(29)
    private fun requestBackgroundPermissions() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            permissionManager.request(Permission.BackgroundLocation)
                .rationale(getString(R.string.permission_denied_explanation))
                .checkPermission { granted ->
                    if (granted) {
                        _viewModel.navigationCommand.value = NavigationCommand.Back
                    } else {
                        _viewModel.showSnackBarInt.value = R.string.location_required_error
                    }
                }
        }
    }

    @SuppressLint("MissingPermission")
    private fun checkDeviceLocationSettings(resolve: Boolean = true) {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_LOW_POWER
        }
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)

        val settingsClient = LocationServices.getSettingsClient(requireActivity())
        val locationSettingsResponseTask = settingsClient.checkLocationSettings(builder.build())

        locationSettingsResponseTask.addOnFailureListener { exception ->
            if (exception is ResolvableApiException && resolve) {
                try {
                    exception.startResolutionForResult(
                        requireActivity(),
                        REQUEST_TURN_ON_DEVICE_LOCATION
                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                    logcat { "Error getting location settings resolution: $sendEx.asLog()" }
                }
            }
        }
        locationSettingsResponseTask.addOnCompleteListener {
            map.isMyLocationEnabled = true
            getLocation()
        }
    }

    private fun getLocation() {
        try {
            val fusedLocationProviderClient =
                LocationServices.getFusedLocationProviderClient(requireActivity())
            val locationResult = fusedLocationProviderClient.lastLocation
            locationResult.addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    task.result?.let { location ->
                        if (mapMarker == null) {
                            logcat { "Location result received, moving camera" }
                            moveCamera(LatLng(location.latitude, location.longitude))
                        } else {
                            logcat { "Current map marker found, moving camera to marker position" }
                        }
                    }
                } else {
                    logcat { "Current location is null, using defaults. Exception: $task.exception.asLog()" }
                    map.uiSettings.isMyLocationButtonEnabled = false
                }
            }
        } catch (e: SecurityException) {
            logcat { "Exception: $e.asLog()" }
        }
    }

    private fun addMarker(selectedPoi: PointOfInterest) {
        mapMarker?.remove()
        mapMarker = map.addMarker {
            title(selectedPoi.name)
            position(selectedPoi.latLng)
        }
        moveCamera(selectedPoi.latLng)
    }

    private fun moveCamera(latLng: LatLng) {
        if (this::map.isInitialized) {
            map.moveCamera(
                CameraUpdateFactory.newLatLngZoom(
                    latLng, DEFAULT_ZOOM_FACTOR
                )
            )
        }
    }
}

private const val REQUEST_TURN_ON_DEVICE_LOCATION = 1
private const val DEFAULT_ZOOM_FACTOR = 15f
