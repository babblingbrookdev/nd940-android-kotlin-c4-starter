package com.udacity.project4.locationreminders.savereminder

import android.app.Application
import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PointOfInterest
import com.google.common.truth.Truth.assertThat
import com.udacity.project4.R
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeReminderDataSource
import com.udacity.project4.locationreminders.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config

@ExperimentalCoroutinesApi
@Config(sdk = [Build.VERSION_CODES.P])
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {


    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private lateinit var applicationContext: Application
    private lateinit var viewModel: SaveReminderViewModel
    private lateinit var fakeReminderDataSource: FakeReminderDataSource

    @Before
    fun setup() {
        applicationContext = ApplicationProvider.getApplicationContext()
        fakeReminderDataSource = FakeReminderDataSource()
        viewModel = SaveReminderViewModel(
            applicationContext,
            fakeReminderDataSource
        )
    }

    @After
    fun clear() = runBlockingTest {
        viewModel.reminderSelectedLocationStr.value = null
        viewModel.reminderDescription.value = null
        viewModel.reminderTitle.value = null
        stopKoin()
    }

    @Test
    fun clearDataTest() {
        viewModel.reminderTitle.value = "Test title"
        viewModel.reminderSelectedLocationStr.value = "Test location"
        viewModel.reminderDescription.value = "Test description"
        viewModel.latitude.value = 1.0
        viewModel.longitude.value = 2.0

        viewModel.onClear()

        assertThat(viewModel.reminderSelectedLocationStr.value).isEqualTo(null)
        assertThat(viewModel.reminderDescription.value).isEqualTo(null)
        assertThat(viewModel.latitude.value).isEqualTo(null)
        assertThat(viewModel.longitude.value).isEqualTo(null)
    }

    @Test
    fun validatedReminderReturnsTrue() {
        viewModel.reminderTitle.value = "Test title"
        viewModel.reminderSelectedLocationStr.value = "Test location"
        assertThat(viewModel.validateEnteredData()).isTrue()
    }

    @Test
    fun noTitleValidation() {
        viewModel.reminderTitle.value = ""
        assertThat(viewModel.validateEnteredData()).isFalse()
    }

    @Test
    fun noLocationValidation() {
        viewModel.reminderSelectedLocationStr.value = ""
        assertThat(viewModel.validateEnteredData()).isFalse()
    }

    @Test
    fun loadingValueReturnsFalseAfterDataIsPresent() = runBlockingTest {
        mainCoroutineRule.pauseDispatcher()
        viewModel.saveReminder()
        assertThat(viewModel.showLoading.getOrAwaitValue()).isTrue()
        mainCoroutineRule.resumeDispatcher()
        assertThat(viewModel.showLoading.getOrAwaitValue()).isFalse()
    }

    @Test
    fun saveReminderSendsNavigationCommand() = runBlockingTest {
        viewModel.saveReminder()
        assertThat(viewModel.navigationCommand.getOrAwaitValue()).isEqualTo(NavigationCommand.Back)
    }

    @Test
    fun saveReminderDisplaysToast() = runBlockingTest {
        viewModel.saveReminder()
        assertThat(viewModel.showToast.getOrAwaitValue()).isEqualTo(
            applicationContext.resources.getString(
                R.string.reminder_saved
            )
        )
    }

    @Test
    fun onPoiSelectedSetsData() = runBlockingTest {
        val testPoi = PointOfInterest(LatLng(1.0, 1.0), "TestId", "TestName")
        viewModel.onPoiSelected(testPoi)

        assertThat(viewModel.selectedPOI.getOrAwaitValue()).isEqualTo(testPoi)
        assertThat(viewModel.latitude.getOrAwaitValue()).isEqualTo(testPoi.latLng.latitude)
        assertThat(viewModel.longitude.getOrAwaitValue()).isEqualTo(testPoi.latLng.longitude)
        assertThat(viewModel.reminderSelectedLocationStr.getOrAwaitValue()).isEqualTo(testPoi.name)
    }

    @Test
    fun getReminderReturnsValidReminderItemWithVMData() = runBlockingTest {
        viewModel.reminderTitle.value = "Test Title"
        viewModel.reminderDescription.value = "Test Description"
        viewModel.reminderSelectedLocationStr.value = "Test location"
        viewModel.latitude.value = 1.0
        viewModel.longitude.value = 2.0

        assertThat(viewModel.getReminder().title).isEqualTo("Test Title")
        assertThat(viewModel.getReminder().description).isEqualTo("Test Description")
        assertThat(viewModel.getReminder().location).isEqualTo("Test location")
        assertThat(viewModel.getReminder().latitude).isEqualTo(1.0)
        assertThat(viewModel.getReminder().longitude).isEqualTo(2.0)
    }

    @Test
    fun navigateBackSetsNavigationValueBack() = runBlockingTest {
        viewModel.navigateBack()
        assertThat(viewModel.navigationCommand.getOrAwaitValue()).isEqualTo(NavigationCommand.Back)
    }
}