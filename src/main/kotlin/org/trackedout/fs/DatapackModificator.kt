package org.trackedout.fs

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import org.trackedout.fullRunType
import java.io.File
import java.io.FileNotFoundException
import java.nio.file.Files
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

internal val json = Json { allowStructuredMapKeys = true }
internal val logger = LoggerFactory.getLogger("Agronet")!!

internal inline fun <reified T> modifyJsonInZip(zipPath: String, jsonFileName: String, modify: (T) -> Unit) {
    val tempDir = Files.createTempDirectory("zip_temp").toFile()

    // Extract all files from the zip
    ZipFile(zipPath).use { zipFile ->
        zipFile.entries().asSequence().forEach { entry ->
            val outputFile = File(tempDir, entry.name)
            if (entry.isDirectory) {
                outputFile.mkdirs()
            } else {
                outputFile.parentFile.mkdirs()
                zipFile.getInputStream(entry).use { input ->
                    outputFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
            }
        }
    }

    // Modify the JSON file
    val jsonFile = File(tempDir, jsonFileName)
    if (jsonFile.exists()) {
        val wardenCanListen: T = json.decodeFromString(jsonFile.readText())
        logger.info("Original JSON content: $wardenCanListen")
        modify(wardenCanListen)
        logger.info("Modified JSON content: $wardenCanListen")
        jsonFile.writeText(Json.encodeToString(wardenCanListen))
    } else {
        throw FileNotFoundException("JSON file $jsonFileName not found in the zip.")
    }

    // Create a new zip file
    val newZipPath = zipPath//.replace(".zip", "_modified.zip")
    File(newZipPath).delete()
    ZipOutputStream(File(newZipPath).outputStream()).use { zipOut ->
        tempDir.walkTopDown().forEach { file ->
            if (!file.isDirectory) {
                val relativePath = tempDir.toPath().relativize(file.toPath()).toString()
                zipOut.putNextEntry(ZipEntry(relativePath))
                file.inputStream().use { input ->
                    input.copyTo(zipOut)
                }
                zipOut.closeEntry()
            }
        }
    }

    // Clean up temporary files
    tempDir.deleteRecursively()
    logger.info("Modified zip file created at: $newZipPath")
}

fun replaceFilesInZip(zipPath: String, runType: String) {
    val tempDir = Files.createTempDirectory("zip_temp").toFile()
    // ./replacements/$runType/ is a sub-dir of $zip_temp after extraction
    val replacementsDir = File(tempDir, "replacements/$runType")
    logger.info("Unpacking zip to ${tempDir}, and applying replacements from $replacementsDir to $zipPath")

    // Extract all files
    ZipFile(zipPath).use { zipFile ->
        zipFile.entries().asSequence().forEach { entry ->
            val outputFile = File(tempDir, entry.name)
            if (entry.isDirectory) {
                outputFile.mkdirs()
            } else {
                outputFile.parentFile.mkdirs()
                zipFile.getInputStream(entry).use { input ->
                    outputFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
            }
        }
    }

    // Apply replacements
    replacementsDir.walkTopDown().forEach { replacementFile ->
        if (!replacementFile.isDirectory) {
            val relativePath = replacementsDir.toPath().relativize(replacementFile.toPath()).toString()
            val targetFile = File(tempDir, relativePath)

            logger.info("Copying $replacementFile to $targetFile")
            targetFile.parentFile.mkdirs()
            replacementFile.copyTo(targetFile, overwrite = true)
        }
    }

    // Replace the zip file with a new copy
    File(zipPath).delete()
    ZipOutputStream(File(zipPath).outputStream()).use { zipOut ->
        tempDir.walkTopDown().forEach { file ->
            if (!file.isDirectory) {
                val relativePath = tempDir.toPath().relativize(file.toPath()).toString()
                val zipEntry = ZipEntry(relativePath)
                zipEntry.time = file.lastModified()
                zipOut.putNextEntry(zipEntry)
                file.inputStream().use { input ->
                    input.copyTo(zipOut)
                }
                zipOut.closeEntry()
            }
        }
    }

    // Clean up temporary files
    tempDir.deleteRecursively()
    logger.info("Modified zip file created at: $zipPath")
}

internal fun applyDatapackReplacements(runTypeShort: String) {
    val runType = runTypeShort.fullRunType().lowercase()
    logger.info("Preparing datapack for run type: $runType")

    val zipPath = "world/datapacks/brilliance-datapack.zip"
    replaceFilesInZip(zipPath, runType)

//    val wardenCanListenFileName = "data/minecraft/tags/game_event_DISABLED/warden_can_listen.json"
//    modifyJsonInZip(zipPath, wardenCanListenFileName) { wardenCanListen: WardenCanListen ->
//        // Modify the JSON content
//        wardenCanListen.values = wardenCanListen.values.filter { it != "minecraft:entity_mount" }
//    }
//
//    val vibrationsFileName = "data/minecraft/tags/game_event_DISABLED/warden_can_listen.json"
//    modifyJsonInZip(zipPath, vibrationsFileName) { wardenCanListen: WardenCanListen ->
//        // Modify the JSON content
//        wardenCanListen.values = wardenCanListen.values.filter { it != "minecraft:entity_mount" }
//    }
}
