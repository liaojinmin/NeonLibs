package me.neon.libs.chunk.index

import java.util.regex.Pattern

/**
 * NeonEngine
 * me.neon.engine.chunk.index
 *
 * @author 老廖
 * @since 2025/10/29 09:05
 */
data class VKey(
    private var namespace: String = "neonlibs",
    private var path: String = "realms",
): Comparable<VKey> {

    companion object {

        private const val NAMESPACE_SEPARATOR: Char = ':'
        //private val NAMESPACE_PATTERN: Pattern = Pattern.compile("[a-z0-9._-]+")
        //private val PATH_PATTERN: Pattern = Pattern.compile("[a-z0-9/._-]+")
        private val NAMESPACE_PATTERN: Pattern = Pattern.compile("[\\w\u4e00-\u9fa5.-_]+")
        private val PATH_PATTERN: Pattern = Pattern.compile("[\\w\u4e00-\u9fa5/.-_]+")

        /**
         * 验证给定的命名空间是否有效
         * 允许：字母（大小写）、数字、中文、下划线、破折号、点
         */
        fun isValidNamespace(namespace: String): Boolean {
            return NAMESPACE_PATTERN.matcher(namespace).matches()
        }

        /**
         * 验证给定的路径是否有效
         * 允许：字母（大小写）、数字、中文、斜杠、下划线、破折号、点
         */
        fun isValidPath(path: String): Boolean {
            return PATH_PATTERN.matcher(path).matches()
        }

        // 主方法，根据输入字符串的内容决定调用不同的处理逻辑
        fun String.fromString(): VKey {
            return if (contains(NAMESPACE_SEPARATOR)) {
                // 含有 `:` 则使用 fromStringStrict 进行解析
                fromStringStrict(this)
            } else {
                // 没有 `:` 则认为是 path，使用默认命名空间
                fromStringFlexible(this)
            }
        }

        /** 根据完整字符串解析，如果没有 namespace 则使用默认值 */
        fun fromStringFlexible(input: String, defaultNamespace: String = "neonlibs"): VKey {
            val parts = input.split(NAMESPACE_SEPARATOR, limit = 2)
            return if (parts.size == 2) {
                val ns = parts[0].ifBlank { defaultNamespace }
                val path = parts[1]
                if (!isValidNamespace(ns)) throw IllegalArgumentException("Invalid namespace: $ns")
                if (!isValidPath(path)) throw IllegalArgumentException("Invalid path: $path")
                VKey(ns, path)
            } else {
                val path = input
                if (!isValidPath(path)) throw IllegalArgumentException("Invalid path: $path")
                VKey(defaultNamespace, path)
            }
        }

        /** 传统严格解析，必须有 namespace:path 格式 */
        fun fromStringStrict(input: String): VKey {
            val parts = input.split(NAMESPACE_SEPARATOR, limit = 2)
            if (parts.size != 2) throw IllegalArgumentException("Invalid key format. Expected 'namespace:path'.")
            val ns = parts[0]
            val path = parts[1]
            if (!isValidNamespace(ns)) throw IllegalArgumentException("Invalid namespace: $ns")
            if (!isValidPath(path)) throw IllegalArgumentException("Invalid path: $path")
            return VKey(ns, path)
        }

        /** 直接通过 namespace 和 path 创建 */
        fun fromParts(namespace: String, path: String): VKey {
            if (!isValidNamespace(namespace)) throw IllegalArgumentException("Invalid namespace: $namespace")
            if (!isValidPath(path)) throw IllegalArgumentException("Invalid path: $path")
            return VKey(namespace, path)
        }

    }

    // 获取命名空间
    fun getNamespace(): String = namespace

    // 设置命名空间
    fun setNamespace(namespace: String) {
        if (!isValidNamespace(namespace)) {
            throw IllegalArgumentException("Invalid namespace: $namespace")
        }
        this.namespace = namespace
    }

    // 获取路径
    fun getPath(): String = path

    // 设置路径
    fun setPath(path: String) {
        if (!isValidPath(path)) {
            throw IllegalArgumentException("Invalid path: $path")
        }
        this.path = path
    }

    /**
     * 判断两个 VKey 是否相同
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is VKey) return false
        return namespace == other.namespace && path == other.path
    }

    override fun hashCode(): Int {
        return 31 * namespace.hashCode() + path.hashCode()
    }

    /**
     * 返回 VKey 的字符串表示形式
     */
    override fun toString(): String {
        return "$namespace$NAMESPACE_SEPARATOR$path"
    }

    override fun compareTo(other: VKey): Int {
        var cmp = namespace.compareTo(other.namespace)
        if (cmp == 0) {
            cmp = path.compareTo(other.path)
        }
        return cmp
    }

}