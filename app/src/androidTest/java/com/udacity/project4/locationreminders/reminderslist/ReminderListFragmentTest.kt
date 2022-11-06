package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import android.os.Bundle
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.utils.EspressoIdlingResource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.mockito.Mockito.mock
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.util.monitorFragment
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.core.IsNot
import org.koin.test.get
import org.mockito.Mockito

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class ReminderListFragmentTest: AutoCloseKoinTest() {

//    TODO: add testing for the error messages.

    private lateinit var repository: ReminderDataSource
    private lateinit var applicationContext: Application

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    // An idling resource that waits for Data Binding to have no pending bindings.
    private val dataBindingIdlingResource = DataBindingIdlingResource()

    @Before
    fun initRepository() {
        stopKoin()
        applicationContext = getApplicationContext()

        val myModule = module {
            //Declare a ViewModel - be later inject into Fragment with dedicated injector using by viewModel()
            viewModel {
                RemindersListViewModel(
                    applicationContext,
                    get() as ReminderDataSource
                )
            }
            //Declare singleton definitions to be later injected using by inject()
            single { RemindersLocalRepository(get()) as ReminderDataSource }
            single { LocalDB.createRemindersDao(applicationContext) }
        }

        startKoin {
            modules(listOf(myModule))
        }

        //Get our real repository
        repository = get()

        runBlocking {
            repository.deleteAllReminders()
        }
    }

    /**
     * Idling resources tell Espresso that the app is idle or busy. This is needed when operations
     * are not scheduled in the main Looper (for example when executed on a different thread).
     */
    @Before
    fun registerIdlingResource() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().register(dataBindingIdlingResource)
    }

    /**
     * Unregister your Idling Resource so it can be garbage collected and does not leak any memory.
     */
    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)
    }

    @Test
    fun clickAddReminderBtn_navigateToSaveFragment() {
        // GIVEN - On the Reminder List screen
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        val navController = mock(NavController::class.java)
        dataBindingIdlingResource.monitorFragment(fragmentScenario = scenario)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        // WHEN - Click on add reminder button
        onView(withId(R.id.addReminderFAB)).perform(click())

        // THEN - Verify that we navigate to the save reminder screen
        Mockito.verify(navController).navigate(ReminderListFragmentDirections.toSaveReminder())
    }

    @Test
    fun reminderList_shownListAndNoDataTextViewNotShown(): Unit = runBlocking{
        val reminder = ReminderDTO("Santiago Bernabeu",
            "Santiago Bernabeu Stadium",
            "location",
            40.45317746686187,
            -3.68831224351148,
            "1")
        val reminder2 = ReminderDTO("Budapest's Parliament",
            "Budapest's Hungary Parliament",
            "location",
            47.5076813529168,
            19.04631526700402,
            "2")
        val reminder3 = ReminderDTO("St Michael's Mount",
            "St Michael's Mount beautiful place",
            "location",
            48.85305821486229,
            -1.35394093963818,
                "3")
        repository.saveReminder(reminder)
        repository.saveReminder(reminder2)
        repository.saveReminder(reminder3)
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        dataBindingIdlingResource.monitorFragment(fragmentScenario = scenario)

        onView(withText(reminder.title)).check(matches(isDisplayed()))
        onView(withText(reminder2.title)).check(matches(isDisplayed()))
        onView(withText(reminder3.title)).check(matches(isDisplayed()))
        onView(withId(R.id.noDataTextView)).check(ViewAssertions.matches(IsNot.not(isDisplayed())))
    }

    @Test
    fun noRemindersData_shownNoDataTextView() = runBlockingTest {
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        dataBindingIdlingResource.monitorFragment(fragmentScenario = scenario)
        onView(withId(R.id.noDataTextView)).check(matches(isDisplayed()))
    }

}

