package top.lanscarlos.vulpecula.kether.property

import org.bukkit.util.Vector
import taboolib.common.OpenResult
import top.lanscarlos.vulpecula.kether.VulKetherProperty
import top.lanscarlos.vulpecula.kether.VulScriptProperty
import top.lanscarlos.vulpecula.utils.coerceDouble

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether.property
 *
 * @author Lanscarlos
 * @since 2022-11-11 23:57
 */

@VulKetherProperty(
    id = "vector-bukkit",
    bind = Vector::class
)
class VectorBukkitProperty : VulScriptProperty<Vector>("vector-bukkit") {

    override fun readProperty(instance: Vector, key: String): OpenResult {
        val property: Any = when (key) {
            "clone" -> instance.clone()
            "taboo" -> taboolib.common.util.Vector(instance.x, instance.y, instance.z)
            "blockX", "block-x" -> instance.blockX
            "blockY", "block-y" -> instance.blockY
            "blockZ", "block-z" -> instance.blockZ
            "x" -> instance.x
            "y" -> instance.y
            "z" -> instance.z
            "length" -> instance.length()
            "lengthSquared", "length-squared", "length-sq", "squared", "sq" -> instance.lengthSquared()
            "isNormalized", "normalized" -> instance.isNormalized
            "normalize" -> instance.clone().normalize()
            "zero" -> instance.clone().zero()
            else -> OpenResult.failed()
        }
        return OpenResult.successful(property)
    }

    override fun writeProperty(instance: Vector, key: String, value: Any?): OpenResult {
        when (key) {
            "x" -> {
                instance.x = value?.coerceDouble() ?: return OpenResult.successful()
            }
            "y" -> {
                instance.y = value?.coerceDouble() ?: return OpenResult.successful()
            }
            "z" -> {
                instance.z = value?.coerceDouble() ?: return OpenResult.successful()
            }
            else -> return OpenResult.failed()
        }
        return OpenResult.successful()
    }
}