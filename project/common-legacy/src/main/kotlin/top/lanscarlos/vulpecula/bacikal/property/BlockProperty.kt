package top.lanscarlos.vulpecula.bacikal.property

import org.bukkit.Material
import org.bukkit.block.Biome
import org.bukkit.block.Block
import taboolib.common.OpenResult
import top.lanscarlos.vulpecula.bacikal.BacikalProperty
import top.lanscarlos.vulpecula.bacikal.BacikalGenericProperty

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.property
 *
 * @author Lanscarlos
 * @since 2023-03-22 10:07
 */
@BacikalProperty(
    id = "block",
    bind = Block::class
)
class BlockProperty : BacikalGenericProperty<Block>("block") {

    override fun readProperty(instance: Block, key: String): OpenResult {
        val property: Any? = when (key) {
            "type", "material", "mat" -> instance.type.name
            "location", "loc" -> instance.location
            "location-x", "loc-x", "x" -> instance.x
            "location-y", "loc-y", "y" -> instance.y
            "location-z", "loc-z", "z" -> instance.z

            "world*" -> instance.world
            "world" -> instance.world.name
            "is-empty" -> instance.isEmpty
            "is-liquid" -> instance.isLiquid
            "passable" -> instance.isPassable

            "biome" -> instance.biome.name
            "drops" -> instance.drops

            "light" -> instance.lightLevel
            "light-sky" -> instance.lightFromSky
            "light-blocks" -> instance.lightFromBlocks

            /* 材质相关属性 */
            "is-solid" -> instance.type.isSolid
            "is-item" -> instance.type.isItem
            "is-record" -> instance.type.isRecord
            "is-occluding" -> instance.type.isOccluding
            "is-interactable" -> instance.type.isInteractable
            "is-fuel" -> instance.type.isFuel
            "is-flammable" -> instance.type.isFlammable
            "is-edible" -> instance.type.isEdible
            "is-burnable" -> instance.type.isBurnable
            "is-block" -> instance.type.isBlock
            "is-air" -> instance.type.isAir
            "has-gravity" -> instance.type.hasGravity()
            "slipperiness" -> instance.type.slipperiness
            "hardness" -> instance.type.hardness
            "slot" -> instance.type.equipmentSlot.name
            "blast-resistance", "resistance" -> instance.type.blastResistance
            "creative-category", "category" -> instance.type.creativeCategory
            else -> return OpenResult.failed()
        }
        return OpenResult.successful(property)
    }

    override fun writeProperty(instance: Block, key: String, value: Any?): OpenResult {
        when (key) {
            "biome" -> {
                val name = value?.toString() ?: return OpenResult.successful()
                instance.biome = Biome.values().firstOrNull {
                    it.name.equals(name, true)
                } ?: return OpenResult.successful()
            }
            "type", "material", "mat" -> {
                val name = value?.toString() ?: return OpenResult.successful()
                instance.type = Material.values().firstOrNull {
                    it.name.equals(name, true)
                } ?: return OpenResult.successful()
            }
            else -> return OpenResult.failed()
        }
        return OpenResult.successful()
    }

}