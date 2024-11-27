package me.neon.libs.util

import com.google.gson.*
import com.google.gson.internal.Streams
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.MalformedJsonException
import java.io.IOException
import java.io.Reader
import java.io.StringReader

/**
 * NeonLibs
 * me.neon.libs.util
 *
 * @author 老廖
 * @since 2024/10/21 18:50
 */

fun String.parseString(): JsonElement {
    return StringReader(this).parseReader()
}

fun Reader.parseReader(): JsonElement {
    try {
        val jsonReader = JsonReader(this)
        val element = JsonParser.parseReader(jsonReader)
        if (!element.isJsonNull && jsonReader.peek() != JsonToken.END_DOCUMENT) {
            throw JsonSyntaxException("Did not consume the entire document.")
        }
        return element
    } catch (e: MalformedJsonException) {
        throw JsonSyntaxException(e)
    } catch (e: IOException) {
        throw JsonIOException(e)
    } catch (e: NumberFormatException) {
        throw JsonSyntaxException(e)
    }
}


fun JsonReader.parseReader(): JsonElement {
    val lenient = this.isLenient
    this.isLenient = true
    try {
        return Streams.parse(this)
    } catch (e: StackOverflowError) {
        throw JsonParseException("Failed parsing JSON source: $this to Json", e)
    } catch (e: OutOfMemoryError) {
        throw JsonParseException("Failed parsing JSON source: $this to Json", e)
    } finally {
        this.isLenient = lenient
    }
}