package me.neon.libs.script

import com.google.gson.GsonBuilder
import java.util.concurrent.ThreadLocalRandom
import kotlin.random.Random
import kotlin.random.nextInt

/**
 * NeonLibs
 * me.neon.libs.api.script
 *
 * @author 老廖
 * @since 2024/2/21 22:10
 */
class RandomUtils {

    private val gson = GsonBuilder().create()

    private fun random(): java.util.Random {
        return ThreadLocalRandom.current()
    }

    fun randomDouble(): Double {
        return random().nextDouble()
    }

    fun int(min: Int, max: Int): Int {
        return Random.nextInt(min..max)
    }

    fun double(min: Double): Double {
        return Random.nextDouble(min)
    }

    fun double(min: Double, max: Double): Double {
        val value = Random.nextDouble(min)
        return if (value > max) max else value
    }

}