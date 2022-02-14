package com.udacity.project4.locationreminders.savereminder

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.PointOfInterest
import com.udacity.project4.R
import com.udacity.project4.base.BaseViewModel
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.savereminder.selectreminderlocation.SelectLocationEvent
import com.udacity.project4.locationreminders.savereminder.selectreminderlocation.SelectLocationUIEvent
import com.udacity.project4.utils.SingleLiveEvent
import kotlinx.coroutines.launch

class SaveReminderViewModel(val app: Application, private val dataSource: ReminderDataSource) :
    BaseViewModel(app) {

    val uiEvent = SingleLiveEvent<SelectLocationUIEvent>()

    val reminderTitle = MutableLiveData<String?>()
    val reminderDescription = MutableLiveData<String?>()
    private val reminderSelectedLocationStr = MutableLiveData<String?>()

    private val latitude = MutableLiveData<Double?>()
    private val longitude = MutableLiveData<Double?>()

    private val _selectedPOI = MutableLiveData<PointOfInterest?>()
    val selectedPOI: LiveData<PointOfInterest?> get() = _selectedPOI

    /**
     * Clear the live data objects to start fresh next time the view model gets called
     */
    fun onClear() {
        _selectedPOI.value = null
        reminderTitle.value = null
        reminderDescription.value = null
        reminderSelectedLocationStr.value = null
        latitude.value = null
        longitude.value = null
    }

    /**
     * Validate the entered data then saves the reminder data to the DataSource
     */
    fun validateAndSaveReminder() {
        if (validateEnteredData()) {
            saveReminder()
        }
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

    /**
     * Validate the entered data and show error to the user if there's any invalid data
     */
    private fun validateEnteredData(): Boolean {
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

    fun processEvent(event: SelectLocationEvent) {
        when(event) {
            is SelectLocationEvent.SaveButtonClicked -> {
                sendUiEvent(SelectLocationUIEvent.SaveClicked)
            }
            is SelectLocationEvent.PoiSelected -> {
                _selectedPOI.value = event.poi
                reminderSelectedLocationStr.value = event.poi.name
                latitude.value = event.poi.latLng.latitude
                longitude.value = event.poi.latLng.longitude
            }
            is SelectLocationEvent.DeviceLocationEnabled -> {
                sendUiEvent(SelectLocationUIEvent.DeviceLocationEnabled)
            }
            is SelectLocationEvent.ForegroundPermissionsApproved -> {
                sendUiEvent(SelectLocationUIEvent.ForegroundPermissionsApproved)
            }
            is SelectLocationEvent.BackgroundPermissionsApproved -> {
                sendUiEvent(SelectLocationUIEvent.BackgroundPermissionsApproved)
            }
            is SelectLocationEvent.ForegroundPermissionsDenied -> {
                showSnackBarInt.value = R.string.location_required_error
            }
            is SelectLocationEvent.BackgroundPermissionsDenied-> {
                showSnackBarInt.value = R.string.location_required_error
            }
            is SelectLocationEvent.MapLoading -> {
                showLoading.value = event.loading
            }
        }
    }

    private fun sendUiEvent(event: SelectLocationUIEvent) {
        uiEvent.value = event
    }

    fun navigateBack() {
        navigationCommand.value = NavigationCommand.Back
    }
}