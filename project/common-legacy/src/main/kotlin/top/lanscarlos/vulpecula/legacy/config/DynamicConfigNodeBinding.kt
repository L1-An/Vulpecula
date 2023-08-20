package top.lanscarlos.vulpecula.legacy.config

import taboolib.library.configuration.ConfigurationSection
import taboolib.module.configuration.ConfigLoader
import kotlin.reflect.KProperty

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.config
 *
 * @author Lanscarlos
 * @since 2022-12-15 22:05
 */
@Suppress("UNCHECKED_CAST")
class DynamicConfigNodeBinding<R>(
    val path: String,
    bind: String,
    private val transfer: ConfigurationSection.(Any?) -> R
) : DynamicConfigNode<R>, Runnable {

    var isInitialized = false
    var value: R? = null

    val config by lazy {
        ConfigLoader.files[bind]?.configuration?.also {
            it.onReload(this)
        } ?: error("config \"$bind\" not defined.")
    }

    override fun run() {
        value = transfer(config, config[path])
    }

    override fun getValue(any: Any?, property: KProperty<*>): R {
        if (!isInitialized) {
            value = transfer(config, config[path])
            isInitialized = true
        }
        return value as R
    }
}