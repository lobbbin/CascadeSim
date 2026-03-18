// PHASE 5: Basic test scaffolding for MainActivity

package com.cascadesim

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.assertIsDisplayed
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test

/**
 * UI tests for MainActivity navigation.
 * PHASE 5: Basic test scaffolding
 */
@HiltAndroidTest
class MainActivityTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun testHomeScreenLoads() {
        // Verify home screen displays initially
        composeTestRule.onNodeWithText("CascadeSim").assertIsDisplayed()
    }

    @Test
    fun testNavigateToDecisions() {
        // Click on Make Decisions button
        composeTestRule.onNodeWithText("Make Decisions").performClick()
        
        // Verify decisions screen loads
        composeTestRule.onNodeWithText("Decisions").assertIsDisplayed()
        composeTestRule.onNodeWithText("Available Policies").assertIsDisplayed()
    }

    @Test
    fun testNavigateToEvents() {
        // Click on View Event Feed button
        composeTestRule.onNodeWithText("View Event Feed").performClick()
        
        // Verify events screen loads
        composeTestRule.onNodeWithText("Event Feed").assertIsDisplayed()
    }
}
