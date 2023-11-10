package top.lanscarlos.vulpecula.bacikal.action.animator

import org.bukkit.Location
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.EntityType
import taboolib.common.platform.function.submit
import taboolib.platform.util.toBukkitLocation
import top.lanscarlos.vulpecula.bacikal.Bacikal
import top.lanscarlos.vulpecula.bacikal.action.animator.ActionAnimator.Companion.isValueInPlayerContext
import top.lanscarlos.vulpecula.bacikal.action.animator.ActionAnimator.Companion.removeFromPlayerContext
import top.lanscarlos.vulpecula.bacikal.action.animator.ActionAnimator.Companion.storeInPlayerContext

object ActionAnimatorRun : ActionAnimator.Resolver {

    override val name = arrayOf("run")

    /**
     * animator run at {location} ( duration {tick} )在指定位置生成盔甲架并转换为旁观者模式附身到该盔甲架身上
     */
    override fun resolve(reader: ActionAnimator.Reader): Bacikal.Parser<Any?> {
        return reader.run {
            combine(
                source(),
                optional("at", then = location()),
                argument("duration", "time", then = long(), def = 200)
            ) { target, location, duration ->
                target.forEach {
                    val armorStand = spawnAnimatorArmorStand(location?.toBukkitLocation() ?: it.location)
                    val preGameMode = it.gameMode
                    val preLocation = it.location
                    it.gameMode = org.bukkit.GameMode.SPECTATOR
                    it.spectatorTarget = armorStand

                    storeInPlayerContext(it.uniqueId, "armorStand", armorStand)
                    storeInPlayerContext(it.uniqueId, "preGameMode", preGameMode)
                    storeInPlayerContext(it.uniqueId, "preLocation", preLocation)
                    if (duration > 0) {
                        submit(delay = duration) {
                            if (isValueInPlayerContext(it.uniqueId, "armorStand")) {
                                removeFromPlayerContext(it.uniqueId, "armorStand")

                                it.gameMode = preGameMode
                                it.teleport(preLocation)
                                armorStand.remove()
                            } else this.cancel()
                        }
                    }
                }
            }
        }
    }

    private fun spawnAnimatorArmorStand(location : Location) : ArmorStand {
        val armorStand = location.world?.spawnEntity(location, EntityType.ARMOR_STAND) as? ArmorStand
        armorStand?.let {
            it.location.yaw = location.yaw
            it.location.pitch = location.pitch
            it.setBasePlate(false)
            it.setGravity(false)
            it.isVisible = false
            it.isMarker = true
        }
        return armorStand ?: error("Can't start animator in this location.")
    }

}