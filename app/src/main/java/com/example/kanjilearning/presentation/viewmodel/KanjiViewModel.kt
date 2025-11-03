package com.example.kanjilearning.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kanjilearning.data.model.KanjiEntity
import com.example.kanjilearning.domain.usecase.GetAllKanjisUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * VI: ViewModel giữ logic UI và sống lâu hơn Activity.
 * EN: ViewModel owning the UI data and surviving configuration changes.
 */
@HiltViewModel
class KanjiViewModel @Inject constructor(
    private val getAllKanjis: GetAllKanjisUseCase
) : ViewModel() {

    private val _allKanjis = MutableLiveData<List<KanjiEntity>>()
    val allKanjis: LiveData<List<KanjiEntity>> = _allKanjis

    init {
        observeKanjis()
    }

    /**
     * VI: Collect Flow trong viewModelScope để tránh rò rỉ.
     * EN: Collect the database stream using viewModelScope + coroutines.
     */
    private fun observeKanjis() {
        viewModelScope.launch {
            getAllKanjis().collectLatest { kanjis ->
                _allKanjis.postValue(kanjis)
            }
        }
    }
}
