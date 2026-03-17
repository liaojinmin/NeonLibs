package me.neon.libs.region

import me.neon.libs.taboolib.configuration.ConfigurationSection
import me.neon.libs.util.BoundingBox

/**
 * NeonLibs
 * me.neon.libs.region
 *
 * @author 老廖
 * @since 2025/12/26 19:45
 */
data class RegionRange(
    val uniqueId: String,
    val box: BoundingBox
) {

    companion object {

        fun ofBoundingBox(section: ConfigurationSection): BoundingBox {
            val minSec = section.getConfigurationSection("min-location")
                ?: error("Missing min-location section at $section")

            val maxSec = section.getConfigurationSection("max-location")
                ?: error("Missing max-location section at $section")
            val minX = minSec.getDouble("x")
            val minY = minSec.getDouble("y")
            val minZ = minSec.getDouble("z")

            val maxX = maxSec.getDouble("x")
            val maxY = maxSec.getDouble("y")
            val maxZ = maxSec.getDouble("z")

            return BoundingBox(
                kotlin.math.min(minX, maxX),
                kotlin.math.min(minY, maxY),
                kotlin.math.min(minZ, maxZ),
                kotlin.math.max(minX, maxX),
                kotlin.math.max(minY, maxY),
                kotlin.math.max(minZ, maxZ),
            )
        }
    }
}