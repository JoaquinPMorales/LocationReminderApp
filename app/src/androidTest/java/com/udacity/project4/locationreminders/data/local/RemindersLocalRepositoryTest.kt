package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is
import org.junit.*
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository RemindersLocalRepository.kt
@MediumTest
class RemindersLocalRepositoryTest {

    //LocalDataSource
    private lateinit var localDataSource: ReminderDataSource
    //Database
    private lateinit var database: RemindersDatabase

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    /**
     * Create DB in memory and our LocalDataSource
     *
     */
    @Before
    fun setup() {
        // Using an in-memory database for testing, because it doesn't survive killing the process.
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).allowMainThreadQueries()
         .build()

        localDataSource =
            RemindersLocalRepository(
                database.reminderDao(),
                Dispatchers.Main
            )
    }

    /**
     * Close DB in memory.
     *
     */
    @After
    fun cleanUp() {
        database.close()
    }

    /**
     * Test to get a Reminder that exist.
     *
     */
    @Test
    fun saveReminder_getReminder() = runBlocking {
        // GIVEN - A new reminder saved in the database.
        val reminder = ReminderDTO("Santiago Bernabeu",
            "Santiago Bernabeu Stadium",
            "location",
            40.45317746686187,
            -3.68831224351148,
            "1")
        database.reminderDao().saveReminder(reminder)

        // WHEN  - Reminder retrieved by ID.
        val resultReminder = localDataSource.getReminder(reminder.id)

        // THEN - Same Reminder is returned.
        if(resultReminder is Result.Success<ReminderDTO>)
        {
            assertThat(resultReminder.data.title, `is`(reminder.title))
            assertThat(resultReminder.data.description, `is`(reminder.description))
            assertThat(resultReminder.data.location, `is`(reminder.location))
            assertThat(resultReminder.data.longitude, `is`(reminder.longitude))
            assertThat(resultReminder.data.latitude, `is`(reminder.latitude))
            assertThat(resultReminder.data.id, `is`(reminder.id))
        }
    }

    /**
     * Test to get a Reminder that not exist.
     *
     */
    @Test
    fun retrievesReminder_errorNotExist() = runBlocking {
        //Get a reminder that not exist
        val existReminder = localDataSource.getReminder("1")
        //Check we get an error because Reminder not exist
        existReminder as Result.Error
        Assert.assertThat(existReminder.message, Is.`is`("Reminder not found!"))
    }
}