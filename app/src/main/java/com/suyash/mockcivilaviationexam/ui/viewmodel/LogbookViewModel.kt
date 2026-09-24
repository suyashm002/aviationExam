package com.suyash.mockcivilaviationexam.ui.viewmodel

import com.suyash.mockcivilaviationexam.domain.logbook.LogbookUser
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.suyash.mockcivilaviationexam.domain.model.FlightEntry
import com.suyash.mockcivilaviationexam.domain.model.LogbookSummary
import com.suyash.mockcivilaviationexam.domain.usecase.FlightOperationsUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class LogbookViewModel(
    private val flightOperationsUseCase: FlightOperationsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(LogbookUiState())
    val uiState: StateFlow<LogbookUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    private val userId: String get() = LogbookUser.id()
    private var flightsJob: Job? = null

    init {
        observeFlights()
        viewModelScope.launch {
            loadSummary()
        }
    }

    /**
     * Called every time the screen is shown. Only the summary needs refreshing
     * (the list is a live Room flow); it must not leave the spinner on, which
     * is what happened when nothing in the table had changed and the flow
     * therefore never re-emitted.
     */
    fun loadFlights() {
        viewModelScope.launch {
            loadSummary()
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        searchFlights(query)
    }

    private fun searchFlights(query: String) {
        if (query.isBlank()) {
            observeFlights()
            return
        }
        flightsJob?.cancel()

        flightsJob = viewModelScope.launch {
            try {
                val searchResults = flightOperationsUseCase.searchFlights(userId, query)
                _uiState.update { 
                    it.copy(
                        flights = searchResults,
                        searchQuery = query,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        error = e.message,
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun observeFlights() {
        flightsJob?.cancel()
        flightsJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = it.flights.isEmpty()) }
            flightOperationsUseCase.getAllFlights(userId)
                .collect { flights ->
                    _uiState.update { 
                        it.copy(
                            flights = flights,
                            searchQuery = "",
                            isLoading = false
                        )
                    }
                    loadSummary()
                }
        }
    }

    private suspend fun loadSummary() {
        try {
            val summary = flightOperationsUseCase.getLogbookSummary(userId)
            _uiState.update { 
                it.copy(summary = summary)
            }
        } catch (e: Exception) {
            _uiState.update { 
                it.copy(error = e.message)
            }
        }
    }

    fun deleteFlight(flight: FlightEntry) {
        viewModelScope.launch {
            try {
                flightOperationsUseCase.deleteFlight(flight)
                loadSummary() // Refresh summary after deletion
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(error = e.message)
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

data class LogbookUiState(
    val flights: List<FlightEntry> = emptyList(),
    val summary: LogbookSummary = LogbookSummary(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)