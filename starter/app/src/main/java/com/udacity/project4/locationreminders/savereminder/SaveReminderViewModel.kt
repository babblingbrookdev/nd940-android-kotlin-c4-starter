package com.udacity.project4.locationreminders.savereminder

import android.app.Application
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PointOfInterest
import com.udacity.project4.R
import com.udacity.project4.base.BaseViewModel
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.getSnippet
import kotlinx.coroutines.launch
import java.util.*

class SaveReminderViewModel(val app: Application, private val dataSource: ReminderDataSource) :
    BaseViewModel(app) {

    val reminderTitle = MutableLiveData<String?>()
    val reminderDescription = MutableLiveData<String?>()
    val reminderSelectedLocationStr = MutableLiveData<String?>()

    val latitude = MutableLiveData<Double?>()
    val longitude = MutableLiveData<Double?>()

    private val _selectedPOI = MutableLiveData<PointOfInterest?>()
    val selectedPOI: LiveData<PointOfInterest?> get() = _selectedPOI

    private val _mapSelection = MutableLiveData<LatLng?>()
    val mapSelection: LiveData<LatLng?> get() = _mapSelection

    /**
     * Clear the live data objects to start fresh next time the view model gets called
     */
    fun onClear() {
        _selectedPOI.value = null
        _mapSelection.value = null
        reminderTitle.value = null
        reminderDescription.value = null
        reminderSelectedLocationStr.value = null
        latitude.value = null
        longitude.value = null
    }

    /**
     * Save the reminder to the data source
     */
    fun saveReminder() {
        showLoading.value = true
        viewModelScope.launch {
            dataSource.saveReminder(
                ReminderDTO(
                    title = reminderTitle.value,
                    description = reminderDescription.value,
                    location = reminderSelectedLocationStr.value,
                    latitude = latitude.value,
                    longitude = longitude.value
                )
            )
            showLoading.value = false
            showToast.value = app.getString(R.string.reminder_saved)
            navigationCommand.value = NavigationCommand.Back
        }
    }

    fun getReminder(): ReminderDataItem {
        return ReminderDataItem(
            title = reminderTitle.value,
            description = reminderDescription.value,
            location = reminderSelectedLocationStr.value,
            latitude = latitude.value,
            longitude = longitude.value)
    }

    /**
     * Validate the entered data and show error to the user if there's any invalid data
     */
    fun validateEnteredData(): Boolean {
        if (reminderTitle.value.isNullOrEmpty()) {
            showSnackBarInt.value = R.string.err_enter_title
            return false
        }

        if (reminderSelectedLocationStr.value.isNullOrEmpty()) {
            showSnackBarInt.value = R.string.err_select_location
            return false
        }
        return true
    }

    fun onPoiSelected(poi: PointOfInterest) {
        _selectedPOI.value = poi
        _mapSelection.value = null
        reminderSelectedLocationStr.value = poi.name
        latitude.value = poi.latLng.latitude
        longitude.value = poi.latLng.longitude
    }

    fun onMapSelected(latLng: LatLng) {
        _mapSelection.value = latLng
        _selectedPOI.value = null
        reminderSelectedLocationStr.value = latLng.getSnippet()
        latitude.value = latLng.latitude
        longitude.value = latLng.longitude
    }

    fun navigateBack() {
        navigationCommand.value = NavigationCommand.Back
    }
}