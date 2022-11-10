package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.ApplicationProvider.getApplicationContext
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
import org.hamcrest.Matchers
import org.junit.After
import org.junit.Test

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    //Our database
    private lateinit var database: RemindersDatabase

    /**
     * Create DB in memory.
     *
     */
    @Before
    fun initDb() {
        // Using an in-memory database so that the information stored here disappears when the
        // process is killed.
        database = Room.inMemoryDatabaseBuilder(
            getApplicationContext(),
            RemindersDatabase::class.java
        ).build()
    }

    /**
     * Close DB created
     *
     */
    @After
    fun closeDb() = database.close()

    /**
     * Test to save a reminder and after that retrieve it correctly
     *
     */
    @Test
    fun insertReminderAndGetById() = runBlockingTest {
        //Valid reminder created
        val reminder = ReminderDTO("Santiago Bernabeu",
            "Santiago Bernabeu Stadium",
            "location",
            40.45317746686187,
            -3.68831224351148,
            "1")
        //Save reminder in our database
        database.reminderDao().saveReminder(reminder)

        // WHEN - Get the reminder by id from the database.
        val reminderLoaded = database.reminderDao().getReminderById(reminder.id)

        // THEN - The loaded data contains the expected values.
        assertThat<ReminderDTO>(reminderLoaded as ReminderDTO, notNullValue())
        assertThat(reminderLoaded.id, `is`(reminder.id))
        assertThat(reminderLoaded.title, `is`(reminder.title))
        assertThat(reminderLoaded.description, `is`(reminder.description))
        assertThat(reminderLoaded.location, `is`(reminder.location))
        assertThat(reminderLoaded.latitude, `is`(reminder.latitude))
        assertThat(reminderLoaded.longitude, `is`(reminder.longitude))
    }

    /**
     * Test to save a reminder and after that update it with another data. After update it
     * we retrieve it and test it has the last values.
     *
     */
    @Test
    fun updateReminderAndGetById() = runBlockingTest {
        // Original reminder created
        val originalReminder = ReminderDTO("Santiago Bernabeu",
            "Santiago Bernabeu Stadium",
            "location",
            40.45317746686187,
            -3.68831224351148,
            "1")
        database.reminderDao().saveReminder(originalReminder)

        // New reminder created with same ID to update it in DB
        val updatedReminder = ReminderDTO("Budapest's Parliament",
                                            "Budapest's Parliament",
                                            "location",
                                            47.5076813529168,
                                            19.04631526700402,
                                            "1")
        //Save reminder(same id, so it's an update) into database
        database.reminderDao().saveReminder(updatedReminder)

        // THEN - The loaded reminder contains the expected values
        val reminderLoaded = database.reminderDao().getReminderById(originalReminder.id)
        assertThat<ReminderDTO>(reminderLoaded as ReminderDTO, notNullValue())
        assertThat(reminderLoaded.id, `is`(updatedReminder.id))
        assertThat(reminderLoaded.title, `is`(updatedReminder.title))
        assertThat(reminderLoaded.description, `is`(updatedReminder.description))
        assertThat(reminderLoaded.location, `is`(updatedReminder.location))
        assertThat(reminderLoaded.latitude, `is`(updatedReminder.latitude))
        assertThat(reminderLoaded.longitude, `is`(updatedReminder.longitude))
    }

    /**
     * Test to save two reminders and after that clear database correctly
     *
     */
    @Test
    fun insertRemindersAndDeleteAll() = runBlockingTest {
        //two valid reminders
        val reminder = ReminderDTO("Santiago Bernabeu",
            "Santiago Bernabeu Stadium",
            "location",
            40.45317746686187,
            -3.68831224351148,
            "1")
        val reminder2 = ReminderDTO("Budapest's Parliament",
            "Budapest's Parliament",
            "location",
            47.5076813529168,
            19.04631526700402,
            "2")
        //Save them into DB
        database.reminderDao().saveReminder(reminder)
        database.reminderDao().saveReminder(reminder2)

        //Check database size
        assertThat(database.reminderDao().getReminders().size, `is`(2))

        //Clear DB
        database.reminderDao().deleteAllReminders()
        //Check DB is empty
        assertThat(database.reminderDao().getReminders().size, `is`(0))
    }

    /**
     * Test to retrieve a reminder with an nonexistent ID
     *
     */
    @Test
    fun getNotAddedReminderById() = runBlockingTest {
        //Retrieve a reminder that not exist in DB
        val existReminder = database.reminderDao().getReminderById("1")
        //Check we get a null reminder, it does not exist in DB
        assertThat<ReminderDTO>(existReminder, `is`(Matchers.nullValue()))
    }

}