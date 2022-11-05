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
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {

    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var saveReminderViewModel: SaveReminderViewModel
    private lateinit var dataSource: FakeDataSource

    @Before
    fun setupViewModel() {
        stopKoin()
        dataSource =
            FakeDataSource()
        saveReminderViewModel = SaveReminderViewModel(
            ApplicationProvider.getApplicationContext(),
            dataSource)
    }

    @Test
    fun validateEnteredData_noTitle() {
        mainCoroutineRule.pauseDispatcher()
        assertThat(saveReminderViewModel.validateEnteredData(
            ReminderDataItem(
                "",
                "Santiago Bernabeu Stadium",
                "location",
                40.45317746686187,
                -3.68831224351148)
        ), Is.`is`(false))
        assertThat(saveReminderViewModel.showSnackBarInt.getOrAwaitValue(), Is.`is`(R.string.err_enter_title))
        mainCoroutineRule.resumeDispatcher()
    }

    @Test
    fun validateEnteredData_noLocation() {
        mainCoroutineRule.pauseDispatcher()
        assertThat(saveReminderViewModel.validateEnteredData(
            ReminderDataItem(
                "Santiago Bernabeu",
                "Santiago Bernabeu Stadium",
                "",
                40.45317746686187,
                -3.68831224351148)
        ), Is.`is`(false))
        assertThat(saveReminderViewModel.showSnackBarInt.getOrAwaitValue(), Is.`is`(R.string.err_select_location))
        mainCoroutineRule.resumeDispatcher()
    }

    @Test
    fun validateEnteredData_correctReminder() = runBlocking {
        mainCoroutineRule.pauseDispatcher()
        val reminder = ReminderDataItem(
            "Santiago Bernabeu",
            "Santiago Bernabeu Stadium",
            "location",
            40.45317746686187,
            -3.68831224351148,
            "id1")
        saveReminderViewModel.validateAndSaveReminder(reminder)
        assertThat(saveReminderViewModel.validateEnteredData(reminder), Is.`is`(true))
        assertThat(saveReminderViewModel.showLoading.getOrAwaitValue(), Is.`is`(true))
        mainCoroutineRule.resumeDispatcher()

        val resultGetReminder = dataSource.getReminder("id1")
        if(resultGetReminder is Result.Success<ReminderDTO>){
            assertThat(resultGetReminder.data.title, Is.`is`(reminder.title))
            assertThat(resultGetReminder.data.description, Is.`is`(reminder.description))
            assertThat(resultGetReminder.data.location, Is.`is`(reminder.location))
            assertThat(resultGetReminder.data.latitude, Is.`is`(reminder.latitude))
            assertThat(resultGetReminder.data.longitude, Is.`is`(reminder.longitude))
        }
        assertThat(saveReminderViewModel.showToast.getOrAwaitValue(), Is.`is`("Reminder Saved !"))
    }
}