package top.lanscarlos.vulpecula.bacikal.property.event

import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import taboolib.common.OpenResult
import top.lanscarlos.vulpecula.bacikal.BacikalProperty
import top.lanscarlos.vulpecula.bacikal.BacikalGenericProperty

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.property.event
 *
 * @author Lanscarlos
 * @since 2023-03-22 14:29
 */
@BacikalProperty(
    id = "player-command-event",
    bind = PlayerInteractEvent::class
)
class PlayerInteractEventProperty : BacikalGenericProperty<PlayerInteractEvent>("player-command-event") {
    override fun readProperty(instance: PlayerInteractEvent, key: String): OpenResult {
        val property: Any? = when (key) {
            "action" -> instance.action.name
            "is-left-click", "is-left", "left-click" -> instance.action == Action.LEFT_CLICK_AIR || instance.action == Action.LEFT_CLICK_BLOCK
            "is-right-click", "is-right", "right-click" -> instance.action == Action.RIGHT_CLICK_AIR || instance.action == Action.RIGHT_CLICK_BLOCK
            "is-click-air", "click-air" -> instance.action == Action.LEFT_CLICK_AIR || instance.action == Action.RIGHT_CLICK_AIR
            "is-click-block", "click-block" -> instance.action == Action.LEFT_CLICK_BLOCK || instance.action == Action.RIGHT_CLICK_BLOCK
            "is-physical" -> instance.action == Action.PHYSICAL
            "hand" -> instance.hand?.name ?: "NULL"
            "item" -> instance.item
            "block" -> instance.clickedBlock
            "has-block" -> instance.hasBlock()
            "has-item" -> instance.hasItem()
            "is-block-place", "is-place" -> instance.isBlockInHand
            "player", "sender" -> instance.player
            else -> return OpenResult.failed()
        }
        return OpenResult.successful(property)
    }

    override fun writeProperty(instance: PlayerInteractEvent, key: String, value: Any?): OpenResult {
        return OpenResult.failed()
    }
}