package com.example.kanjilearning.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * VI: ViewModel chia sẻ để các Fragment điều chỉnh tiêu đề AppBar.
 * EN: Shared ViewModel letting fragments set the toolbar title.
 */
class MainToolbarViewModel : ViewModel() {

    private val _title = MutableLiveData<String>()
    val title: LiveData<String> = _title

    /**
     * VI: Cập nhật tiêu đề, fragment có thể gọi khi onResume.
     * EN: Called by fragments to update the toolbar label.
     */
    fun updateTitle(value: String) {
        _title.value = value
    }
}
