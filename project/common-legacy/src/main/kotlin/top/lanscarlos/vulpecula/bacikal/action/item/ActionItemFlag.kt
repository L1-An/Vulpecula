package top.lanscarlos.vulpecula.bacikal.action.item

import org.bukkit.inventory.ItemFlag

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.action.item
 *
 * @author Lanscarlos
 * @since 2023-03-24 17:12
 */
object ActionItemFlag : ActionItem.Resolver {

    override val name: Array<String> = arrayOf("flags", "flag")

    /**
     * item flag &item add &type
     * item flag &item remove &type
     * item flag &item clear
     * item flag &item has &type
     * */
    override fun resolve(reader: ActionItem.Reader): ActionItem.Handler<out Any?> {
        val source = reader.source().accept(reader)

        reader.mark()
        return when (reader.nextToken()) {
            "add", "plus" -> {
                reader.transfer {
                    combine(
                        source,
                        text(display = "flag type")
                    ) { item, type ->
                        val meta = item.itemMeta ?: return@combine item
                        meta.addItemFlags(type.asFlag() ?: return@combine item)
                        item.also { it.itemMeta = meta }
                    }
                }
            }
            "remove", "rm" -> {
                reader.transfer {
                    combine(
                        source,
                        text(display = "flag type")
                    ) { item, type ->
                        val meta = item.itemMeta ?: return@combine item
                        meta.removeItemFlags(type.asFlag() ?: return@combine item)
                        item.also { it.itemMeta = meta }
                    }
                }
            }
            "clear" -> {
                reader.transfer {
                    combine(
                        source
                    ) { item ->
                        val meta = item.itemMeta ?: return@combine item
                        meta.removeItemFlags(*meta.itemFlags.toTypedArray())
                        item.also { it.itemMeta = meta }
                    }
                }
            }
            "has", "contains" -> {
                reader.handle {
                    combine(
                        source,
                        text(display = "flag type")
                    ) { item, type ->
                        val meta = item.itemMeta ?: return@combine false
                        meta.hasItemFlag(type.asFlag() ?: return@combine false)
                    }
                }
            }
            else -> {
                reader.reset()
                reader.handle {
                    combine(source) { item -> item.itemMeta?.itemFlags }
                }
            }
        }
    }

    private fun String.asFlag(): ItemFlag? {
        return ItemFlag.values().firstOrNull { it.name.equals(this, true) }
    }
}