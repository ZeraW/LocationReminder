package com.udacity.project4.locationreminders.savereminder

import android.app.Application
import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.getOrAwaitValue
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config

@Config(sdk = [Build.VERSION_CODES.P])
@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class SaveReminderViewModelTest {
    private lateinit var application: Application

    private lateinit var dataSource: FakeDataSource

    private lateinit var saveReminderViewModel: SaveReminderViewModel

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()


    @Before
    fun setupViewModel() {
        stopKoin()
        application = ApplicationProvider.getApplicationContext()
        dataSource = FakeDataSource()
        saveReminderViewModel = SaveReminderViewModel(application, dataSource)
    }


    @Test
    fun addNewReminder_saveReminder() {
        //Given
        val reminder = ReminderDataItem("Title", "Description", "Location", 37.0, 25.7)
        // When
        saveReminderViewModel.saveReminder(reminder)
        // Then the new task event is triggered
        val value = saveReminderViewModel.showToast.getOrAwaitValue()
        assertThat(value,  `is`("Reminder Saved !"))
    }

    @Test
    fun addNewReminder_validateReminder() {
        //Given
        val reminder = ReminderDataItem(null, "Description", "Location", 37.0, 25.7)
        // When
        val value = saveReminderViewModel.validateReminder(reminder)
        // Then
        assertThat(value,  `is`(false))
    }

    @Test
    fun addNewReminder_isLoadingTriggered() = mainCoroutineRule.runBlockingTest {
        //Given
        val reminder = ReminderDataItem("Title", "Description", "Location", 37.0, 25.7)

        //When
        mainCoroutineRule.pauseDispatcher()
        saveReminderViewModel.validateReminder(reminder)
        saveReminderViewModel.saveReminder(reminder)

        //Then
        assertThat(saveReminderViewModel.showLoading.getOrAwaitValue(), `is`(true))
        //When
        mainCoroutineRule.resumeDispatcher()
        //Then
        assertThat(saveReminderViewModel.showLoading.getOrAwaitValue(), `is`(false))
    }

}