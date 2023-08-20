package top.lanscarlos.vulpecula.legacy.bacikal.property.entity

import org.bukkit.entity.Entity
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.util.Vector
import taboolib.common.OpenResult
import top.lanscarlos.vulpecula.legacy.bacikal.BacikalProperty
import top.lanscarlos.vulpecula.legacy.bacikal.BacikalGenericProperty
import top.lanscarlos.vulpecula.legacy.utils.coerceBoolean
import top.lanscarlos.vulpecula.legacy.utils.coerceFloat
import top.lanscarlos.vulpecula.legacy.utils.coerceInt

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.property.entity
 *
 * @author Lanscarlos
 * @since 2023-03-21 23:52
 */
@BacikalProperty(
    id = "entity",
    bind = Entity::class
)
@Suppress("deprecation")
class EntityProperty : BacikalGenericProperty<Entity>("entity") {

    override fun readProperty(instance: Entity, key: String): OpenResult {
        val property: Any? = when (key) {
            "boundingBox", "bounding-box" -> instance.boundingBox
            "entityId", "entity-id", "id" -> instance.entityId
            "facing*" -> instance.facing.name
            "facing" -> instance.facing
            "fallDistance", "fall-distance" -> instance.fallDistance
            "fireTicks", "fire-ticks" -> instance.fireTicks
            "freezeTicks", "freeze-ticks" -> instance.freezeTicks
            "height" -> instance.height
            "lastDamageCause", "last-damage-cause" -> instance.lastDamageCause
            "location", "loc" -> instance.location
            "maxFireTicks", "max-fire-ticks" -> instance.maxFireTicks
            "maxFreezeTicks", "max-freeze-ticks" -> instance.maxFreezeTicks
            "passenger" -> instance.passenger
            "passengers" -> instance.passengers
            "pistonMoveReaction", "piston-move-reaction", "piston-reaction" -> instance.pistonMoveReaction
            "portalCooldown", "portal-cooldown" -> instance.portalCooldown
            "pose" -> instance.pose
            "scoreboardTags", "scoreboard-tags" -> instance.scoreboardTags
            "server" -> instance.server
            "spawn-category" -> instance.spawnCategory
            "ticks-lived" -> instance.ticksLived
            "type*" -> instance.type.name
            "type" -> instance.type.name
            "uniqueId", "unique-id", "uuid" -> instance.uniqueId
            "vehicle" -> instance.vehicle
            "velocity" -> instance.velocity
            "width" -> instance.width
            "world" -> instance.world

            "gravity" -> instance.hasGravity()
            "isCustomNameVisible", "custom-name-visible" -> instance.isCustomNameVisible
            "isDead", "dead" -> instance.isDead
            "isEmpty", "empty" -> instance.isEmpty
            "isFrozen", "frozen" -> instance.isFrozen
            "isGlowing", "glowing" -> instance.isGlowing
            "isInsideVehicle", "inside-vehicle" -> instance.isInsideVehicle
            "isInvulnerable", "invulnerable" -> instance.isInvulnerable
            "isInWater", "in-water" -> instance.isInWater
            "isOnGround", "on-ground" -> instance.isOnGround
            "isPersistent", "persistent" -> instance.isPersistent
            "isSilent", "silent" -> instance.isSilent
            "isValid", "valid" -> instance.isValid
            "isVisualFire", "visual-fire" -> instance.isVisualFire

            // Nameable
            "custom-name" -> instance.customName

            // CommandSender
            "name" -> instance.name

            // ServerOperator
            "isOp", "op" -> instance.isOp
            else -> return OpenResult.failed()
        }
        return OpenResult.successful(property)
    }

    override fun writeProperty(instance: Entity, key: String, value: Any?): OpenResult {
        when (key) {
            "isCustomNameVisible", "custom-name-visible" -> {
                instance.isCustomNameVisible = value?.coerceBoolean() ?: return OpenResult.successful()
            }
            "fallDistance", "fall-distance" -> {
                instance.fallDistance = value?.coerceFloat() ?: return OpenResult.successful()
            }
            "fireTicks", "fire-ticks" -> {
                instance.fireTicks = value?.coerceInt() ?: return OpenResult.successful()
            }
            "freezeTicks", "freeze-ticks" -> {
                instance.freezeTicks = value?.coerceInt() ?: return OpenResult.successful()
            }
            "isGlowing", "glowing" -> {
                instance.isGlowing = value?.coerceBoolean() ?: return OpenResult.successful()
            }
            "gravity" -> {
                instance.setGravity(value?.coerceBoolean() ?: return OpenResult.successful())
            }
            "isInvulnerable", "invulnerable" -> {
                instance.isInvulnerable = value?.coerceBoolean() ?: return OpenResult.successful()
            }
            "lastDamageCause", "last-damage-cause" -> {
                instance.lastDamageCause = value as? EntityDamageEvent
            }
            "passenger" -> {
                instance.setPassenger(value as? Entity ?: return OpenResult.successful())
            }
            "isPersistent", "persistent" -> {
                instance.isPersistent = value?.coerceBoolean() ?: return OpenResult.successful()
            }
            "portalCooldown", "portal-cooldown" -> {
                instance.portalCooldown = value?.coerceInt() ?: return OpenResult.successful()
            }
            "rotation" -> {
                val pair = value as? Pair<*, *> ?: return OpenResult.successful()
                instance.setRotation(
                    pair.first.coerceFloat(0f),
                    pair.second.coerceFloat(0f)
                )
            }
            "isSilent", "silent" -> {
                instance.isSilent = value?.coerceBoolean() ?: return OpenResult.successful()
            }
            "ticks-lived" -> {
                instance.ticksLived = value?.coerceInt() ?: return OpenResult.successful()
            }
            "velocity" -> {
                instance.velocity = value as? Vector ?: Vector(0, 0, 0)
            }
            "isVisualFire", "visual-fire" -> {
                instance.isVisualFire = value?.coerceBoolean() ?: return OpenResult.successful()
            }

            // Nameable
            "customName", "custom-name" -> {
                instance.customName = value?.toString() ?: return OpenResult.successful()
            }

            // ServerOperator
            "isOp", "op" -> {
                instance.isOp = value?.coerceBoolean() ?: return OpenResult.successful()
            }
            else -> return OpenResult.failed()
        }
        return OpenResult.successful()
    }

}