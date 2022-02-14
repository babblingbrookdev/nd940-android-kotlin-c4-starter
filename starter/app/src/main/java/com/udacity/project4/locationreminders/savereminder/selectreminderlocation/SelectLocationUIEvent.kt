package com.udacity.project4.locationreminders.savereminder.selectreminderlocation

sealed class SelectLocationUIEvent {
    object ForegroundPermissionsApproved : SelectLocationUIEvent()
    object BackgroundPermissionsApproved : SelectLocationUIEvent()
    object DeviceLocationEnabled : SelectLocationUIEvent()
    object SaveClicked : SelectLocationUIEvent()
}