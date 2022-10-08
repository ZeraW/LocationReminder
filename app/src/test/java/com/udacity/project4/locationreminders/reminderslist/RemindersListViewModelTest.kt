package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.notNullValue
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config

@Config(sdk = [Build.VERSION_CODES.P])
@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {

    private lateinit var application: Application
    private lateinit var dataSource: FakeDataSource
    private lateinit var remindersListViewModel: RemindersListViewModel

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setupViewModel() {
        stopKoin()
        application = ApplicationProvider.getApplicationContext()
        dataSource = FakeDataSource()
        remindersListViewModel = RemindersListViewModel(application, dataSource)
    }


    @Test
    fun getReminder_invalidateShowNoData_Empty()  = mainCoroutineRule.runBlockingTest  {
        //Given nothing

        // When
        remindersListViewModel.loadReminders()
        // Then
        assertThat(remindersListViewModel.showNoData.getOrAwaitValue(), `is`(true))
    }


    @Test
    fun getReminder_invalidateShowNoData_resultNotEmpty() = mainCoroutineRule.runBlockingTest  {
        //Given
        val reminder = ReminderDTO("Title", "Description", "Location", 37.0, 25.7, "id")
        dataSource.saveReminder(reminder)

        //when
        remindersListViewModel.loadReminders()

        //then
        assertThat(remindersListViewModel.showNoData.getOrAwaitValue(), `is`(false))
    }


}