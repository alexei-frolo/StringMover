package com.frolo.plugin.stringmover

internal object GradleHelper : GradleModuleFinder {
    @JvmStatic
    fun getAllModules(): List<GradleModule> {
        return listOf(
            GradleModule(":path1"),
            GradleModule(":path2")
        )
    }

    override fun getGradleModuleSuggestions(query: String): List<GradleModule> {
        return listOf(
            GradleModule("GradleModule $query")
        )
    }

    override fun findGradleModule(query: String): GradleModule? {
        return null
    }
}