package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var remindersDatabase: RemindersDatabase
    private lateinit var remindersRepository: RemindersLocalRepository
    private lateinit var reminder: ReminderDTO

    @Before
    fun setup() {
        remindersDatabase = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).allowMainThreadQueries().build()
        reminder = ReminderDTO(
            title = "Test title",
            description = "Test description",
            location = "Test location",
            latitude = 1.0,
            longitude = 2.0
        )
        remindersRepository = RemindersLocalRepository(remindersDatabase.reminderDao(), Dispatchers.Unconfined)
    }

    @After
    fun clear() {
        remindersDatabase.close()
    }

    @Test
    fun testInsertAndGetReminder() = runBlockingTest {
        remindersRepository.saveReminder(reminder)

        val result = remindersRepository.getReminder(reminder.id) as Result.Success
        val loaded = result.data

        assertThat<ReminderDTO>(loaded as ReminderDTO, notNullValue())
        assertThat(loaded.id, `is`(reminder.id))
        assertThat(loaded.description, `is`(reminder.description))
        assertThat(loaded.location, `is`(reminder.location))
        assertThat(loaded.latitude, `is`(reminder.latitude))
        assertThat(loaded.longitude, `is`(reminder.longitude))
    }

    @Test
    fun deleteAllReminders() = runBlockingTest {
        remindersRepository.saveReminder(reminder)
        remindersRepository.deleteAllReminders()

        val result = remindersRepository.getReminders() as Result.Success
        val loaded = result.data
        assertThat(loaded.size, `is`(0))
    }

    @Test
    fun getReminderByIdIsError() = runBlockingTest {
        val loaded = remindersRepository.getReminder(reminder.id) as Result.Error

        assertThat(loaded.message, `is`(notNullValue()))
        assertThat(loaded.message, `is`("Reminder not found!"))
    }
}