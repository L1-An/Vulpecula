package top.lanscarlos.vulpecula.bacikal.action.animator

import org.bukkit.entity.Player
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.library.kether.QuestAction
import taboolib.library.kether.QuestReader
import taboolib.module.kether.ScriptActionParser
import taboolib.module.kether.ScriptFrame
import top.lanscarlos.vulpecula.bacikal.Bacikal
import top.lanscarlos.vulpecula.bacikal.BacikalParser
import top.lanscarlos.vulpecula.bacikal.BacikalReader
import top.lanscarlos.vulpecula.bacikal.LiveData
import top.lanscarlos.vulpecula.internal.ClassInjector
import top.lanscarlos.vulpecula.utils.getVariable
import top.lanscarlos.vulpecula.utils.playerOrNull
import top.lanscarlos.vulpecula.utils.toBukkit
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.function.Supplier

class ActionAnimator : QuestAction<Any?>()  {
    lateinit var handler : Bacikal.Parser<Any?>

    fun resolve(reader : QuestReader) : QuestAction<Any?> {
        val next = reader.nextToken()
        handler = registry[next.lowercase()]?.resolve(Reader(next, reader))
            ?: error("Unknown sub action \"$next\" at animator action.")

        return this
    }

    override fun process(frame: ScriptFrame): CompletableFuture<Any?> {
        return handler.action.run(frame)
    }

    /**
     * 自动注册包下所有解析器 Resolver
     */
    @Awake(LifeCycle.LOAD)
    companion object : ClassInjector() {

        private val registry = mutableMapOf<String, Resolver>()

        /**
         * 向 Animator 语句注册子语句
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
            id = "animator",
            aliases = ["animator"]
        )
        fun parser() = ScriptActionParser<Any?> {
            ActionAnimator().resolve(this)
        }

        /**
         * 存储玩家的一些数据
         */
        val globalPlayerContext : MutableMap<UUID, MutableMap<String, Any>> = mutableMapOf()

        /**
         * 使用这个函数来为特定玩家在Context中存储值
         */
        fun storeInPlayerContext(playerUUID: UUID, key: String, value: Any) {
            val playerContext = globalPlayerContext.getOrPut(playerUUID) { mutableMapOf() }
            playerContext[key] = value
        }

        /**
         * 使用这个函数来从特定玩家的Context中检索值
         */
        fun <T> retrieveFromPlayerContext(playerUUID: UUID, key: String): T? {
            val playerContext = globalPlayerContext[playerUUID] ?: return null
            @Suppress("UNCHECKED_CAST")
            return playerContext[key] as? T
        }

        /**
         * 使用这个函数来检查特定玩家的Context中是否存在值
         */
        fun isValueInPlayerContext(playerUUID: UUID, key: String): Boolean {
            val playerContext = globalPlayerContext[playerUUID]
            return playerContext?.containsKey(key) ?: false
        }

        /**
         * 使用这个函数来从特定玩家的Context中移除值
         */
        fun removeFromPlayerContext(playerUUID: UUID, key: String) {
            val playerContext = globalPlayerContext[playerUUID]
            playerContext?.remove(key)
        }

    }

    /**
     * 语句解析器
     * */
    interface Resolver {

        val name: Array<String>

        fun resolve(reader: Reader): Bacikal.Parser<Any?>
    }

    /**
     * 语句读取器
     * */
    class Reader(val token: String, source: QuestReader) : BacikalReader(source) {
        fun source(): LiveData<List<Player>> {
            return LiveData {
                Bacikal.Action { frame ->
                    val animators = frame.getVariable<List<Player>>("@Animators") ?: listOf(
                        frame.playerOrNull()?.toBukkit() ?: error("No player selected. [ERROR: animator@$token]")
                    )
                    CompletableFuture.completedFuture(animators)
                }
            }
        }
    }

}