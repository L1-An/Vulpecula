package top.lanscarlos.vulpecula.bacikal.action.illusion

import top.lanscarlos.vulpecula.bacikal.Bacikal
import top.lanscarlos.vulpecula.volatile.VolatileEntityMetadata

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.action.illusion
 *
 * @author Lanscarlos
 * @since 2023-08-09 23:15
 */
object ActionIllusionHealth : ActionIllusion.Resolver {

    override val name = arrayOf("health")

    override fun resolve(reader: ActionIllusion.Reader): Bacikal.Parser<Any?> {
        return reader.run {
            combine(
                source(),
                expect("to", then = double()),
                argument("duration", then = int()), // 幻觉持续时间
                argument("period", then = long()) // 幻觉刷新间隔
            ) { target, health, duration, period ->
                target.forEach { VolatileEntityMetadata.updateHealth(it, it, health.toFloat()) }
            }
        }
    }
}