package com.suyash.mockcivilaviationexam.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.suyash.mockcivilaviationexam.data.local.database.AppDatabase
import com.suyash.mockcivilaviationexam.data.local.repository.SectionRepository
import com.suyash.mockcivilaviationexam.domain.model.Section
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val sectionRepository: SectionRepository
) : ViewModel() {
    private val _sections = MutableStateFlow<List<Section>>(emptyList())
    val sections: StateFlow<List<Section>> = _sections.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    init {
        loadSections()
    }
    
    private fun loadSections() {
        viewModelScope.launch {
            _isLoading.value = true
            sectionRepository.getAllSections().collect { sectionsList ->
                _sections.value = sectionsList
                _isLoading.value = false
            }
        }
    }
}

