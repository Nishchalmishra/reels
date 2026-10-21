package arivihan.technologies.doubtbuzzter2.toppernotes.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arivihan.technologies.doubtbuzzter2.MyApplication
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys
import arivihan.technologies.doubtbuzzter2.network.ApiResult
import arivihan.technologies.doubtbuzzter2.network.errorMessage
import arivihan.technologies.doubtbuzzter2.toppernotes.domain.usecase.GetTopperNotesUseCase
import arivihan.technologies.doubtbuzzter2.toppernotes.domain.usecase.GetToppersUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TopperNotesViewModel @Inject constructor(
    private val getToppers: GetToppersUseCase,
    private val getTopperNotes: GetTopperNotesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(TopperNotesUiState())
    val uiState: StateFlow<TopperNotesUiState> = _uiState.asStateFlow()

    private var notesJob: Job? = null

    init {
        loadToppers()
    }

    fun loadToppers() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(isLoadingToppers = true, errorMessage = null, isNetworkError = false)
            }

            when (val result = getToppers()) {
                is ApiResult.Success -> {
                    val toppers = result.data
                    val remembered = MyApplication.getStringData(SelectedTopperKey)
                    val selected = toppers.firstOrNull { it.code == remembered }
                        ?: toppers.firstOrNull()
                    _uiState.update {
                        it.copy(
                            isLoadingToppers = false,
                            toppers = toppers,
                            selectedTopperCode = selected?.code,
                            selectedSubjectId = selected?.subjects?.firstOrNull()?.id,
                            notes = emptyList()
                        )
                    }
                    loadNotes()
                }

                else -> _uiState.update {
                    it.copy(
                        isLoadingToppers = false,
                        errorMessage = result.errorMessage,
                        isNetworkError = result is ApiResult.NetworkError
                    )
                }
            }
        }
    }

    fun onTopperSelected(topperCode: String) {
        val state = _uiState.value
        if (state.selectedTopperCode == topperCode) return

        val topper = state.toppers.firstOrNull { it.code == topperCode } ?: return
        val subjectId = topper.subjects
            .firstOrNull { it.id == state.selectedSubjectId }?.id
            ?: topper.subjects.firstOrNull()?.id

        MyApplication.saveData(SelectedTopperKey, topperCode)

        _uiState.update {
            it.copy(
                selectedTopperCode = topperCode,
                selectedSubjectId = subjectId,
                notes = emptyList()
            )
        }
        loadNotes()
    }

    fun onSubjectSelected(subjectId: String) {
        if (_uiState.value.selectedSubjectId == subjectId) return
        _uiState.update { it.copy(selectedSubjectId = subjectId, notes = emptyList()) }
        loadNotes()
    }

    fun retry() {
        if (_uiState.value.toppers.isEmpty()) loadToppers() else loadNotes()
    }

    private fun loadNotes() {
        val state = _uiState.value
        val topperCode = state.selectedTopperCode
        val subjectId = state.selectedSubjectId
        if (topperCode.isNullOrBlank() || subjectId.isNullOrBlank()) {
            _uiState.update { it.copy(isLoadingNotes = false, notes = emptyList()) }
            return
        }

        notesJob?.cancel()
        notesJob = viewModelScope.launch {
            _uiState.update {
                it.copy(isLoadingNotes = true, errorMessage = null, isNetworkError = false)
            }

            when (val result = getTopperNotes(topperCode, subjectId)) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(isLoadingNotes = false, notes = result.data)
                }

                else -> _uiState.update {
                    it.copy(
                        isLoadingNotes = false,
                        errorMessage = result.errorMessage,
                        isNetworkError = result is ApiResult.NetworkError
                    )
                }
            }
        }
    }

    private companion object {
        val SelectedTopperKey: String = PreferenceKeys.TOPPER_NOTES_SELECTED_TOPPER.key
    }
}
