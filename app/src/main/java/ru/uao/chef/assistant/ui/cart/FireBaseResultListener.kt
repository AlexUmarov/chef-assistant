package ru.uao.chef.assistant.ui.cart

interface FireBaseResultListener {
    fun onResult(isAdded: Boolean)
    fun onError(error: Throwable)
}