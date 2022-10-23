package com.frolo.plugin.stringmover


internal interface GradleModuleFinder {
    fun getAllGradleModules(): List<GradleModule>
    fun getGradleModuleSuggestions(query: String): List<GradleModule>
    fun findGradleModule(query: String): GradleModule?
}