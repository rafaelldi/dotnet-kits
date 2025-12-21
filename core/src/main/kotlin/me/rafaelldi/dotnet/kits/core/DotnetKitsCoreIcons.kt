package me.rafaelldi.dotnet.kits.core

import com.intellij.ui.IconManager

@Suppress("unused")
internal object DotnetKitsCoreIcons {
    @JvmField
    val Dotnet =
        IconManager.getInstance().getIcon("/icons/dotnet.svg", javaClass.getClassLoader())
}