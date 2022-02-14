package com.udacity.project4.locationreminders.savereminder

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.locationreminders.geofence.GeofenceBroadcastReceiver
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.Permission
import com.udacity.project4.utils.PermissionManager
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import logcat.logcat
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

@SuppressLint("UnspecifiedImmutableFlag")
class SaveReminderFragment : BaseFragment() {
    //Get the view model this time as a single to be shared with the another fragment
    //override val _viewModel: SaveReminderViewModel by inject()
    override val _viewModel by sharedViewModel<SaveReminderViewModel>()
    private lateinit var binding: FragmentSaveReminderBinding

    private val permissionManager = PermissionManager.from(this)

    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(requireActivity(), GeofenceBroadcastReceiver::class.java)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            PendingIntent.getBroadcast(
                requireActivity(),
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        } else {
            PendingIntent.getBroadcast(
                requireActivity(),
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        }
    }

    private lateinit var geofencingClient: GeofencingClient

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_save_reminder, container, false)

        setDisplayHomeAsUpEnabled(true)

        binding.viewModel = _viewModel
        geofencingClient = GeofencingClient(requireActivity())

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
        binding.selectLocation.setOnClickListener {
            _viewModel.navigationCommand.value =
                NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment())
        }

        binding.saveReminder.setOnClickListener {
            if (_viewModel.validateEnteredData()) {
                addGeofence(_viewModel.getReminder())
            }
        }
    }

    private fun addGeofence(reminder: ReminderDataItem?) {
        reminder?.let {
            permissionManager.request(Permission.ForegroundLocation)
                .rationale(getString(R.string.permission_denied_explanation))
                .checkPermission { granted ->
                    if (granted) {
                        val geofence =
                            Geofence.Builder()
                                .setRequestId(reminder.id)
                                .setCircularRegion(reminder.latitude!!, reminder.longitude!!, 300f)
                                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                                .build()

                        val request = GeofencingRequest.Builder().apply {
                            setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                                .addGeofence(geofence)
                        }.build()
                        geofencingClient.addGeofences(request, geofencePendingIntent).run {
                            addOnSuccessListener {
                                _viewModel.saveReminder()
                                _viewModel.showSnackBarInt.value = R.string.reminder_saved
                            }
                            addOnFailureListener {
                                _viewModel.showSnackBarInt.value =
                                    R.string.error_adding_geofence
                            }
                        }
                    } else {
                        logcat { "No permissions to add geofence" }
                        _viewModel.showSnackBarInt.value = R.string.location_required_error
                    }
                }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        //make sure to clear the view model after destroy, as it's a single view model.
        _viewModel.onClear()
    }
}
