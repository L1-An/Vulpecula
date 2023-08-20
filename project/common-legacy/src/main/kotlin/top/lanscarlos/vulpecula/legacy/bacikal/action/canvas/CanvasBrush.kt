package top.lanscarlos.vulpecula.legacy.bacikal.action.canvas

import taboolib.common.platform.ProxyParticle
import taboolib.common.platform.ProxyPlayer
import taboolib.common.util.Location
import taboolib.common.util.Vector
import taboolib.module.nms.MinecraftVersion
import java.awt.Color

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.action.canvas
 *
 * 笔刷
 *
 * @author Lanscarlos
 * @since 2022-11-08 20:08
 */
class CanvasBrush {

    var particle: ProxyParticle = ProxyParticle.FLAME
    var count: Int = 1
    var speed: Double = -1.0
    var offset: Vector = Vector(0, 0, 0)
    var vector: Vector = Vector(0, 0, 0)

    var size: Float = 1f
    var color: Color = Color.WHITE
        set(value) {
            field = value
            count = 0
            speed = value.alpha.div(255.0)
            vector.x = value.red.div(255.0)
            vector.y = value.green.div(255.0)
            vector.z = value.blue.div(255.0)
        }

    var transition: Color = Color.WHITE
    var material: String = "STONE"
    var data: Int = 0
    var name: String = ""
    var lore: List<String> = emptyList()
    var model: Int = -1

    fun draw(locations: Collection<Location>, viewers: Collection<ProxyPlayer>) {
        if (locations.isEmpty()) return
        if (locations.size == 1) {
            draw(locations.first(), viewers)
            return
        }
        locations.forEach { draw(it, viewers) }
    }

    fun draw(location: Location, viewers: Collection<ProxyPlayer>) {
        if (viewers.isEmpty()) return

        val meta = when (particle) {
            ProxyParticle.BLOCK_DUST -> ProxyParticle.BlockData(material, data)
            ProxyParticle.DUST_COLOR_TRANSITION -> ProxyParticle.DustTransitionData(color, transition, size)
            ProxyParticle.ITEM_CRACK -> ProxyParticle.ItemData(material, data, name, lore, model)
            ProxyParticle.SPELL_MOB,
            ProxyParticle.SPELL_MOB_AMBIENT -> {
                if (speed < 0) {
                    // 默认调整为彩色粒子
                    speed = 1.0
                }
                null
            }
            ProxyParticle.REDSTONE -> {
                if (speed < 0) {
                    // 默认调整为彩色粒子
                    speed = 1.0
                }

                if (MinecraftVersion.major >= 5) {
                    // v1.13+
                    ProxyParticle.DustData(color, size)
                } else {
                    // v1.12 及以下
                    null
                }
            }
            else -> null
        }

        if ((offset.x == 0.0) && (offset.y == 0.0) && (offset.z == 0.0)) {
            viewers.forEach {
                it.sendParticle(particle, location, vector, count, speed.coerceAtLeast(0.0), meta)
            }
        } else {
            viewers.forEach {
                it.sendParticle(particle, location.clone().add(offset), vector, count, speed.coerceAtLeast(0.0), meta)
            }
        }
    }
}