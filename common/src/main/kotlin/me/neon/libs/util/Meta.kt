
package me.neon.libs.util

import me.neon.libs.NeonLibsLoader
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.metadata.MetadataValue
import org.bukkit.metadata.Metadatable

fun Metadatable.setMeta(key: String, value: Any) {
    setMetadata(key, FixedMetadataValue(NeonLibsLoader.getInstance(), value))
}

fun Metadatable.hasMeta(key: String): Boolean {
    return getMetaFirstOrNull(key) != null
}

fun Metadatable.getMeta(key: String): List<MetadataValue> {
    return getMetadata(key)
}

fun Metadatable.getMetaFirst(key: String): MetadataValue {
    return getMetadata(key).first()
}

fun Metadatable.getMetaFirstOrNull(key: String): MetadataValue? {
    return getMetadata(key).firstOrNull()
}

fun Metadatable.removeMeta(key: String) {
    removeMetadata(key, NeonLibsLoader.getInstance())
}

inline fun <reified T> MetadataValue.cast(): T {
    return value() as T
}

inline fun <reified T> MetadataValue.castOrNull(): T? {
    return value() as? T
}