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

    @Before
    fun setupViewModel() {
        stopKoin()
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
        remindersListViewModel = RemindersListViewModel(ApplicationProvider.getApplicationContext(),
                                                        dataSource)
    }

    @Test
    fun loadReminders_nonEmptyList() {
        remindersListViewModel.loadReminders()
        val showNoData = remindersListViewModel.showNoData.getOrAwaitValue()
        MatcherAssert.assertThat(showNoData, Is.`is`(false))
    }

    @Test
    fun loadReminders_emptyList() = runBlocking{
        dataSource.deleteAllReminders()
        remindersListViewModel.loadReminders()
        val showNoData = remindersListViewModel.showNoData.getOrAwaitValue()
        MatcherAssert.assertThat(showNoData, Is.`is`(true))
    }

    @Test
    fun loadReminders_showErrorMessage() {
        dataSource.setError(true)
        remindersListViewModel.loadReminders()
        val showNoData = remindersListViewModel.showNoData.getOrAwaitValue()
        val errorMsgSnackBar = remindersListViewModel.showSnackBar.getOrAwaitValue()

        assertThat(showNoData, Is.`is`(true))
        assertThat(errorMsgSnackBar, Is.`is`("Error message"))
    }

    @Test
    fun loadReminders_showLoadingMessage() {
        mainCoroutineRule.pauseDispatcher()
        remindersListViewModel.loadReminders()
        assertThat(remindersListViewModel.showLoading.getOrAwaitValue(), Is.`is`(true))
        mainCoroutineRule.resumeDispatcher()
        assertThat(remindersListViewModel.showLoading.getOrAwaitValue(), Is.`is`(false))
    }

}