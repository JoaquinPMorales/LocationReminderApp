package com.udacity.project4.locationreminders.savereminder

import android.provider.Settings.Global.getString
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.R
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.getOrAwaitValue
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.MatcherAssert
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Class to test our SaveReminderViewModel
class SaveReminderViewModelTest {

    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var saveReminderViewModel: SaveReminderViewModel
    private lateinit var dataSource: FakeDataSource

    /**
     * Stop koin and create our tested viewModel.
     *
     */
    @Before
    fun setupViewModel() {
        //stop Koin
        stopKoin()
        //Create dataSource with the fake one created for us
        dataSource = FakeDataSource()
        //Instantiate saveReminderViewModel with the fake data source
        saveReminderViewModel = SaveReminderViewModel(
            ApplicationProvider.getApplicationContext(),
            dataSource)
    }

    /**
     * validateEnteredData is the tested method.
     * Create a reminder without title and should appear a snackBar asking for it
     */
    @Test
    fun validateEnteredData_noTitle() {
        //Pause dispatcher just to check whether snackBar appear
        mainCoroutineRule.pauseDispatcher()
        //Create Reminder, without title, and call validateEnteredData just to test if check correctly reminder has no title
        assertThat(saveReminderViewModel.validateEnteredData(
            ReminderDataItem(
                "",
                "Santiago Bernabeu Stadium",
                "location",
                40.45317746686187,
                -3.68831224351148)
        ), Is.`is`(false))
        //Verify snackBar with correct text appears
        assertThat(saveReminderViewModel.showSnackBarInt.getOrAwaitValue(), Is.`is`(R.string.err_enter_title))
        mainCoroutineRule.resumeDispatcher()
    }

    /**
     * validateEnteredData is the tested method.
     * Create a reminder without location and should appear a snackBar asking for it
     */
    @Test
    fun validateEnteredData_noLocation() {
        //Pause dispatcher just to check whether snackBar appear
        mainCoroutineRule.pauseDispatcher()
        //Create Reminder, without location, and call validateEnteredData just to test if check correctly reminder has no location
        assertThat(saveReminderViewModel.validateEnteredData(
            ReminderDataItem(
                "Santiago Bernabeu",
                "Santiago Bernabeu Stadium",
                "",
                40.45317746686187,
                -3.68831224351148)
        ), Is.`is`(false))
        //Verify snackBar with correct text appears
        assertThat(saveReminderViewModel.showSnackBarInt.getOrAwaitValue(), Is.`is`(R.string.err_select_location))
        mainCoroutineRule.resumeDispatcher()
    }

    /**
     * validateEnteredData is the tested method.
     * Create a valid reminder,  should appear a snackBar and get back to list reminder
     * After that try to get Reminder with its ID and check all of its data.
     */
    @Test
    fun validateEnteredData_correctReminder() = runBlocking {
        //Pause dispatcher just to check whether snackBar appear
        mainCoroutineRule.pauseDispatcher()
        //Valid reminder
        val reminder = ReminderDataItem(
            "Santiago Bernabeu",
            "Santiago Bernabeu Stadium",
            "location",
            40.45317746686187,
            -3.68831224351148,
            "id1")
        //Save reminder if it's valid
        saveReminderViewModel.validateAndSaveReminder(reminder)
        //Check if it's valid
        assertThat(saveReminderViewModel.validateEnteredData(reminder), Is.`is`(true))
        //Check showLoading appears
        assertThat(saveReminderViewModel.showLoading.getOrAwaitValue(), Is.`is`(true))
        mainCoroutineRule.resumeDispatcher()

        //Get reminder with id1
        val resultGetReminder = dataSource.getReminder("id1")
        //Check whether we get a success and it has same data we entered previously
        if(resultGetReminder is Result.Success<ReminderDTO>){
            assertThat(resultGetReminder.data.title, Is.`is`(reminder.title))
            assertThat(resultGetReminder.data.description, Is.`is`(reminder.description))
            assertThat(resultGetReminder.data.location, Is.`is`(reminder.location))
            assertThat(resultGetReminder.data.latitude, Is.`is`(reminder.latitude))
            assertThat(resultGetReminder.data.longitude, Is.`is`(reminder.longitude))
        }
        //Verify snackBar for Reminder saved appears
        assertThat(saveReminderViewModel.showToast.getOrAwaitValue(), Is.`is`("Reminder Saved !"))
        //Verify go back to Reminder list
        assertEquals(saveReminderViewModel.navigationCommand.getOrAwaitValue(), NavigationCommand.Back)
    }
}