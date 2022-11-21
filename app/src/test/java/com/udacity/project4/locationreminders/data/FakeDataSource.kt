package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource(var reminders: MutableList<ReminderDTO> = mutableListOf()) : ReminderDataSource {

    //Create a variable just to force errors and being able to reproduce it.
    private var isError = false

    /**
     * getReminders is the faked method.
     *
     */
    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        return if(isError)//just to force an error getting reminder list
        {
            Result.Error("Error message")
        }
        else{
            //Return reminder list
            Result.Success(ArrayList(reminders))
        }
    }

    /**
     * saveReminder is the faked method.
     *
     */
    override suspend fun saveReminder(reminder: ReminderDTO) {
        //Add reminder to list
        reminders.add(reminder)
    }

    /**
     * getReminder is the faked method.
     *
     */
    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        //Check if id exists in the reminder list
        val reminder = reminders?.find {
            it.id == id
        }
        return if(isError)
        {
            //Forced error trying to retrieve a reminder
            Result.Error("Error message")
        } else {
            if (reminder == null) {
                //ID does not exist
                Result.Error("No Reminder")
            }
            else
                //ID exists so a Success is returned with the reminder info
                Result.Success(reminder)
        }
    }

    /**
     * deleteAllReminders is the faked method.
     *
     */
    override suspend fun deleteAllReminders() {
        //Clear all the reminder list
        reminders.clear()
    }

    /**
     * method to force an error.
     *
     */
    fun setError(errorValue: Boolean) {
        isError = errorValue
    }

}