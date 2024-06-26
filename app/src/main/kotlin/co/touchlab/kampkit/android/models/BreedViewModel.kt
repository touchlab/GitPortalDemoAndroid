package co.touchlab.kampkit.android.models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.gitportaltemplate.db.Breed
import co.touchlab.gitportaltemplate.repository.BreedDataEvent
import co.touchlab.gitportaltemplate.repository.BreedDataRefreshState
import co.touchlab.gitportaltemplate.repository.BreedRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class BreedViewModel(
    private val breedRepository: BreedRepository,
) : ViewModel() {

    val dataState: StateFlow<BreedDataRefreshState> = breedRepository.dataState

    val breedListState: StateFlow<List<Breed>> = breedRepository.getBreeds()
        .stateIn(
            scope = viewModelScope,
            initialValue = emptyList(),
            started = SharingStarted.WhileSubscribed(5000L)
        )

    val dataEventState: StateFlow<BreedDataEvent> = breedRepository.dataEvents
        .stateIn(
            scope = viewModelScope,
            initialValue = BreedDataEvent.Initial,
            started = SharingStarted.WhileSubscribed(5000L)
        )

    fun refreshBreeds(): Job {
        return viewModelScope.launch {
            breedRepository.refreshBreeds()
        }
    }

    fun updateBreedFavorite(breed: Breed): Job {
        return viewModelScope.launch {
            breedRepository.updateBreedFavorite(breed)
        }
    }
}