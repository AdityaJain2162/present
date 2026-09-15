package com.aditya.present.androidtest.steps

import android.util.Log
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.room.Room
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.platform.app.InstrumentationRegistry
import com.aditya.present.MainActivity
import com.aditya.present.data.PresentDatabase
import io.cucumber.java.After
import io.cucumber.java.Before
import kotlinx.coroutines.CoroutineExceptionHandler
import org.junit.runner.Description
import org.junit.runners.model.Statement
import java.util.ServiceLoader

class ComposeRuleHolder {

    private var _composeRule: AndroidComposeTestRule<ActivityScenarioRule<MainActivity>, MainActivity>? = null

    val composeRule: AndroidComposeTestRule<ActivityScenarioRule<MainActivity>, MainActivity>
        get() = _composeRule ?: throw IllegalStateException("composeRule not initialized")

    @Before
    fun setUp() {
        Thread.currentThread().contextClassLoader = javaClass.classLoader
        fixCoroutineExceptionHandlers()
        clearDatabase()

        // Manually create and apply the Compose rule on the Cucumber thread
        // so the ComposeRootRegistry (thread-local) is set up for step definitions.
        val activityRule = ActivityScenarioRule(MainActivity::class.java)
        _composeRule = AndroidComposeTestRule(
            activityRule = activityRule,
            activityProvider = { activityRule.scenario.getActivity()!! },
        )
        val statement = object : Statement() { override fun evaluate() {} }
        val description = Description.createSuiteDescription("PresentCucumber")
        _composeRule!!.apply(statement, description).evaluate()
        Log.i("PresentCucumber", "Compose rule applied on thread: ${Thread.currentThread().name}")
    }

    @After
    fun tearDown() {
        try {
            _composeRule?.activityRule?.scenario?.close()
        } catch (e: Exception) {
            Log.e("PresentCucumber", "tearDown failed", e)
        }
        _composeRule = null
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
