package com.suyash.mockcivilaviationexam.logbook

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.suyash.mockcivilaviationexam.CivilAviationApp
import com.suyash.mockcivilaviationexam.domain.logbook.FlightPhase
import com.suyash.mockcivilaviationexam.domain.logbook.LogbookUser
import com.suyash.mockcivilaviationexam.domain.logbook.TrackPoint
import com.suyash.mockcivilaviationexam.ui.screens.flight.FlightEntryScreen
import com.suyash.mockcivilaviationexam.ui.screens.recorder.FlightRecorderScreen
import com.suyash.mockcivilaviationexam.ui.theme.MockcivilAviationExamTheme
import com.suyash.mockcivilaviationexam.ui.viewmodel.FlightEntryViewModel
import com.suyash.mockcivilaviationexam.ui.viewmodel.FlightRecorderViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Drives a whole lesson through the real screens, ViewModels and Room
 * database on the device: Fly Now → off blocks → takeoff → landing → on
 * blocks → review form → save, and checks the logbook row that results.
 * No Firebase account is needed: signed-out entries go under the legacy id.
 */
@RunWith(AndroidJUnit4::class)
class FlyNowFlowTest {

    @get:Rule
    val compose = createComposeRule()

    private val app: CivilAviationApp get() = ApplicationProvider.getApplicationContext()

    @Before
    fun cleanSlate() {
        app.flightRecorder.discard()
        app.logbookPreferences.gpsAutoDetect = false
    }

    @Test
    fun recordAFlightAndSaveIt() {
        val recorder = app.flightRecorder
        val recorderVm = FlightRecorderViewModel(recorder, app.aircraftDefaultsUseCase, app.logbookPreferences)
        val entryVm = FlightEntryViewModel(
            app.flightOperationsUseCase, app.aircraftDefaultsUseCase,
            app.logbookPreferences, app.flightRecorder
        )
        // The Compose rule allows one content block per test, so the two
        // screens are swapped by state, the way the NavGraph would.
        var showReviewForm by mutableStateOf(false)

        compose.setContent {
            MockcivilAviationExamTheme {
                if (!showReviewForm) {
                    FlightRecorderScreen(
                        onNavigateBack = {},
                        onSaveFlight = { showReviewForm = true },
                        onManageAircraft = {},
                        viewModel = recorderVm
                    )
                } else {
                    FlightEntryScreen(
                        onNavigateBack = {},
                        fromRecorder = true,
                        viewModel = entryVm
                    )
                }
            }
        }

        // The seeded Cessna 172N is the default aircraft.
        compose.waitUntil(10_000) { recorderVm.form.value.selectedAircraft != null }
        assertEquals("172N", recorderVm.form.value.selectedAircraft!!.model)

        compose.onNodeWithText("Start flight").performClick()
        compose.waitUntil(5_000) { recorder.state.value.isActive }

        compose.onNodeWithText("Off blocks").performScrollTo().performClick()
        compose.waitUntil(5_000) { recorder.state.value.phase == FlightPhase.TAXI_OUT }

        compose.onNodeWithText("Takeoff").performScrollTo().performClick()
        compose.waitUntil(5_000) { recorder.state.value.phase == FlightPhase.AIRBORNE }

        // GPS is off for this session, so a stray fix must be ignored.
        recorder.onFix(TrackPoint(System.currentTimeMillis(), -1.32, 36.81, 6500.0, 95.0))
        assertEquals(0, recorder.state.value.pointCount)

        compose.onNodeWithText("Landing").performScrollTo().performClick()
        compose.waitUntil(5_000) { recorder.state.value.phase == FlightPhase.TAXI_IN }
        assertEquals(1, recorder.state.value.landings)

        // A touch-and-go the pilot adds by hand.
        compose.onNodeWithContentDescription("Add landing").performClick()
        compose.waitUntil(5_000) { recorder.state.value.landings == 2 }

        compose.onNodeWithText("On blocks").performScrollTo().performClick()
        compose.waitUntil(5_000) { recorder.state.value.phase == FlightPhase.COMPLETE }
        assertNotNull(recorder.state.value.onBlockAt)

        compose.onNodeWithText("Save to logbook").performScrollTo().performClick()
        compose.waitUntil(5_000) { showReviewForm }

        // ---- Review form, pre-filled from the recorder -------------------
        compose.waitUntil(10_000) { entryVm.uiState.value.fromRecorder && entryVm.uiState.value.fleet.isNotEmpty() }
        compose.onNodeWithText("Review Flight").assertIsDisplayed()

        val state = entryVm.uiState.value
        assertEquals("172N", state.aircraftModel)
        assertEquals(2, state.dayLandings)
        assertTrue("off block time should be filled", state.offBlockText.isNotBlank())
        assertTrue("on block time should be filled", state.onBlockText.isNotBlank())

        // The seeded profile has no registration; the form needs one to be valid.
        entryVm.updateAircraftRegistration("5Y-TST")
        entryVm.updateDeparture("HKNW")
        entryVm.updateArrival("HKNW")
        // A same-minute flight has zero block time in whole minutes; give it a
        // plausible total so validation passes, as a pilot would.
        if (entryVm.uiState.value.totalFlightTime <= 0.0) entryVm.updateTotalFlightTime(0.5)
        compose.waitUntil(5_000) { entryVm.uiState.value.isValid }

        entryVm.saveFlight()
        compose.waitUntil(10_000) { entryVm.uiState.value.isSaved }

        // ---- What landed in Room ----------------------------------------
        val flights = runBlocking { app.flightEntryRepository.getAllFlights(LogbookUser.id()).first() }
        val saved = flights.first { it.aircraftRegistration == "5Y-TST" }
        assertEquals(2, saved.dayLandings)
        assertEquals("HKNW - HKNW", saved.routeSummary)
        assertEquals(saved.aircraftId, app.logbookPreferences.defaultAircraftId)

        // The registration typed once is now on the remembered aircraft profile.
        val profile = runBlocking { app.aircraftRepository.getAircraftById(saved.aircraftId!!) }
        assertEquals("5Y-TST", profile!!.registration)

        // The recorder session was consumed.
        assertTrue(recorder.state.value.sessionId.isEmpty())
        assertEquals("HKNW", app.logbookPreferences.lastDeparture)
    }
}
