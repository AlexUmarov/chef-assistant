package ru.uao.chef.assistant.ui.cakes

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SpongeCakesViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is sponge cakes Fragment"
    }
    val text: LiveData<String> = _text
}