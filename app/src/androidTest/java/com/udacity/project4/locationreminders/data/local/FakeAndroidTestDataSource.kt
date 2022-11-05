package com.udacity.project4.locationreminders.data.local

import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result


class FakeAndroidTestDataSource(var reminders: MutableList<ReminderDTO>? = mutableListOf()) :
    ReminderDataSource {

    private var isError = false

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        return if (reminders.isNullOrEmpty()){
            Result.Error("No Reminders list")
        }
        else if(isError)
        {
            Result.Error("Error message")
        }
        else{
            Result.Success(ArrayList(reminders))
        }
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminders?.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        val reminder = reminders?.find {
            it.id == id
        }
        return if (reminder == null) {
            Result.Error("No Reminder")
        }
        else if(isError)
        {
            Result.Error("Error message")
        } else {
            Result.Success(reminder)
        }
    }

    override suspend fun deleteAllReminders() {
        reminders?.clear()
    }

    fun setError(errorValue: Boolean) {
        isError = errorValue
    }

}