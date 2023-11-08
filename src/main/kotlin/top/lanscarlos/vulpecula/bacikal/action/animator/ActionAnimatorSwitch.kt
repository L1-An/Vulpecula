package top.lanscarlos.vulpecula.bacikal.action.animator

import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import taboolib.platform.type.BukkitPlayer
import top.lanscarlos.vulpecula.bacikal.Bacikal
import top.lanscarlos.vulpecula.utils.setVariable

object ActionAnimatorSwitch : ActionAnimator.Resolver {

    override val name = arrayOf("switch")

    override fun resolve(reader: ActionAnimator.Reader): Bacikal.Parser<Any?> {
        return reader.run {
            combine(
                any(),
            ) { source ->
                val targets: Collection<Player> = when (source) {
                    "*" -> {
                        Bukkit.getOnlinePlayers()
                    }

                    is Player -> {
                        listOf(source)
                    }

                    is BukkitPlayer -> {
                        listOf(source.player)
                    }

                    is OfflinePlayer -> {
                        source.player?.let { listOf(it) } ?: return@combine false
                    }

                    is String -> {
                        Bukkit.getPlayerExact(source)?.let { listOf(it) } ?: return@combine false
                    }

                    is List<*> -> {
                        source.mapNotNull {
                            when (it) {
                                is Player -> it
                                is BukkitPlayer -> it.player
                                is String -> Bukkit.getPlayerExact(it)
                                is OfflinePlayer -> it.player
                                else -> null
                            }
                        }
                    }

                    else -> return@combine false
                }

                this.setVariable("@Animators", targets.distinct())
                return@combine true
            }
        }
    }
}