package com.udacity.project4.locationreminders.reminderslist

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.MatcherAssert
import org.hamcrest.core.Is
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {

    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var remindersListViewModel: RemindersListViewModel
    private lateinit var dataSource: FakeDataSource

    /**
     * Stop koin and create our tested viewModel with already inserted values.
     *
     */
    @Before
    fun setupViewModel() {
        //Stop koin
        stopKoin()
        //Create our dataSource with the fake one and already added reminders
        dataSource =
            FakeDataSource(mutableListOf(ReminderDTO("Santiago Bernabeu",
                                                "Santiago Bernabeu Stadium",
                                                  "location",
                                                  40.45317746686187,
                                                 -3.68831224351148),
                                        ReminderDTO("Budapest's Parliament",
                                                "Budapest's Parliament",
                                                "location",
                                                47.5076813529168,
                                               19.04631526700402),
                                        ReminderDTO("St Michael's Mount",
                                               "St Michael's Mount",
                                                  "location",
                                                  48.85305821486229,
                                                 -1.35394093963818)))
        //Create our tested viewModel with the fakeDataSource
        remindersListViewModel = RemindersListViewModel(ApplicationProvider.getApplicationContext(),
                                                        dataSource)
    }

    /**
     * Test retrieve reminders from a non empty list.
     *
     */
    @Test
    fun loadReminders_nonEmptyList() {
        //Load reminders that calls our FakeDataSource to retrieve the list
        remindersListViewModel.loadReminders()
        val showNoData = remindersListViewModel.showNoData.getOrAwaitValue()
        //Check noData info does not appear
        MatcherAssert.assertThat(showNoData, Is.`is`(false))
    }

    /**
     * Test retrieve reminders from an empty list.
     *
     */
    @Test
    fun loadReminders_emptyList() = runBlocking{
        //Clear all the existing reminders
        dataSource.deleteAllReminders()
        //Retrieve reminder list
        remindersListViewModel.loadReminders()
        //Get showNotData LiveData value
        val showNoData = remindersListViewModel.showNoData.getOrAwaitValue()
        //ShowNoData appears, list is empty
        MatcherAssert.assertThat(showNoData, Is.`is`(true))
    }

    /**
     * Force an error when we try to retrieve reminders.
     *
     */
    @Test
    fun loadReminders_showErrorMessage() {
        //Force error with created method
        dataSource.setError(true)
        //Try to get Reminders list
        remindersListViewModel.loadReminders()
        //Get showNotData LiveData
        val showNoData = remindersListViewModel.showNoData.getOrAwaitValue()
        //Get snackBar liveData
        val errorMsgSnackBar = remindersListViewModel.showSnackBar.getOrAwaitValue()

        //Check showNoData appears
        assertThat(showNoData, Is.`is`(true))
        //Check SnackBar shows an error
        assertThat(errorMsgSnackBar, Is.`is`("Error message"))
    }

    /**
     * Check showLoading message appears.
     *
     */
    @Test
    fun loadReminders_showLoadingMessage() {
        //Pause dispatcher to get showLoading value
        mainCoroutineRule.pauseDispatcher()
        //Try to get reminders list
        remindersListViewModel.loadReminders()
        //Check if showLoading appears
        assertThat(remindersListViewModel.showLoading.getOrAwaitValue(), Is.`is`(true))
        //resume dispatcher
        mainCoroutineRule.resumeDispatcher()
        //Check showLoading disappear
        assertThat(remindersListViewModel.showLoading.getOrAwaitValue(), Is.`is`(false))
    }

}