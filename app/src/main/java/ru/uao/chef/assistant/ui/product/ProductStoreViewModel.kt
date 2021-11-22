package ru.uao.chef.assistant.ui.product

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ProductStoreViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is product store Fragment"
    }
    val text: LiveData<String> = _text
}