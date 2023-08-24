package top.lanscarlos.vulpecula.bacikal.quest

import taboolib.library.configuration.ConfigurationSection

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.quest
 *
 * @author Lanscarlos
 * @since 2023-08-20 23:15
 */
open class BacikalQuestBuilder {

    /**
     * 主函数名
     * */
    var name = "main"

    /**
     * 预设命名空间
     * */
    val namespace = mutableListOf<String>()

    /**
     * 前置处理
     * */
    val preprocessor = StringBuilder()

    /**
     * 后置处理
     * */
    val postprocessor = StringBuilder()

    /**
     * 预设变量
     * */
    val variables = mutableMapOf<String, StringBuilder>()

    /**
     * 脚本内容
     * */
    val content = StringBuilder()

    /**
     * 条件体
     * */
    val condition = StringBuilder()

    /**
     * 条件假值分支
     * */
    val deny = StringBuilder()

    /**
     * 异常捕捉
     * */
    val exceptions = mutableMapOf<String, StringBuilder>() // catch -> handle

    /**
     * 其他函数
     * */
    val functions = mutableListOf<BacikalQuestBuilder>()

    /**
     * 源码转换器
     * */
    val transfer = mutableListOf(
        FragmentReplacer(),
        CommentEraser(),
        UnicodeEscalator()
    )

    /**
     * 编译器
     * */
    var compiler: BacikalQuestCompiler? = DefaultBacikalCompiler

    /**
     * 脚本源码缓存
     * */
    val source = StringBuilder()

    /**
     * 构建源码
     * */
    fun buildSource(): String {
        // 清空原有源码
        val builder = source.clear()

        if (content.startsWith("def ")) {
            // 自行定义函数
            builder.append(content.toString())
            return builder.toString()
        }

        /*
        * 导入预设命名空间
        * */
        if (namespace.isNotEmpty()) {
            for (it in namespace) {
                builder.append("import $it\n")
            }
            builder.append('\n')
        }

        /*
        * 构建前置处理语句
        * */
        if (preprocessor.isNotEmpty()) {
            builder.append(preprocessor.toString())
            builder.append('\n')
        }

        /*
        * 构建变量
        * set $key to $value
        * set $key to {
        *   ...$value
        * }
        * */
        if (variables.isNotEmpty()) {
            for (entry in variables) {
                if (entry.value.contains('\n')) {
                    // 含有换行
                    builder.append("set ${entry.key} to {\n")
                    builder.appendWithIndent(entry.value.toString())
                    builder.append("}\n")
                } else {
                    // 不含有换行
                    builder.append("set ${entry.key} to ${entry.value}\n")
                }
            }
            builder.append('\n')
        }

        /*
        * 构建核心语句
        * */
        if (content.isNotEmpty()) {
            builder.append(content.toString())
        }

        /*
        * 构建条件体
        * if {
        *   ...$condition
        * } then {
        *   ...$content
        * } else {
        *   ...$deny
        * }
        * */
        if (condition.isNotEmpty()) {
            val content = builder.extract()

            builder.append("if {\n")
            builder.appendWithIndent(condition.toString())
            builder.append("} then {\n")
            builder.appendWithIndent(content)
            if (deny.isNotEmpty()) {
                builder.append("} else {\n")
                builder.appendWithIndent(deny.toString())
            }
            builder.append("}")
        }

        /*
        * 构建异常捕捉
        * try {
        *   ...$content
        * } catch with "...$key...|...$key2..." {
        *   ...$value
        * }
        * */
        if (exceptions.isNotEmpty()) {
            val content = builder.extract()

            builder.append("try {\n")
            builder.appendWithIndent(content)
            builder.append("}\n")

            for (it in exceptions) {
                if (it.value.isEmpty()) continue

                builder.append(" catch ")
                if (it.key.isNotEmpty() && it.key != "*") {
                    builder.append("with \"${it.key}\" ")
                }
                builder.append("{\n")
                builder.appendWithIndent(it.value.toString())
                builder.append("}")
            }
        }

        /*
        * 构建后置语句
        * */
        if (postprocessor.isNotEmpty()) {
            builder.append('\n')
            builder.append(postprocessor.toString())
        }

        /*
        * 构建方法体
        * def $name = {
        *   ...$content
        * }
        * */
        if (!builder.startsWith("def")) {
            // 提取先前所有内容
            val content = builder.extract()

            builder.append("def $name = {\n")
            builder.appendWithIndent(content)
            builder.append("}")
        }

        /*
        * 追加其他函数
        * */
        for (function in functions) {
            if (function.name == name) {
                continue
            }
            builder.append("\n\n")
            builder.append(function.buildSource())
        }

        /*
        * 源码转换
        * */
        for (transfer in transfer) {
            transfer.transfer(builder)
        }

        return builder.toString()
    }

    fun compile(namespace: List<String>): BacikalQuest {
        if (source.isEmpty()) {
            // 构建源码
            buildSource()
        }
        return compiler?.compile(source.toString(), namespace) ?: error("Quest Compiler is null.")
    }

    /**
     * 追加前置处理
     * */
    fun appendPreprocessor(value: Any?) {
        if (value == null) return
        preprocessor.appendSection(value)
    }

    /**
     * 追加后置处理
     * */
    fun appendPostprocessor(value: Any?) {
        if (value == null) return
        postprocessor.appendSection(value)
    }

    /**
     * 追加内容
     * */
    fun appendContent(value: Any?) {
        if (value == null) return
        content.appendSection(value)
    }

    /**
     * 追加文本
     * */
    fun appendLiteral(value: String) {
        content.append(value)
    }

    fun appendCondition(value: Any?) {
        if (value == null) return
        condition.appendSection(value)
    }

    fun appendDeny(value: Any?) {
        if (value == null) return
        deny.appendSection(value)
    }

    fun appendVariables(value: Any?) {
        when (value) {
            is ConfigurationSection -> {
                for (key in value.getKeys(false)) {
                    variables.computeIfAbsent(key) { StringBuilder() }.appendSection(value[key] ?: continue)
                }
            }
            is Map<*, *> -> {
                for (entry in value) {
                    val key = entry.key?.toString() ?: continue
                    variables.computeIfAbsent(key) { StringBuilder() }.appendSection(entry.value ?: continue)
                }
            }
        }
    }

    fun appendExceptions(value: Any?) {
        when (value) {
            is String -> exceptions.computeIfAbsent("*") { StringBuilder() }.appendSection(value)
            is Map<*, *>,
            is ConfigurationSection -> {
                // 指定了异常类型
                val section = if (value is ConfigurationSection) {
                    value.toMap()
                } else value as Map<*, *>

                val handle = section["handle"] ?: return
                val catch = section["catch"]?.flatBy("|") ?: return
                exceptions.computeIfAbsent(catch) { StringBuilder() }.appendSection(handle)
            }
            is List<*> -> {
                value.mapNotNull { it as? Map<*, *> }.forEach { section ->
                    val handle = section["handle"] ?: return
                    val catch = section["catch"]?.flatBy("|") ?: return@forEach
                    exceptions.computeIfAbsent(catch) { StringBuilder() }.appendSection(handle)
                }
            }
        }
    }

    fun appendFunction(builder: BacikalQuestBuilder) {
        functions.add(builder)
    }

    fun appendFunction(builder: BacikalQuestBuilder.() -> Unit) {
        appendFunction(BacikalQuestBuilder().also(builder))
    }

    /**
     * 追加内容
     * */
    private fun StringBuilder.appendSection(section: Any) {
        when (section) {
            is String, is StringBuilder, is StringBuffer -> {
                if (this.isNotEmpty()) {
                    // 此前含有其他内容，换行隔开
                    this.append('\n')
                }
                this.append(section.toString())
            }
            is Map<*, *>,
            is ConfigurationSection -> {
                val it = if (section is ConfigurationSection) {
                    section.toMap()
                } else section as Map<*, *>

                val content = it["content"]?.format(it["type"]?.toString() ?: "ke")

                if (this.isNotEmpty()) {
                    // 此前含有其他内容，换行隔开
                    this.append('\n')
                }
                this.append(content)
            }
            is List<*> -> {
                section.forEach {
                    if (it == null) return@forEach
                    appendSection(it)
                }
            }
            else -> {
                error("Unsupported section type: ${section::class.java.name} [ERROR@BacikalScriptBuilder#appendSection]")
            }
        }
    }

    /**
     * 抽取所有字符并清空容器
     * */
    private fun StringBuilder.extract(): String {
        val content = this.toString()
        this.clear()
        return content
    }

    /**
     * 追加缩进内容
     * */
    private fun StringBuilder.appendWithIndent(content: String) {
        content.split('\n').joinTo(
            buffer = this,
            separator = "\n    ",
            prefix = "    ",
            postfix = "\n"
        )
    }

    /**
     * 将未知的内容扁平化为字符串
     * */
    private fun Any?.flatBy(separator: CharSequence): String {
        return when (this) {
            is String -> this
            is Array<*> -> this.mapNotNull { it?.toString() }.joinToString(separator = separator)
            is Collection<*> -> this.mapNotNull { it?.toString() }.joinToString(separator = separator)
            else -> ""
        }
    }

    /**
     * 根据指定的算法格式化内容
     * */
    private fun Any.format(algorithm: String = "ke"): String {
        return when (algorithm.lowercase()) {
            "ke", "kether" -> this.toString()
            "js", "javascript" -> "js '$this'"
            "ks", "script" -> "vul script run *\"$this\""
//            "kf", "fragment" -> ScriptFragment.link(this, content)
//            "kf", "fragment" -> "fragment $content"
            else -> error("Unknown format algorithm: $algorithm")
        }
    }
}