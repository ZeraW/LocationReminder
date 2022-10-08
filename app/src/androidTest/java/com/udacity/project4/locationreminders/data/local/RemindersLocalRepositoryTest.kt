package com.udacity.project4.locationreminders.data.local

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {

    private lateinit var application: Application
    private lateinit var remindersDatabase: RemindersDatabase
    private lateinit var remindersLocalRepository: RemindersLocalRepository

    @Before
    fun setupRepository() {
        application = ApplicationProvider.getApplicationContext()
        remindersDatabase = Room.inMemoryDatabaseBuilder(application, RemindersDatabase::class.java)
            .allowMainThreadQueries().build()

        remindersLocalRepository =
            RemindersLocalRepository(remindersDatabase.reminderDao(), Dispatchers.Main)
    }

    @After
    fun closeDatabase() {
        remindersDatabase.close()
    }


    @Test
    fun insertReminder_Success() = runBlocking {
        //Given
        val reminder = ReminderDTO("Title", "Description", "Location", 37.0, 25.7)

        //When
        remindersLocalRepository.saveReminder(reminder)

        //Then
        val result = remindersLocalRepository.getReminders() as Result.Success
        assertThat(result.data.isNotEmpty(), `is`(true))
        assertThat(result.data.size, `is`(1))
    }


    @Test
    fun deleteReminder_Success() = runBlocking {
        //Given
        val reminder = ReminderDTO("Title", "Description", "Location", 37.0, 25.7)

        //when
        remindersLocalRepository.saveReminder(reminder)

        //Then
        val result = remindersLocalRepository.getReminders() as Result.Success
        assertThat(result.data.isNotEmpty(), `is`(true))

        //when
        remindersLocalRepository.deleteAllReminders()

        //Then
        val result2 = remindersLocalRepository.getReminders() as Result.Success
        assertThat(result2.data.isNotEmpty(), `is`(false))

    }


}