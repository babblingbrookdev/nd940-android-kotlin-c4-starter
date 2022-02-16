package com.udacity.project4.locationreminders.reminderslist

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeAuthDataSource
import com.udacity.project4.locationreminders.data.FakeReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
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

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.P])
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private lateinit var viewModel: RemindersListViewModel
    private lateinit var fakeReminderDataSource: FakeReminderDataSource
    private lateinit var fakeAuthDataSource: FakeAuthDataSource

    @Before
    fun setup() {
        fakeReminderDataSource = FakeReminderDataSource()
        fakeAuthDataSource = FakeAuthDataSource()
        viewModel = RemindersListViewModel(ApplicationProvider.getApplicationContext(), fakeReminderDataSource, fakeAuthDataSource)
        val reminder1 = ReminderDTO("One", "Desc1", "Location1", 1.0, 1.0)
        val reminder2 = ReminderDTO("Two", "Desc2", "Location2", 2.0, 2.0 )
        val reminder3 = ReminderDTO("Three", "Desc3", "Location3", 3.0, 3.0)
        val reminder4 = ReminderDTO("Four", "Desc4", "Location4", 2.0, 2.0 )
        fakeReminderDataSource.addTasks(reminder1, reminder2, reminder3, reminder4)
    }

    @After
    fun clear() = runBlockingTest {
        fakeReminderDataSource.deleteAllReminders()
        stopKoin()
    }

    @Test
    fun loadRemindersTest() = runBlockingTest {
        viewModel.loadReminders()

        val reminders = viewModel.remindersList.getOrAwaitValue()

        assertThat(reminders).isNotEmpty()
        assertThat(reminders).hasSize(4)
    }

    @Test
    fun loadRemindersWithErrorTest() = runBlockingTest {
        fakeReminderDataSource.setReturnError(true)
        viewModel.loadReminders()

        assertThat(viewModel.showSnackBar.getOrAwaitValue().equals("Test Exception"))
    }

    @Test
    fun testShowNoDataIsFalseWithData() = runBlockingTest {
        viewModel.loadReminders()

        assertThat(viewModel.showNoData.getOrAwaitValue()).isFalse()
    }

    @Test
    fun testShowNoDataIsTrueWhenEmpty() = runBlockingTest {
        fakeReminderDataSource.deleteAllReminders()
        viewModel.loadReminders()

        assertThat(viewModel.showNoData.getOrAwaitValue()).isTrue()
    }

    @Test
    fun loadingValueReturnsFalseAfterDataIsPresent() = runBlockingTest {
        mainCoroutineRule.pauseDispatcher()
        viewModel.loadReminders()
        assertThat(viewModel.showLoading.getOrAwaitValue()).isTrue()
        mainCoroutineRule.resumeDispatcher()
        assertThat(viewModel.showLoading.getOrAwaitValue()).isFalse()
    }

}