package top.lanscarlos.vulpecula.bacikal.action.animator

import org.bukkit.entity.ArmorStand
import top.lanscarlos.vulpecula.bacikal.Bacikal
import top.lanscarlos.vulpecula.bacikal.action.animator.ActionAnimator.Companion.removeFromPlayerContext
import top.lanscarlos.vulpecula.bacikal.action.animator.ActionAnimator.Companion.retrieveFromPlayerContext

object ActionAnimatorStop : ActionAnimator.Resolver {

    override val name = arrayOf("stop")

    /**
     * animator stop 将玩家从旁观者模式转换为生存模式并移除盔甲架、tp 回原来的位置
     */
    override fun resolve(reader: ActionAnimator.Reader): Bacikal.Parser<Any?> {
        return reader.run {
            combine(
                source()
            ) { target ->
                target.forEach {
                    it.spectatorTarget = null
                    it.gameMode = retrieveFromPlayerContext(it.uniqueId, "preGameMode") ?: it.gameMode
                    it.teleport(retrieveFromPlayerContext(it.uniqueId, "preLocation") ?: it.location)
                    val armorStand : ArmorStand = retrieveFromPlayerContext(it.uniqueId, "armorStand") ?: return@forEach
                    armorStand.remove()
                    removeFromPlayerContext(it.uniqueId, "armorStand")
                    removeFromPlayerContext(it.uniqueId, "preGameMode")
                    removeFromPlayerContext(it.uniqueId, "preLocation")
                }
            }
        }
    }
}