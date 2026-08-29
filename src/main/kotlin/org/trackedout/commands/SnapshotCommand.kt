package org.trackedout.commands

import com.mojang.brigadier.context.CommandContext
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import org.trackedout.Agronet.runAsyncTask
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * `/create-snapshot` — captures the current sandbox world into the per-league restic repo.
 *
 * Only valid on sandbox servers (slots s400–s409). Performs a native, race-safe
 * save on the server thread (waiting for any in-flight save to finish, then a
 * flushing [saveAll] plus player data) before shelling out to `restic backup`,
 * so the on-disk world is consistent when it is read.
 *
 * The world lives on EFS at /efs/<slug>/world (symlinked from /server/world).
 * We back it up by cd-ing into /efs/<slug> and backing up the relative path
 * "world", which matches how the entrypoint restores
 * (`restic restore latest --target /efs/<slug>`).
 */
class SnapshotCommand(
    private val serverName: String,
    private val leagueSlug: String,
) : PlayerCommand {

    override fun run(context: CommandContext<ServerCommandSource>): Int {
        val source = context.source

        if (!SANDBOX_SLOT.matches(serverName)) {
            source.sendFeedback(
                { Text.literal("Snapshots are only available on sandbox servers").formatted(Formatting.RED) },
                false,
            )
            return 0
        }

        val server = source.server
        source.sendFeedback({ Text.literal("Creating world snapshot…").formatted(Formatting.YELLOW) }, true)

        runAsyncTask {
            try {
                // 1. Wait for any in-flight save to finish so we don't race the tick loop.
                var waited = 0
                while (server.isSaving && waited < SAVE_WAIT_TIMEOUT_MS) {
                    Thread.sleep(SAVE_POLL_INTERVAL_MS)
                    waited += SAVE_POLL_INTERVAL_MS.toInt()
                }

                // 2. Flush a full save on the server thread and wait for it to complete.
                //    force=true saves every world even if saving is disabled; flush=true
                //    blocks until all chunks are written to disk.
                server.submit<Boolean> {
                    val saved = server.saveAll(/* suppressLogs = */ true, /* flush = */ true, /* force = */ true)
                    server.playerManager.saveAllPlayerData()
                    saved
                }.get(SAVE_TIMEOUT_SECONDS, TimeUnit.SECONDS)

                // 3. Back up the now-consistent world via restic (RESTIC_REPOSITORY /
                //    RESTIC_PASSWORD / AWS role creds are inherited from the entrypoint).
                val leagueDir = "/efs/$leagueSlug"
                val process = ProcessBuilder("restic", "backup", "--tag", "snapshot", "world")
                    .directory(File(leagueDir))
                    .redirectErrorStream(true)
                    .start()

                val output = process.inputStream.bufferedReader().use { it.readText() }
                val finished = process.waitFor(RESTIC_TIMEOUT_SECONDS, TimeUnit.SECONDS)

                if (!finished) {
                    process.destroyForcibly()
                    throw IllegalStateException("restic backup timed out after ${RESTIC_TIMEOUT_SECONDS}s")
                }
                if (process.exitValue() != 0) {
                    throw IllegalStateException("restic backup failed (exit ${process.exitValue()}):\n$output")
                }

                logger.info("Snapshot created for league $leagueSlug:\n$output")
                source.sendFeedback(
                    { Text.literal("Snapshot saved for $leagueSlug").formatted(Formatting.GREEN) },
                    true,
                )
            } catch (e: Exception) {
                logger.error("Failed to create snapshot for league $leagueSlug", e)
                source.sendFeedback(
                    { Text.literal("Snapshot failed: ${e.message}").formatted(Formatting.RED) },
                    false,
                )
            }
        }

        return 1
    }

    companion object {
        private val SANDBOX_SLOT = Regex("^s4\\d\\d$")
        private const val SAVE_POLL_INTERVAL_MS = 100L
        private const val SAVE_WAIT_TIMEOUT_MS = 30_000
        private const val SAVE_TIMEOUT_SECONDS = 60L
        private const val RESTIC_TIMEOUT_SECONDS = 300L
    }
}
