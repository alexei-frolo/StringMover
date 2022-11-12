package com.frolo.plugin.stringmover

fun interface ErrorDispatcher {
    fun dispatchError(error: Throwable)

    object Rethrow : ErrorDispatcher {
        override fun dispatchError(error: Throwable) {
            throw error
        }
    }
}