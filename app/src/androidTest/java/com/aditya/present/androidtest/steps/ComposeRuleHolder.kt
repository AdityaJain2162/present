package com.aditya.present.androidtest.steps

import android.util.Log
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.aditya.present.MainActivity
import com.aditya.present.data.PresentDatabase
import io.cucumber.java.After
import io.cucumber.java.Before
import io.cucumber.junit.WithJunitRule
import kotlinx.coroutines.CoroutineExceptionHandler
import java.util.ServiceLoader

class ComposeRuleHolder {

    @field:WithJunitRule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setUp() {
        Thread.currentThread().contextClassLoader = javaClass.classLoader
        fixCoroutineExceptionHandlers()
        clearDatabase()
        // Don't call waitForIdle() here — @WithJunitRule handles setup.
        // Step definitions will call waitForIdle() as needed.
    }

    @After
    fun tearDown() {
        // Activity is managed by the rule
    }

    private fun fixCoroutineExceptionHandlers() {
        try {
            val testCl = javaClass.classLoader!!
            val testHandlers = ServiceLoader.load(CoroutineExceptionHandler::class.java, testCl).toList()

            val implKtClass = Class.forName("kotlinx.coroutines.internal.CoroutineExceptionHandlerImplKt")
            val field = implKtClass.getDeclaredField("platformExceptionHandlers")
            field.isAccessible = true

            val currentValue = field.get(null) as? Collection<*>
            val combined = mutableListOf<Any>()
            @Suppress("UNCHECKED_CAST")
            currentValue?.let { combined.addAll(it as Collection<Any>) }
            combined.addAll(testHandlers)

            field.set(null, combined)
        } catch (e: Exception) {
            Log.e("PresentCucumber", "fixCoroutineExceptionHandlers failed", e)
        }
    }

    private fun clearDatabase() {
        try {
            val context = InstrumentationRegistry.getInstrumentation().targetContext
            val db = Room.databaseBuilder(context, PresentDatabase::class.java, "present.db")
                .fallbackToDestructiveMigration()
                .allowMainThreadQueries()
                .build()
            db.openHelper.writableDatabase.execSQL("DELETE FROM attendance")
            db.openHelper.writableDatabase.execSQL("DELETE FROM class_slots")
            db.openHelper.writableDatabase.execSQL("DELETE FROM subjects")
            db.openHelper.writableDatabase.execSQL("DELETE FROM academic_sessions")
            db.close()
        } catch (e: Exception) {
            Log.e("PresentCucumber", "clearDatabase failed", e)
        }
    }
}
