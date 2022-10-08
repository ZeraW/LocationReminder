package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import android.os.Bundle
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class ReminderListFragmentTest {

    private lateinit var application: Application
    private lateinit var repo: ReminderDataSource

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()


    @Before
    fun initRepository() {
        stopKoin()
        application = getApplicationContext()
        val kModule = module {
            viewModel {
                RemindersListViewModel(
                    application,
                    get() as ReminderDataSource
                )
            }
            single<ReminderDataSource> { RemindersLocalRepository(get()) }
            single { LocalDB.createRemindersDao(application) }
        }

        startKoin {
            modules(listOf(kModule))
        }

        repo = GlobalContext.get().koin.get()

        // Given always
        runBlocking {
            repo.saveReminder(ReminderDTO("reminder 1", "Description", "Giza", 12.34, 56.78))
            repo.saveReminder(ReminderDTO("reminder 2", "Description", "Cairo", 9.10, 11.12))
            repo.saveReminder(ReminderDTO("reminder 3", "Description", "Google", 9.10, 11.12))
        }

    }

    @After
    fun clear() = runBlocking {
        repo.deleteAllReminders()
    }

    @Test
    fun fabClick_navToSaveReminderFragments() {
        // Given
        val navController = mock(NavController::class.java)
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        // When
        onView(withId(R.id.addReminderFAB)).perform(click())

        // Then
        verify(navController).navigate(ReminderListFragmentDirections.toSaveReminder())
    }

    @Test
    fun displayedReminderOnUI() {
        // When
        launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)

        // Then
        onView(withText("reminder 1")).check(matches(isDisplayed()))
        onView(withText("Cairo")).check(matches(isDisplayed()))
    }

    @Test
    fun clearDB_showNoData() {
        //When
        runBlocking { repo.deleteAllReminders() }
        launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)

        // Then
        onView(withId(R.id.noDataTextView)).check(matches(isDisplayed()))
    }
}