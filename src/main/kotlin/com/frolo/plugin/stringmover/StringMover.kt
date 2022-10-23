package com.frolo.plugin.stringmover

class StringMover {
    data class Params(
        val srcModule: GradleModule,
        val dstModule: GradleModule,
        val stringKeys: Set<String>
    )

    fun moveStrings(params: Params) {

    }
}