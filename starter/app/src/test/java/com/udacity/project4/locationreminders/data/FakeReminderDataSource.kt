package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeReminderDataSource : ReminderDataSource {

    private val fakeData = mutableListOf<ReminderDTO>()

    private var shouldReturnError = false

    fun setReturnError(value: Boolean) {
        shouldReturnError = value
    }

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        if (shouldReturnError) {
            return Result.Error("Test Exception")
        }
        return Result.Success(fakeData)
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        fakeData.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        if (shouldReturnError) {
            return Result.Error("Test exception")
        }
        val reminder: ReminderDTO? = fakeData.firstOrNull {
            it.id == id
        }
        reminder?.let {
            return Result.Success(reminder)
        }
        return Result.Error("Reminder with id: $id not found")
    }

    override suspend fun deleteAllReminders() {
        fakeData.clear()
    }

    fun addTasks(vararg reminders: ReminderDTO) {
        for (reminder in reminders) {
            fakeData.add(reminder)
        }
    }
}