package me.rafaelldi.dotnet.kits.core

import com.intellij.ide.minimap.utils.MiniMessagesBundle.getLazyMessage
import com.intellij.ide.minimap.utils.MiniMessagesBundle.getMessage
import org.jetbrains.annotations.NonNls
import org.jetbrains.annotations.PropertyKey

@NonNls
private const val BUNDLE = "messages.DotnetKitsCoreBundle"

internal object DotnetKitsCoreBundle {
    @JvmStatic
    fun message(@PropertyKey(resourceBundle = BUNDLE) key: String, vararg params: Any) =
        getMessage(key, *params)

    @Suppress("unused")
    @JvmStatic
    fun messagePointer(@PropertyKey(resourceBundle = BUNDLE) key: String, vararg params: Any) =
        getLazyMessage(key, *params)
}