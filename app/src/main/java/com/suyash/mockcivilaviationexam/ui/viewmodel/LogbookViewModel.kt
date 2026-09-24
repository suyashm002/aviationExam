package com.suyash.mockcivilaviationexam.ui.viewmodel

import com.suyash.mockcivilaviationexam.domain.logbook.LogbookUser
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.suyash.mockcivilaviationexam.domain.model.FlightEntry
import com.suyash.mockcivilaviationexam.domain.model.LogbookSummary
import com.suyash.mockcivilaviationexam.domain.usecase.FlightOperationsUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter

class LogbookViewModel(
    private val flightOperationsUseCase: FlightOperationsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(LogbookUiState())
    val uiState: StateFlow<LogbookUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    private val userId: String get() = LogbookUser.id()

    init {
        observeFlights()
        viewModelScope.launch { loadSummary() }
    }

    /**
     * Called every time the screen is shown. Only the summary needs refreshing
     * (the list is a live Room flow); it must not leave the spinner on, which
     * is what happened when nothing in the table had changed and the flow
     * therefore never re-emitted.
     */
    fun loadFlights() {
        viewModelScope.launch { loadSummary() }
    }

    /**
     * The text field is driven from [LogbookUiState.searchQuery], so it must be
     * updated synchronously here. Updating it only when the results arrived
     * made the field lag one keystroke behind: the cursor jumped to the start
     * and the query that reached the database was garbled.
     */
    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        _searchQuery.value = query
    }

    /**
     * One collector for the life of the ViewModel: the Room flow combined with
     * the query, filtered in memory. A logbook is a few hundred rows at most,
     * so this is instant and cannot race the way per-keystroke DB queries did.
     */
    private fun observeFlights() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            combine(
                flightOperationsUseCase.getAllFlights(userId),
                _searchQuery.map { it.trim() }.distinctUntilChanged()
            ) { flights, query ->
                if (query.isBlank()) flights else flights.filter { it.matches(query) }
            }.collect { flights ->
                _uiState.update { it.copy(flights = flights, isLoading = false) }
                loadSummary()
            }
        }
    }

    private fun FlightEntry.matches(query: String): Boolean {
        val q = query.lowercase()
        return listOfNotNull(
            departureAerodrome, arrivalAerodrome, routeVia,
            aircraftRegistration, aircraftType, aircraftModel,
            remarks, instructorName, exerciseNumber, lessonNumber,
            date.format(DATE_FORMAT), date.toString()
        ).any { it.lowercase().contains(q) }
    }

    private suspend fun loadSummary() {
        try {
            val summary = flightOperationsUseCase.getLogbookSummary(userId)
            _uiState.update { it.copy(summary = summary) }
        } catch (e: Exception) {
            _uiState.update { it.copy(error = e.message) }
        }
    }

    fun deleteFlight(flight: FlightEntry) {
        viewModelScope.launch {
            try {
                flightOperationsUseCase.deleteFlight(flight)
                loadSummary()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    private companion object {
        val DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMM yyyy")
    }
}

data class LogbookUiState(
    val flights: List<FlightEntry> = emptyList(),
    val summary: LogbookSummary = LogbookSummary(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)
