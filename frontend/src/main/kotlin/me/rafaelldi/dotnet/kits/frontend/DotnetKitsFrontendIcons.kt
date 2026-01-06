package me.rafaelldi.dotnet.kits.frontend

import com.intellij.ui.IconManager

@Suppress("unused")
internal object DotnetKitsFrontendIcons {
    @JvmField
    val Dotnet =
        IconManager.getInstance().getIcon("/icons/dotnet.svg", javaClass.getClassLoader())
}