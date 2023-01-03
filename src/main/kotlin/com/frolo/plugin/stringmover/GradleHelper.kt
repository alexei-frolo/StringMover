package com.frolo.plugin.stringmover

import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.vfs.VirtualFile

internal class GradleHelper(private val project: Project) : GradleModuleFinder {

    private fun collectGradleModules(): List<GradleModule> {
        val rootDir = project.guessProjectDir() ?: throw NullPointerException("Failed to guess project dir")
        val gradleModules = ArrayList<GradleModule>()
        collectGradleModules(rootDir, rootDir, gradleModules)
        return gradleModules
    }

    private fun collectGradleModules(file: VirtualFile, rootFile: VirtualFile, dst: MutableList<GradleModule>) {
        if (!file.exists() || !file.isDirectory) {
            return
        }
        val gradleBuildFile = file.children?.find { child ->
            child.exists() && !child.isDirectory && child.name in GRADLE_BUILD_FILES
        }
        if (gradleBuildFile != null) {
            val dirPath = gradleBuildFile.parent.path
            val moduleName = dirPath
                    .removePrefix(rootFile.path)
                    .removePrefix("/")
            val module = GradleModule(
                dirPath = dirPath,
                moduleName = moduleName,
                buildFile = gradleBuildFile.path
            )
            dst.add(module)
        }
        file.children?.forEach { child ->
            if (child.exists() && child.isDirectory) {
                collectGradleModules(child, rootFile, dst)
            }
        }
    }

    override fun getAllGradleModules(): List<GradleModule> {
        return collectGradleModules()
    }

    override fun getGradleModuleSuggestions(query: String): List<GradleModule> {
        return collectGradleModules()
    }

    override fun findGradleModule(query: String): GradleModule? {
        return collectGradleModules().find { it.dirPath == query }
    }

    companion object {
        private val GRADLE_BUILD_FILES = setOf<String>("build.gradle", "build.gradle.kts")
    }
}