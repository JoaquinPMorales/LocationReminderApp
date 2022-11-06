package com.udacity.project4

import android.app.Application
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.RootMatchers
import androidx.test.espresso.matcher.RootMatchers.withDecorView
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.google.android.material.internal.ContextUtils
import com.google.android.material.internal.ContextUtils.getActivity
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.monitorActivity
import com.udacity.project4.utils.EspressoIdlingResource
import kotlinx.coroutines.runBlocking
import org.hamcrest.core.IsNot
import org.hamcrest.core.IsNot.not
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get

@RunWith(AndroidJUnit4::class)
@LargeTest
//END TO END test to black box test the app
class RemindersActivityTest : AutoCloseKoinTest() {// Extended Koin Test - embed autoclose @after method to close Koin after every test

    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application

    // An Idling Resource that waits for Data Binding to have no pending bindings.
    private val dataBindingIdlingResource = DataBindingIdlingResource()

    /**
     * As we use Koin as a Service Locator Library to develop our code, we'll also use Koin to test our code.
     * at this step we will initialize Koin related code to be able to use it in out testing.
     */
    @Before
    fun init() {
        stopKoin()//stop the original app koin
        appContext = getApplicationContext()
        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single {
                SaveReminderViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single { RemindersLocalRepository(get()) as ReminderDataSource }
            single { LocalDB.createRemindersDao(appContext) }
        }
        //declare a new koin module
        startKoin {
            modules(listOf(myModule))
        }
        //Get our real repository
        repository = get()

        //clear the data to start fresh
        runBlocking {
            repository.deleteAllReminders()
        }
    }

    @Before
    fun registerIdlingResource() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().register(dataBindingIdlingResource)
    }

    /**
     * Unregister your idling resource so it can be garbage collected and does not leak any memory.
     */
    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)
    }

    @Test
    fun createReminder_successfullyCreated(){
        // 1. Start RemindersActivity
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        // 2. Add a reminder by clicking on the FAB and saving it.
        Espresso.onView(ViewMatchers.withId(R.id.addReminderFAB)).perform(ViewActions.click())

        Espresso.onView(ViewMatchers.withId(R.id.reminderTitle)).perform(ViewActions.replaceText("SANTIAGO BERNABEU"))
        Espresso.onView(ViewMatchers.withId(R.id.reminderDescription)).perform(ViewActions.replaceText("SANTIAGO BERNABEU STADIUM"))

        // 3. Select location
        Espresso.onView(ViewMatchers.withId(R.id.selectLocation)).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withId(R.id.map)).perform(ViewActions.click())
        Thread.sleep(1000)

        // 4. Save location
        Espresso.onView(withId(R.id.saveBtn)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        Espresso.onView(withId(R.id.saveBtn)).perform(ViewActions.click())

        // 5. Save Reminder
        Espresso.onView(ViewMatchers.withId(R.id.saveReminder)).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withText(R.string.geofence_entered))
            .inRoot(RootMatchers.withDecorView(IsNot.not((ContextUtils.getActivity(appContext)?.window?.decorView)))).check(
                ViewAssertions.matches(ViewMatchers.isDisplayed()))
        Thread.sleep(2000)

        // 6. Verify it was added.
        Espresso.onView(ViewMatchers.withText("SANTIAGO BERNABEU")).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        Espresso.onView(ViewMatchers.withText("SANTIAGO BERNABEU STADIUM")).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        Espresso.onView(withId(R.id.noDataTextView))
            .check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.GONE)))

        // 7. Make sure the activity is closed.
        activityScenario.close()
    }

    @Test
    fun navigateToRemindersDetail(){
        // 1. Before start activity, added a reminder to the repo
        val reminder = ReminderDTO("Santiago Bernabeu",
            "Santiago Bernabeu Stadium",
            "location",
            40.45317746686187,
            -3.68831224351148,
            "1")
        runBlocking {
            repository.saveReminder(reminder)
        }

        // 2. Start RemindersActivity
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        // 3. Check reminder added appears in reminders list
        Espresso.onView(ViewMatchers.withText(reminder.title)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        Espresso.onView(ViewMatchers.withText(reminder.description)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        Espresso.onView(ViewMatchers.withText(reminder.location)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))

        // 4. Go to details fragment
        onView(withId(R.id.reminderssRecyclerView)).check(matches(isDisplayed()))
        Espresso.onView(ViewMatchers.withId(R.id.reminderssRecyclerView)).perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(0, ViewActions.click()))
        Thread.sleep(1000)

        // 5. Check details fragment info
        Espresso.onView(ViewMatchers.withText(reminder.title)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        Espresso.onView(ViewMatchers.withText(reminder.description)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        Espresso.onView(ViewMatchers.withText(reminder.location)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))

        // 6. Make sure the activity is closed.
        activityScenario.close()
    }

    @Test
    fun createWrongReminder_noLocation() = runBlocking {
        // 1. Start RemindersActivity
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        // 2. Add a reminder by clicking on the FAB and saving it.
        Espresso.onView(ViewMatchers.withId(R.id.addReminderFAB)).perform(ViewActions.click())

        Espresso.onView(ViewMatchers.withId(R.id.reminderTitle)).perform(ViewActions.replaceText("SANTIAGO BERNABEU"))
        Espresso.onView(ViewMatchers.withId(R.id.reminderDescription)).perform(ViewActions.replaceText("SANTIAGO BERNABEU STADIUM"))

        // 3. Try to save Reminder without location
        Espresso.onView(ViewMatchers.withId(R.id.saveReminder)).perform(ViewActions.click())

        // 4. Check you cannot save reminder without location
        onView(withId(com.google.android.material.R.id.snackbar_text)).check(matches(withText(R.string.err_select_location)))

        // 5. Make sure the activity is closed.
        activityScenario.close()
    }

    @Test
    fun createWrongReminder_noTitle() = runBlocking {
        // 1. Start RemindersActivity
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        // 2. Add a reminder by clicking on the FAB and saving it.
        Espresso.onView(ViewMatchers.withId(R.id.addReminderFAB)).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withId(R.id.reminderDescription)).perform(ViewActions.replaceText("SANTIAGO BERNABEU STADIUM"))

        // 3. Try to save Reminder without title
        Espresso.onView(ViewMatchers.withId(R.id.saveReminder)).perform(ViewActions.click())

        // 4. Check you cannot save reminder without title
        onView(withId(com.google.android.material.R.id.snackbar_text)).check(matches(withText(R.string.err_enter_title)))

        // 5. Make sure the activity is closed.
        activityScenario.close()
    }

}
