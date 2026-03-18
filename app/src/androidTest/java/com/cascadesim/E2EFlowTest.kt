// PHASE 6: End-to-End integration test for CascadeSim

package com.cascadesim

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.*
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cascadesim.core.db.AppDatabase
import com.cascadesim.core.db.dao.WorldDao
import com.cascadesim.game.engine.CascadeEngine
import com.cascadesim.game.model.Decision
import com.cascadesim.game.model.DecisionType
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * E2E flow test for CascadeSim.
 * PHASE 6: Tests complete app flow from launch to decision making
 */
@RunWith(AndroidJUnit4::class)
class E2EFlowTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    private lateinit var database: AppDatabase
    private lateinit var worldDao: WorldDao
    private lateinit var cascadeEngine: CascadeEngine

    @Before
    fun setup() {
        // Initialize test database
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).build()
        worldDao = database.worldDao()
        cascadeEngine = CascadeEngine()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun testAppLaunchesSuccessfully() {
        // Verify home screen loads
        composeTestRule.onNodeWithText("CascadeSim").assertIsDisplayed()
    }

    @Test
    fun testNavigateToDecisionsScreen() {
        // Click on Make Decisions button
        composeTestRule.onNodeWithText("Make Decisions").performClick()

        // Verify decisions screen loads
        composeTestRule.onNodeWithText("Decisions").assertIsDisplayed()
        composeTestRule.onNodeWithText("Available Policies").assertIsDisplayed()
    }

    @Test
    fun testNavigateToEventsScreen() {
        // Click on View Event Feed button
        composeTestRule.onNodeWithText("View Event Feed").performClick()

        // Verify events screen loads
        composeTestRule.onNodeWithText("Event Feed").assertIsDisplayed()
    }

    @Test
    fun testDecisionMakesEventAppear() = runBlocking {
        // Initialize engine
        cascadeEngine.initialize()

        // Make a decision
        val decision = Decision(
            id = "test_decision",
            type = DecisionType.ECONOMIC,
            impactScore = 0.5f
        )

        val events = cascadeEngine.processDecision(decision)

        // Verify events were generated
        assert(events.isNotEmpty()) { "Events should not be empty after decision" }
        assert(events.first().sourceDecisionId == "test_decision") {
            "Event should reference the decision that created it"
        }
    }

    @Test
    fun testEngineTickIncreasesTickCount() = runBlocking {
        cascadeEngine.initialize()
        val initialTick = cascadeEngine.getState().tickCount

        cascadeEngine.tick()

        assert(cascadeEngine.getState().tickCount == initialTick + 1) {
            "Tick count should increment after tick()"
        }
    }

    @Test
    fun testResetClearsEngineState() = runBlocking {
        cascadeEngine.initialize()

        // Make some changes
        cascadeEngine.tick()
        cascadeEngine.tick()

        val tickBeforeReset = cascadeEngine.getState().tickCount
        assert(tickBeforeReset > 0)

        // Reset
        cascadeEngine.reset()

        assert(cascadeEngine.getState().tickCount == 0L) {
            "Tick count should be 0 after reset"
        }
    }

    @Test
    fun testDatabaseInsertAndQuery() = runBlocking {
        // Insert test country
        val country = com.cascadesim.core.db.entity.CountryEntity(
            id = "test_country",
            name = "Testland",
            stability = 0.8f,
            resourcesJson = "{}"
        )

        worldDao.insertCountry(country)

        // Query and verify
        val retrieved = worldDao.getCountryById("test_country")
        assert(retrieved != null) { "Country should be retrieved" }
        assert(retrieved?.name == "Testland") { "Country name should match" }
    }

    @Test
    fun testFullE2EFlow() = runBlocking {
        // 1. Initialize engine
        cascadeEngine.initialize()

        // 2. Make a decision
        val decision = Decision(
            id = "e2e_test_decision",
            type = DecisionType.DIPLOMATIC,
            impactScore = 0.6f
        )
        val events = cascadeEngine.processDecision(decision)

        // 3. Verify events generated
        assert(events.isNotEmpty()) { "Events should be generated" }

        // 4. Persist to database
        val eventEntities = events.map { event ->
            com.cascadesim.core.db.entity.EventEntity(
                id = event.id,
                description = event.description,
                severity = event.severity.name,
                chainId = event.chainId,
                sourceDecisionId = event.sourceDecisionId,
                timestamp = event.timestamp,
                affectedEntityIdsJson = "[]"
            )
        }
        worldDao.insertEvents(eventEntities)

        // 5. Verify database persistence
        val storedEvents = worldDao.getEventsByChain(eventEntities.first().chainId)
        assert(storedEvents.isNotEmpty()) { "Events should be stored in database" }

        // 6. Verify tick count increased
        assert(cascadeEngine.getState().tickCount > 0) {
            "Tick count should increase after decision"
        }
    }
}
