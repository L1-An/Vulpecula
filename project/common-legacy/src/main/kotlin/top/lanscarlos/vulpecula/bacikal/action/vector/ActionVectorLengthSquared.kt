package top.lanscarlos.vulpecula.bacikal.action.vector

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.action.vector
 *
 * @author Lanscarlos
 * @since 2023-03-22 15:26
 */
object ActionVectorLengthSquared : ActionVector.Resolver {

    override val name: Array<String> = arrayOf("length2", "size2")

    /**
     * vec length2 &vec
     * */
    override fun resolve(reader: ActionVector.Reader): ActionVector.Handler<out Any?> {
        return reader.handle {
            combine(
                source(),
            ) { vector ->
                vector.lengthSquared()
            }
        }
    }
}