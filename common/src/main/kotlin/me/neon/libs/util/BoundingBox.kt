package me.neon.libs.util

import org.apache.commons.lang.Validate
import org.bukkit.Location
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.util.RayTraceResult
import org.bukkit.util.Vector

/**
 * NeonLibs
 * me.neon.libs.util
 *
 * @author 老廖
 * @since 2024/5/3 12:38
 */
class BoundingBox(x1: Double, y1: Double, z1: Double, x2: Double, y2: Double, z2: Double) : Cloneable {

    companion object {

        fun of(corner1: Vector, corner2: Vector): BoundingBox {
            return BoundingBox(corner1.x, corner1.y, corner1.z, corner2.x, corner2.y, corner2.z)
        }

        fun of(corner1: Location, corner2: Location): BoundingBox {
            Validate.isTrue(corner1.world == corner2.world, "Locations from different worlds!")
            return BoundingBox(corner1.x, corner1.y, corner1.z, corner2.x, corner2.y, corner2.z)
        }

        fun of(corner1: Block, corner2: Block): BoundingBox {
            Validate.isTrue(corner1.world == corner2.world, "Blocks from different worlds!")
            val x1 = corner1.x
            val y1 = corner1.y
            val z1 = corner1.z
            val x2 = corner2.x
            val y2 = corner2.y
            val z2 = corner2.z
            val minX = x1.coerceAtMost(x2)
            val minY = y1.coerceAtMost(y2)
            val minZ = z1.coerceAtMost(z2)
            val maxX = x1.coerceAtLeast(x2) + 1
            val maxY = y1.coerceAtLeast(y2) + 1
            val maxZ = z1.coerceAtLeast(z2) + 1
            return BoundingBox(minX.toDouble(), minY.toDouble(), minZ.toDouble(), maxX.toDouble(), maxY.toDouble(), maxZ.toDouble())
        }

        fun of(block: Block): BoundingBox {
            return BoundingBox(
                block.x.toDouble(),
                block.y.toDouble(),
                block.z.toDouble(),
                (block.x + 1).toDouble(),
                (block.y + 1).toDouble(),
                (block.z + 1).toDouble()
            )
        }

        fun of(center: Vector, x: Double, y: Double, z: Double): BoundingBox {
            return BoundingBox(center.x - x, center.y - y, center.z - z, center.x + x, center.y + y, center.z + z)
        }

        fun of(center: Location, x: Double, y: Double, z: Double): BoundingBox {
            return BoundingBox(center.x - x, center.y - y, center.z - z, center.x + x, center.y + y, center.z + z)
        }

    }

    private var minX: Double = 0.0
    private var minY: Double = 0.0
    private var minZ: Double = 0.0
    private var maxX: Double = 0.0
    private var maxY: Double = 0.0
    private var maxZ: Double = 0.0

    init {
        resize(x1, y1, z1, x2, y2, z2)
    }

    fun resize(x1: Double, y1: Double, z1: Double, x2: Double, y2: Double, z2: Double): BoundingBox {
        minX = x1.coerceAtMost(x2)
        minY = y1.coerceAtMost(y2)
        minZ = z1.coerceAtMost(z2)
        maxX = x1.coerceAtLeast(x2)
        maxY = y1.coerceAtLeast(y2)
        maxZ = z1.coerceAtLeast(z2)
        return this
    }

    fun getMinX(): Double {
        return minX
    }

    fun getMinY(): Double {
        return minY
    }

    fun getMinZ(): Double {
        return minZ
    }

    fun getMin(): Vector {
        return Vector(minX, minY, minZ)
    }

    fun getMaxX(): Double {
        return maxX
    }

    fun getMaxY(): Double {
        return maxY
    }

    fun getMaxZ(): Double {
        return maxZ
    }

    fun getMax(): Vector {
        return Vector(maxX, maxY, maxZ)
    }

    fun getWidthX(): Double {
        return maxX - minX
    }

    fun getWidthZ(): Double {
        return maxZ - minZ
    }

    fun getHeight(): Double {
        return maxY - minY
    }

    fun getVolume(): Double {
        return getHeight() * getWidthX() * getWidthZ()
    }

    fun getCenterX(): Double {
        return minX + getWidthX() * 0.5
    }

    fun getCenterY(): Double {
        return minY + getHeight() * 0.5
    }

    fun getCenterZ(): Double {
        return minZ + getWidthZ() * 0.5
    }

    fun getCenter(): Vector {
        return Vector(getCenterX(), getCenterY(), getCenterZ())
    }

    fun copy(other: BoundingBox): BoundingBox {
        return resize(other.minX, other.minY, other.minZ, other.maxX, other.maxY, other.maxZ)
    }

    fun expand(
        negativeX: Double,
        negativeY: Double,
        negativeZ: Double,
        positiveX: Double,
        positiveY: Double,
        positiveZ: Double
    ): BoundingBox {
        return if (negativeX == 0.0 && negativeY == 0.0 && negativeZ == 0.0 && positiveX == 0.0 && positiveY == 0.0 && positiveZ == 0.0) {
            this
        } else {
            var newMinX = minX - negativeX
            var newMinY = minY - negativeY
            var newMinZ = minZ - negativeZ
            var newMaxX = maxX + positiveX
            var newMaxY = maxY + positiveY
            var newMaxZ = maxZ + positiveZ
            var centerZ: Double
            if (newMinX > newMaxX) {
                centerZ = getCenterX()
                if (newMaxX >= centerZ) {
                    newMinX = newMaxX
                } else if (newMinX <= centerZ) {
                    newMaxX = newMinX
                } else {
                    newMinX = centerZ
                    newMaxX = centerZ
                }
            }
            if (newMinY > newMaxY) {
                centerZ = getCenterY()
                if (newMaxY >= centerZ) {
                    newMinY = newMaxY
                } else if (newMinY <= centerZ) {
                    newMaxY = newMinY
                } else {
                    newMinY = centerZ
                    newMaxY = centerZ
                }
            }
            if (newMinZ > newMaxZ) {
                centerZ = getCenterZ()
                if (newMaxZ >= centerZ) {
                    newMinZ = newMaxZ
                } else if (newMinZ <= centerZ) {
                    newMaxZ = newMinZ
                } else {
                    newMinZ = centerZ
                    newMaxZ = centerZ
                }
            }
            resize(newMinX, newMinY, newMinZ, newMaxX, newMaxY, newMaxZ)
        }
    }

    fun expand(x: Double, y: Double, z: Double): BoundingBox {
        return this.expand(x, y, z, x, y, z)
    }

    fun expand(expansion: Vector): BoundingBox {
        val x = expansion.x
        val y = expansion.y
        val z = expansion.z
        return this.expand(x, y, z, x, y, z)
    }

    fun expand(expansion: Double): BoundingBox {
        return this.expand(expansion, expansion, expansion, expansion, expansion, expansion)
    }

    fun expand(dirX: Double, dirY: Double, dirZ: Double, expansion: Double): BoundingBox {
        return if (expansion == 0.0) {
            this
        } else if (dirX == 0.0 && dirY == 0.0 && dirZ == 0.0) {
            this
        } else {
            val negativeX = if (dirX < 0.0) -dirX * expansion else 0.0
            val negativeY = if (dirY < 0.0) -dirY * expansion else 0.0
            val negativeZ = if (dirZ < 0.0) -dirZ * expansion else 0.0
            val positiveX = if (dirX > 0.0) dirX * expansion else 0.0
            val positiveY = if (dirY > 0.0) dirY * expansion else 0.0
            val positiveZ = if (dirZ > 0.0) dirZ * expansion else 0.0
            this.expand(negativeX, negativeY, negativeZ, positiveX, positiveY, positiveZ)
        }
    }

    fun expand(direction: Vector, expansion: Double): BoundingBox {
        return this.expand(direction.x, direction.y, direction.z, expansion)
    }

    fun expand(blockFace: BlockFace, expansion: Double): BoundingBox {
        return if (blockFace == BlockFace.SELF) this else this.expand(blockFace.direction, expansion)
    }

    fun expandDirectional(dirX: Double, dirY: Double, dirZ: Double): BoundingBox {
        return this.expand(dirX, dirY, dirZ, 1.0)
    }

    fun expandDirectional(direction: Vector): BoundingBox {
        return this.expand(direction.x, direction.y, direction.z, 1.0)
    }

    fun union(posX: Double, posY: Double, posZ: Double): BoundingBox {
        val newMinX = minX.coerceAtMost(posX)
        val newMinY = minY.coerceAtMost(posY)
        val newMinZ = minZ.coerceAtMost(posZ)
        val newMaxX = maxX.coerceAtLeast(posX)
        val newMaxY = maxY.coerceAtLeast(posY)
        val newMaxZ = maxZ.coerceAtLeast(posZ)
        return if (newMinX == minX && newMinY == minY && newMinZ == minZ && newMaxX == maxX && newMaxY == maxY && newMaxZ == maxZ) {
            this
        } else resize(
            newMinX,
            newMinY,
            newMinZ,
            newMaxX,
            newMaxY,
            newMaxZ
        )
    }

    fun union(position: Vector): BoundingBox {
        return this.union(position.x, position.y, position.z)
    }

    fun union(position: Location): BoundingBox {
        return this.union(position.x, position.y, position.z)
    }

    fun union(other: BoundingBox): BoundingBox {
        return if (this.contains(other)) {
            this
        } else {
            val newMinX = minX.coerceAtMost(other.minX)
            val newMinY = minY.coerceAtMost(other.minY)
            val newMinZ = minZ.coerceAtMost(other.minZ)
            val newMaxX = maxX.coerceAtLeast(other.maxX)
            val newMaxY = maxY.coerceAtLeast(other.maxY)
            val newMaxZ = maxZ.coerceAtLeast(other.maxZ)
            resize(newMinX, newMinY, newMinZ, newMaxX, newMaxY, newMaxZ)
        }
    }

    fun intersection(other: BoundingBox): BoundingBox {
        Validate.notNull(other, "Other bounding box is null!")
        Validate.isTrue(this.overlaps(other), "The bounding boxes do not overlap!")
        val newMinX = minX.coerceAtLeast(other.minX)
        val newMinY = minY.coerceAtLeast(other.minY)
        val newMinZ = minZ.coerceAtLeast(other.minZ)
        val newMaxX = maxX.coerceAtMost(other.maxX)
        val newMaxY = maxY.coerceAtMost(other.maxY)
        val newMaxZ = maxZ.coerceAtMost(other.maxZ)
        return resize(newMinX, newMinY, newMinZ, newMaxX, newMaxY, newMaxZ)
    }

    fun shift(shiftX: Double, shiftY: Double, shiftZ: Double): BoundingBox {
        return if (shiftX == 0.0 && shiftY == 0.0 && shiftZ == 0.0) this
        else resize(
            minX + shiftX,
            minY + shiftY,
            minZ + shiftZ,
            maxX + shiftX,
            maxY + shiftY,
            maxZ + shiftZ
        )
    }

    fun shift(shift: Vector): BoundingBox {
        return this.shift(shift.x, shift.y, shift.z)
    }

    fun shift(shift: Location): BoundingBox {
        return this.shift(shift.x, shift.y, shift.z)
    }

    private fun overlaps(minX: Double, minY: Double, minZ: Double, maxX: Double, maxY: Double, maxZ: Double): Boolean {
        return this.minX < maxX && this.maxX > minX && this.minY < maxY && this.maxY > minY && this.minZ < maxZ && this.maxZ > minZ
    }

    fun overlaps(other: BoundingBox): Boolean {
        return this.overlaps(other.minX, other.minY, other.minZ, other.maxX, other.maxY, other.maxZ)
    }

    fun overlaps(min: Vector, max: Vector): Boolean {
        val x1 = min.x
        val y1 = min.y
        val z1 = min.z
        val x2 = max.x
        val y2 = max.y
        val z2 = max.z
        return this.overlaps(
            x1.coerceAtMost(x2),
            y1.coerceAtMost(y2),
            z1.coerceAtMost(z2),
            x1.coerceAtLeast(x2),
            y1.coerceAtLeast(y2),
            z1.coerceAtLeast(z2)
        )
    }

    fun contains(x: Double, y: Double, z: Double): Boolean {
        return x >= minX && x < maxX && y >= minY && y < maxY && z >= minZ && z < maxZ
    }

    operator fun contains(position: Vector): Boolean {
        return this.contains(position.x, position.y, position.z)
    }

    private fun contains(minX: Double, minY: Double, minZ: Double, maxX: Double, maxY: Double, maxZ: Double): Boolean {
        return this.minX <= minX && this.maxX >= maxX && this.minY <= minY && this.maxY >= maxY && this.minZ <= minZ && this.maxZ >= maxZ
    }

    operator fun contains(other: BoundingBox): Boolean {
        return this.contains(other.minX, other.minY, other.minZ, other.maxX, other.maxY, other.maxZ)
    }

    fun contains(min: Vector, max: Vector): Boolean {
        val x1 = min.x
        val y1 = min.y
        val z1 = min.z
        val x2 = max.x
        val y2 = max.y
        val z2 = max.z
        return this.contains(
            x1.coerceAtMost(x2),
            y1.coerceAtMost(y2),
            z1.coerceAtMost(z2),
            x1.coerceAtLeast(x2),
            y1.coerceAtLeast(y2),
            z1.coerceAtLeast(z2)
        )
    }

    fun rayTrace(start: Vector, direction: Vector, maxDistance: Double): RayTraceResult? {
        start.checkFinite()
        direction.checkFinite()
        Validate.isTrue(direction.lengthSquared() > 0.0, "Direction's magnitude is 0!")
        return if (maxDistance < 0.0) {
            null
        } else {
            val startX = start.x
            val startY = start.y
            val startZ = start.z
           // val dir = direction.clone().normalizeZeros().normalize()
            val dir = direction.clone().normalize()
            val dirX = dir.x
            val dirY = dir.y
            val dirZ = dir.z
            val divX = 1.0 / dirX
            val divY = 1.0 / dirY
            val divZ = 1.0 / dirZ
            var tMin: Double
            var tMax: Double
            var hitBlockFaceMin: BlockFace?
            var hitBlockFaceMax: BlockFace?
            if (dirX >= 0.0) {
                tMin = (minX - startX) * divX
                tMax = (maxX - startX) * divX
                hitBlockFaceMin = BlockFace.WEST
                hitBlockFaceMax = BlockFace.EAST
            } else {
                tMin = (maxX - startX) * divX
                tMax = (minX - startX) * divX
                hitBlockFaceMin = BlockFace.EAST
                hitBlockFaceMax = BlockFace.WEST
            }
            val tyMin: Double
            val tyMax: Double
            val hitBlockFaceYMin: BlockFace
            val hitBlockFaceYMax: BlockFace
            if (dirY >= 0.0) {
                tyMin = (minY - startY) * divY
                tyMax = (maxY - startY) * divY
                hitBlockFaceYMin = BlockFace.DOWN
                hitBlockFaceYMax = BlockFace.UP
            } else {
                tyMin = (maxY - startY) * divY
                tyMax = (minY - startY) * divY
                hitBlockFaceYMin = BlockFace.UP
                hitBlockFaceYMax = BlockFace.DOWN
            }
            if (tMin <= tyMax && tMax >= tyMin) {
                if (tyMin > tMin) {
                    tMin = tyMin
                    hitBlockFaceMin = hitBlockFaceYMin
                }
                if (tyMax < tMax) {
                    tMax = tyMax
                    hitBlockFaceMax = hitBlockFaceYMax
                }
                val tzMin: Double
                val tzMax: Double
                val hitBlockFaceZMin: BlockFace
                val hitBlockFaceZMax: BlockFace
                if (dirZ >= 0.0) {
                    tzMin = (minZ - startZ) * divZ
                    tzMax = (maxZ - startZ) * divZ
                    hitBlockFaceZMin = BlockFace.NORTH
                    hitBlockFaceZMax = BlockFace.SOUTH
                } else {
                    tzMin = (maxZ - startZ) * divZ
                    tzMax = (minZ - startZ) * divZ
                    hitBlockFaceZMin = BlockFace.SOUTH
                    hitBlockFaceZMax = BlockFace.NORTH
                }
                if (tMin <= tzMax && tMax >= tzMin) {
                    if (tzMin > tMin) {
                        tMin = tzMin
                        hitBlockFaceMin = hitBlockFaceZMin
                    }
                    if (tzMax < tMax) {
                        tMax = tzMax
                        hitBlockFaceMax = hitBlockFaceZMax
                    }
                    if (tMax < 0.0) {
                        null
                    } else if (tMin > maxDistance) {
                        null
                    } else {
                        val t: Double
                        val hitBlockFace: BlockFace
                        if (tMin < 0.0) {
                            t = tMax
                            hitBlockFace = hitBlockFaceMax
                        } else {
                            t = tMin
                            hitBlockFace = hitBlockFaceMin
                        }
                        val hitPosition = dir.multiply(t).add(start)
                        RayTraceResult(hitPosition, hitBlockFace)
                    }
                } else {
                    null
                }
            } else {
                null
            }
        }
    }


    override fun toString(): String {
        val builder = StringBuilder()
        builder.append("BoundingBox [minX=")
        builder.append(minX)
        builder.append(", minY=")
        builder.append(minY)
        builder.append(", minZ=")
        builder.append(minZ)
        builder.append(", maxX=")
        builder.append(maxX)
        builder.append(", maxY=")
        builder.append(maxY)
        builder.append(", maxZ=")
        builder.append(maxZ)
        builder.append("]")
        return builder.toString()
    }

    override fun clone(): BoundingBox {
        return try {
            super.clone() as BoundingBox
        } catch (var2: CloneNotSupportedException) {
            throw Error(var2)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as BoundingBox
        if (minX != other.minX
            || minY != other.minY
            || minZ != other.minZ
            || maxX != other.maxX
            || maxY != other.maxY
            || maxZ != other.maxZ) {
            return false
        }
        return true
    }

    override fun hashCode(): Int {
        var result = minX.hashCode()
        result = 31 * result + minY.hashCode()
        result = 31 * result + minZ.hashCode()
        result = 31 * result + maxX.hashCode()
        result = 31 * result + maxY.hashCode()
        result = 31 * result + maxZ.hashCode()
        return result
    }
}