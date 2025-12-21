package me.rafaelldi.dotnet.kits.core

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.Service.Level
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.platform.util.coroutines.childScope
import kotlinx.coroutines.CoroutineScope

@Service(Level.PROJECT)
internal class DotnetKitsService(private val scope: CoroutineScope) {
    companion object {
        fun getInstance(project: Project): DotnetKitsService = project.service()
    }

    @Suppress("UnstableApiUsage")
    internal fun createScope(name: String): CoroutineScope = scope.childScope(name)
}