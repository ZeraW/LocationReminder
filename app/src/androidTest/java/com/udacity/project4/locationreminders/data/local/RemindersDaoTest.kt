package com.udacity.project4.locationreminders.data.local

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import net.bytebuddy.pool.TypePool
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.hamcrest.Matchers.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {

    private lateinit var application: Application
    private lateinit var remindersDatabase: RemindersDatabase

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setupDatabase() {
        application = ApplicationProvider.getApplicationContext()
        remindersDatabase = Room.inMemoryDatabaseBuilder(application, RemindersDatabase::class.java)
            .allowMainThreadQueries().build()
    }

    @After
    fun closeDatabase() {
        remindersDatabase.close()
    }

    @Test
    fun insertIsEqualRetrieve() = runBlocking {
        //Given
        val givenReminder = ReminderDTO("Title", "Description", "Location", 37.0, 25.7)

        //When save given reminder to db
        remindersDatabase.reminderDao().saveReminder(givenReminder)

        //then reminderFromDB is givenReminder
        val reminderFromDB: ReminderDTO? = remindersDatabase.reminderDao().getReminderById(givenReminder.id)
        assertThat(reminderFromDB, `is`(givenReminder))
    }



    @Test
    fun insertReminder_DeleteReminders() = runBlockingTest {
        //Given
        val reminder = ReminderDTO("Title", "Description", "Location", 37.0, 25.7)

        //when saveReminder
        remindersDatabase.reminderDao().saveReminder(reminder)
        //then getReminders is not empty
        assertThat(remindersDatabase.reminderDao().getReminders().isEmpty(), `is`(false))
        //when deleteAllReminders
        remindersDatabase.reminderDao().deleteAllReminders()
        //then getReminders is empty
        assertThat(remindersDatabase.reminderDao().getReminders().isEmpty(), `is`(true))
    }


}