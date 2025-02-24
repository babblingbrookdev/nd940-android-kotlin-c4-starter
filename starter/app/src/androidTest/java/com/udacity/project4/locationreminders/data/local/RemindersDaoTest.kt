package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import com.udacity.project4.locationreminders.data.dto.ReminderDTO

import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;

import kotlinx.coroutines.ExperimentalCoroutinesApi;
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Test
import java.time.Instant

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var remindersDatabase: RemindersDatabase
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
    }

    @After
    fun clear() {
        remindersDatabase.close()
    }

    @Test
    fun insertReminderAndGetById() = runBlockingTest {
        remindersDatabase.reminderDao().saveReminder(reminder)

        val loaded = remindersDatabase.reminderDao().getReminderById(reminder.id)

        assertThat<ReminderDTO>(loaded as ReminderDTO, notNullValue())
        assertThat(loaded.id, `is`(reminder.id))
        assertThat(loaded.description, `is`(reminder.description))
        assertThat(loaded.location, `is`(reminder.location))
        assertThat(loaded.latitude, `is`(reminder.latitude))
        assertThat(loaded.longitude, `is`(reminder.longitude))
    }

    @Test
    fun deleteAllReminders() = runBlockingTest {
        remindersDatabase.reminderDao().saveReminder(reminder)
        remindersDatabase.reminderDao().deleteAllReminders()

        val reminders = remindersDatabase.reminderDao().getReminders()

        assertThat(reminders.size, `is`(0))
    }
}
