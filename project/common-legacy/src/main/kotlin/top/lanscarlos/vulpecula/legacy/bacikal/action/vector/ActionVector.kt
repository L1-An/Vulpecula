package top.lanscarlos.vulpecula.legacy.bacikal.action.vector

import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.util.Vector
import taboolib.library.kether.QuestAction
import taboolib.library.kether.QuestReader
import taboolib.module.kether.ScriptActionParser
import taboolib.module.kether.ScriptFrame
import top.lanscarlos.vulpecula.legacy.bacikal.Bacikal
import top.lanscarlos.vulpecula.legacy.bacikal.BacikalParser
import top.lanscarlos.vulpecula.legacy.bacikal.BacikalReader
import top.lanscarlos.vulpecula.legacy.bacikal.LiveData
import top.lanscarlos.vulpecula.legacy.internal.ClassInjector
import top.lanscarlos.vulpecula.legacy.utils.getVariable
import top.lanscarlos.vulpecula.legacy.utils.hasNextToken
import top.lanscarlos.vulpecula.legacy.utils.nextPeek
import top.lanscarlos.vulpecula.legacy.utils.setVariable
import java.util.concurrent.CompletableFuture
import java.util.function.Supplier

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.action.vector
 *
 * @author Lanscarlos
 * @since 2023-03-22 14:48
 */
class ActionVector : QuestAction<Any?>() {

    val handlers = mutableListOf<Handler<*>>()

    fun resolve(reader: QuestReader): QuestAction<Any?> {
        do {
            reader.mark()
            val next = reader.nextToken()
            val isRoot = handlers.isEmpty()
            handlers += registry[next.lowercase()]?.resolve(Reader(next, reader, isRoot))
                ?: let {
                    // 默认使用 vector 构建坐标功能
                    reader.reset()
                    ActionVectorBuild.resolve(Reader(next, reader, isRoot))
                }

            // 判断管道是否已经关闭
            if (handlers.lastOrNull() !is Transfer) {
                if (reader.hasNextToken(">>")) {
                    error("Cannot use \">> ${reader.nextPeek()}\", previous action \"$next\" has closed the pipeline.")
                }
                break
            }
        } while (reader.hasNextToken(">>"))
        return this
    }

    override fun process(frame: ScriptFrame): CompletableFuture<Any?> {
        if (handlers.size == 1 || handlers[0] !is Transfer) {
            return handlers[0].accept(frame).thenApply { adaptVector(it) }
        }

        var previous: CompletableFuture<Vector> = (handlers[0] as Transfer).accept(frame)

        for (index in 1 until handlers.size - 1) {
            val current = handlers[index]

            // 除去最后一个 Handler 以及非 Transfer
            if (current !is Transfer) break

            // 判断 future 是否已完成，减少嵌套
            previous = if (previous.isDone) {
                val vector = previous.getNow(null)
                frame.setVariable("@Transfer", vector, false)
                current.accept(frame)
            } else {
                previous.thenCompose { vector ->
                    frame.setVariable("@Transfer", vector, false)
                    current.accept(frame)
                }
            }
        }

        // 判断 future 是否已完成，减少嵌套
        return if (previous.isDone) {
            val vector = previous.getNow(null)
            frame.setVariable("@Transfer", vector, false)
            handlers.last().accept(frame).thenApply { adaptVector(it) }
        } else {
            previous.thenCompose { vector ->
                frame.setVariable("@Transfer", vector, false)
                handlers.last().accept(frame).thenApply { adaptVector(it) }
            }
        }
    }

    fun adaptVector(any: Any?): Any? {
        return if (any is Vector) {
            org.bukkit.util.Vector(any.x, any.y, any.z)
        } else {
            any
        }
    }

    /*
    * 自动注册包下所有解析器 Resolver
    * */
    @Awake(LifeCycle.LOAD)
    companion object : ClassInjector() {

        private val registry = mutableMapOf<String, Resolver>()

        /**
         * 向 Vector 语句注册子语句
         * @param resolver 子语句解析器
         * */
        fun registerResolver(resolver: Resolver) {
            resolver.name.forEach { registry[it.lowercase()] = resolver }
        }

        override fun visitStart(clazz: Class<*>, supplier: Supplier<*>?) {
            if (!Resolver::class.java.isAssignableFrom(clazz)) return

            val resolver = let {
                if (supplier?.get() != null) {
                    supplier.get()
                } else try {
                    clazz.getDeclaredConstructor().newInstance()
                } catch (e: Exception) {
                    null
                }
            } as? Resolver ?: return

            registerResolver(resolver)
        }

        @BacikalParser(
            id = "vector",
            aliases = ["vector", "vec"]
        )
        fun parser() = ScriptActionParser<Any?> {
            ActionVector().resolve(this)
        }
    }

    /**
     * 语句解析器
     * */
    interface Resolver {

        val name: Array<String>

        fun resolve(reader: Reader): Handler<out Any?>
    }

    /**
     * 语句读取器
     * */
    class Reader(val token: String, source: QuestReader, val isRoot: Boolean) : BacikalReader(source) {

        fun <T> handle(func: Reader.() -> Bacikal.Parser<T>): Handler<T> {
            return Handler(func(this))
        }

        fun transfer(func: Reader.() -> Bacikal.Parser<Vector>): Handler<Vector> {
            return Transfer(func(this))
        }

        fun source(): LiveData<Vector> {
            return if (isRoot) {
                vector(display = "vector source")
            } else {
                LiveData {
                    Bacikal.Action { frame ->
                        CompletableFuture.completedFuture(
                            frame.getVariable<Vector>("@Transfer")
                                ?: error("No vector source selected. [ERROR: vector@$token]")
                        )
                    }
                }
            }
        }

    }

    /**
     * 语句处理器
     * */
    open class Handler<T : Any?>(val parser: Bacikal.Parser<T>) {

        /**
         * 运行
         * */
        open fun accept(frame: ScriptFrame): CompletableFuture<T> {
            return parser.action.run(frame)
        }
    }

    /**
     * 用于传递 Vector
     * */
    open class Transfer(parser: Bacikal.Parser<Vector>) : Handler<Vector>(parser)
}