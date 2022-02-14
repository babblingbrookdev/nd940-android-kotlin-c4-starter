package com.udacity.project4.locationreminders.savereminder.selectreminderlocation

import com.google.android.gms.maps.model.PointOfInterest

sealed class SelectLocationEvent {
    data class MapLoading(val loading: Boolean) : SelectLocationEvent()
    object ForegroundPermissionsApproved : SelectLocationEvent()
    object BackgroundPermissionsApproved : SelectLocationEvent()
    object ForegroundPermissionsDenied : SelectLocationEvent()
    object BackgroundPermissionsDenied : SelectLocationEvent()
    object DeviceLocationEnabled : SelectLocationEvent()
    data class PoiSelected(val poi: PointOfInterest) : SelectLocationEvent()
    object SaveButtonClicked : SelectLocationEvent()
}