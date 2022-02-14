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
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.locationreminders.savereminder.selectreminderlocation.SelectLocationEvent
import com.udacity.project4.locationreminders.savereminder.selectreminderlocation.SelectLocationUIEvent
import com.udacity.project4.utils.SingleLiveEvent
import kotlinx.coroutines.launch

class SaveReminderViewModel(val app: Application, val dataSource: ReminderDataSource) :
    BaseViewModel(app) {

    val uiEvent = SingleLiveEvent<SelectLocationUIEvent>()

    val reminderTitle = MutableLiveData<String?>()
    val reminderDescription = MutableLiveData<String?>()
    val reminderSelectedLocationStr = MutableLiveData<String?>()

    val latitude = MutableLiveData<Double?>()
    val longitude = MutableLiveData<Double?>()

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
    fun validateAndSaveReminder(reminderData: ReminderDataItem) {
        if (validateEnteredData(reminderData)) {
            saveReminder(reminderData)
        }
    }

    /**
     * Save the reminder to the data source
     */
    fun saveReminder(reminderData: ReminderDataItem) {
        showLoading.value = true
        viewModelScope.launch {
            dataSource.saveReminder(
                ReminderDTO(
                    reminderData.title,
                    reminderData.description,
                    reminderData.location,
                    reminderData.latitude,
                    reminderData.longitude,
                    reminderData.id
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
    fun validateEnteredData(reminderData: ReminderDataItem): Boolean {
        if (reminderData.title.isNullOrEmpty()) {
            showSnackBarInt.value = R.string.err_enter_title
            return false
        }

        if (reminderData.location.isNullOrEmpty()) {
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

    fun navigateToSaveReminder() {
        navigationCommand.value = NavigationCommand.Back
    }
}