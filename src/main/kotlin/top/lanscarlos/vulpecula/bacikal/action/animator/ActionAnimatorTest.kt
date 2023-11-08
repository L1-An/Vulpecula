package top.lanscarlos.vulpecula.bacikal.action.animator

import top.lanscarlos.vulpecula.bacikal.Bacikal
import top.lanscarlos.vulpecula.bacikal.LiveData

object ActionAnimatorTest : ActionAnimator.Resolver {

    override val name = arrayOf("test")

    /**
     * animator test {content}/{action}
     */
    override fun resolve(reader: ActionAnimator.Reader): Bacikal.Parser<Any?> {
        return reader.run {
            combine(
                source(),
                LiveData {
                    val action = readAction()
                    Bacikal.Action { frame ->
                        frame.newFrame(action).run<Any?>().thenApply { it }
                    }
                }
            ) { target, content ->
                val message = content.toString()
                target.forEach { player ->
                    player.sendMessage(message)
                }
            }
        }
    }
}