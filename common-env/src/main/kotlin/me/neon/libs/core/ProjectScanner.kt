@file:Suppress("UNCHECKED_CAST")

package me.neon.libs.core

import me.neon.libs.RuntimeClass
import org.tabooproject.reflex.ReflexClass
import java.io.File
import java.lang.reflect.Modifier
import java.net.JarURLConnection
import java.net.URISyntaxException
import java.net.URL
import java.util.*
import java.util.function.Supplier
import java.util.jar.JarFile


/**
 * 当前插件的所有类（在本体中）
 */
val runningClassMapInJar by lazy(LazyThreadSafetyMode.NONE) {
    RuntimeClass::class.java.protectionDomain.codeSource.location.getClasses()
}

/**
 * 当前插件的所有资源文件（在本体中）
 */
val runningResourcesInJar by lazy(LazyThreadSafetyMode.NONE) {
    RuntimeClass::class.java.protectionDomain.codeSource.location.getResources()
}

/**
 * 当前插件的所有类
 */
val runningClassMap: Map<String, Class<*>>
    get() {
        val map = LinkedHashMap<String, Class<*>>()
        map.putAll(runningClassMapInJar)
        return map
    }

/**
 * 当前插件的所有类（排除第三方库）
 */
val runningClassMapWithoutLibrary: Map<String, Class<*>>
    get() = runningClassMap.filterKeys { !it.contains(".library.") }

/**
 * 当前插件的所有类（排除第三方库）
 */
val runningTabooLibClassMap: Map<String, Class<*>>
    get() = runningClassMap.filterKeys { it.contains(".taboolib.") }

/**
 * 当前插件的所有类（排除匿名类、内部类）
 */
val runningExactClassMap: Map<String, Class<*>>
    get() = runningClassMap.filter { !it.key.contains('$') && it.key.substringAfterLast('$').toIntOrNull() == null }



/**
 * 取该类在当前项目中被加载的任何实例
 * 例如：@Awake 自唤醒类，或是 Kotlin Companion Object、Kotlin Object 对象
 *
 * @param newInstance 若无任何已加载的实例，是否实例化
 */
fun <T> Class<T>.getInstance(newInstance: Boolean = false): Supplier<T>? {
    // 反射获取实例字段
    return try {
        // 伴生类
        val instanceObj = if (simpleName == "Companion") {
            ReflexClass.of(classOf(name.substringBeforeLast('$')), false).getField("Companion", findToParent = false, remap = false)
        } else {
            ReflexClass.of(this, false).getField("INSTANCE", findToParent = false, remap = false)
        }
        sup { instanceObj.get() as T }
    } catch (ex: Throwable) {
       // ex.printStackTrace()
        when (ex) {
            // 忽略异常
            is ClassNotFoundException, is NoClassDefFoundError, is IllegalAccessError, is IncompatibleClassChangeError -> null
            // 未找到方法
            is NoSuchFieldException -> {
                if (Modifier.isStatic(modifiers)) {
                    return null
                } else {
                    if (newInstance) {
                        //println("新实例")
                        sup { getDeclaredConstructor().newInstance() as T }
                    } else null
                }

            }
            // 初始化错误 & 内部错误
            is ExceptionInInitializerError, is InternalError -> {
                if (ex.message != "Malformed class name") {
                    PrimitiveIO.println("Failed to get instance: $this")
                    ex.printStackTrace()
                }
                null
            }
            // 其他异常
            else -> throw ex
        }
    }
}

/**
 * 获取 URL 下的所有类
 */
fun URL.getClasses(classLoader: ClassLoader = ClassAppender.getClassLoader()): Map<String, Class<*>> {
    val classes = LinkedHashMap<String, Class<*>>()
    val srcFile = try {
        File(toURI())
    } catch (ex: IllegalArgumentException) {
        File((openConnection() as JarURLConnection).jarFileURL.toURI())
    } catch (ex: URISyntaxException) {
        File(path)
    }
    // 是文件
    if (srcFile.isFile) {
        JarFile(srcFile).stream().filter { it.name.endsWith(".class") }.forEach {
            val className = it.name.replace('/', '.').substringBeforeLast(".class")
            runCatching { classes[className] = Class.forName(className, false, classLoader) }
        }
    } else {
        srcFile.walkTopDown().filter { it.extension == "class" }.forEach {
            val className = it.path.substringAfter(srcFile.path).drop(1).replace('/', '.').substringBeforeLast(".class")
            runCatching { classes[className] = Class.forName(className, false, classLoader) }
        }
    }
    return classes
}

/**
 * 获取 URL 下的所有文件
 */
fun URL.getResources(): Map<String, ByteArray> {
    val resources = LinkedHashMap<String, ByteArray>()
    val srcFile = try {
        File(toURI())
    } catch (ex: IllegalArgumentException) {
        File((openConnection() as JarURLConnection).jarFileURL.toURI())
    } catch (ex: URISyntaxException) {
        File(path)
    }
    val jarFile = JarFile(srcFile)
    jarFile.stream().filter { !it.name.endsWith(".class") && !it.isDirectory }.forEach {
        resources[it.name] = jarFile.getInputStream(jarFile.getJarEntry(it.name)).readBytes()
    }
    return resources
}

private fun classOf(name: String): Class<*> {
    return Class.forName(name, false, ClassAppender.getClassLoader())
}

private fun <T> sup(supplier: () -> T): Supplier<T> {
    return object : Supplier<T> {

        val value by lazy(LazyThreadSafetyMode.NONE) { supplier() }

        override fun get(): T {
            return value
        }
    }
}
