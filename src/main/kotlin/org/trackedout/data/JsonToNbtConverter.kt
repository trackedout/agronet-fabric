package org.trackedout.data

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import net.minecraft.nbt.NbtByte
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtInt
import net.minecraft.nbt.NbtList
import net.minecraft.nbt.NbtShort
import net.minecraft.nbt.NbtString

object JsonToNbtConverter {
    fun fromJson(json: String): NbtCompound {
        val jsonObject: JsonObject = JsonParser.parseString(json).getAsJsonObject()
        return parseJsonObject(jsonObject)
    }

    private fun parseJsonObject(jsonObject: JsonObject): NbtCompound {
        val nbt = NbtCompound()
        for (entry in jsonObject.entrySet()) {
            val key: String? = entry.key
            val value: com.google.gson.JsonElement = entry.value

            if (value.isJsonObject) {
                nbt.put(key, parseJsonObject(value.getAsJsonObject()))
            } else if (value.isJsonArray) {
                nbt.put(key, parseJsonArray(value.getAsJsonArray()))
            } else if (value.isJsonPrimitive) {
                nbt.put(key, parseJsonPrimitive(value.getAsJsonPrimitive()))
            }
        }
        return nbt
    }

    private fun parseJsonArray(jsonArray: com.google.gson.JsonArray): NbtElement {
        val nbtList = NbtList()
        for (element in jsonArray) {
            if (element.isJsonObject) {
                nbtList.add(parseJsonObject(element.getAsJsonObject()))
            } else if (element.isJsonPrimitive) {
                nbtList.add(parseJsonPrimitive(element.getAsJsonPrimitive()))
            }
        }
        return nbtList
    }

    private fun parseJsonPrimitive(primitive: com.google.gson.JsonPrimitive): NbtElement? {
        return if (primitive.isNumber) {
            NbtInt.of(primitive.asInt)
        } else if (primitive.isBoolean) {
            NbtByte.of((if (primitive.getAsBoolean()) 1 else 0).toByte())
        } else {
            return if (primitive.asString.endsWith("b")) { // Handle `1b`, `0b`
                NbtByte.of(primitive.asString.removeSuffix("b").toByte())
            } else if (primitive.asString.endsWith("s")) { // Handle `123s`
                NbtShort.of(primitive.asString.removeSuffix("s").toShort())
            } else {
                NbtString.of(primitive.asString)
            }
        }
    }
}
