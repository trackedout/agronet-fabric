package org.trackedout.commands

import com.mojang.brigadier.context.CommandContext
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import org.trackedout.Agronet.runAsyncTask
import org.trackedout.fs.enforceEditGamerules
import java.io.File
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

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

    private val snapshotInProgress = AtomicBoolean(false)

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

        // Only one snapshot at a time: a second run would race the same world
        // files on EFS and could push an inconsistent backup. Reject overlapping
        // invocations instead of queuing them.
        if (!snapshotInProgress.compareAndSet(false, true)) {
            source.sendFeedback(
                { Text.literal("A snapshot is already in progress, please wait for it to finish").formatted(Formatting.RED) },
                false,
            )
            return 0
        }

        source.sendFeedback({ Text.literal("Snapshot: saving the world to disk…").formatted(Formatting.YELLOW) }, true)

        // The datapack may have disabled command feedback; make sure the player
        // can see this command's own output.
        server.execute { server.enforceEditGamerules() }

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

                source.sendFeedback(
                    { Text.literal("Snapshot: world saved, uploading backup (this can take a minute or two)…").formatted(Formatting.YELLOW) },
                    true,
                )

                // 3. Back up the now-consistent world via restic (RESTIC_REPOSITORY /
                //    RESTIC_PASSWORD / AWS role creds are inherited from the entrypoint).
                val leagueDir = "/efs/$leagueSlug"
                val process = ProcessBuilder("restic", "backup", "--tag", "snapshot", "world")
                    .directory(File(leagueDir))
                    .redirectErrorStream(true)
                    .start()

                // The upload is the long phase. Emit a periodic heartbeat so the player
                // knows the command is still working rather than staring at a blank chat.
                val heartbeat = startHeartbeat(source)

                val output: String
                val finished: Boolean
                try {
                    output = process.inputStream.bufferedReader().use { it.readText() }
                    finished = process.waitFor(RESTIC_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                } finally {
                    heartbeat.interrupt()
                }

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
            } finally {
                snapshotInProgress.set(false)
            }
        }

        return 1
    }

    /**
     * Emits a "still uploading" message to the command source every
     * [HEARTBEAT_INTERVAL_MS] until interrupted, so a long restic backup never
     * leaves the player looking at a silent chat. Returns the thread so the
     * caller can [Thread.interrupt] it once the backup finishes.
     */
    private fun startHeartbeat(source: ServerCommandSource): Thread {
        val thread = Thread {
            var elapsedSeconds = 0
            try {
                while (!Thread.currentThread().isInterrupted) {
                    Thread.sleep(HEARTBEAT_INTERVAL_MS)
                    elapsedSeconds += (HEARTBEAT_INTERVAL_MS / 1000).toInt()
                    source.sendFeedback(
                        { Text.literal("Snapshot: still uploading… (${elapsedSeconds}s)").formatted(Formatting.GRAY) },
                        false,
                    )
                }
            } catch (_: InterruptedException) {
                // Backup finished; stop quietly.
            }
        }
        thread.isDaemon = true
        thread.start()
        return thread
    }

    companion object {
        private val SANDBOX_SLOT = Regex("^s4\\d\\d$")
        private const val SAVE_POLL_INTERVAL_MS = 100L
        private const val SAVE_WAIT_TIMEOUT_MS = 30_000
        private const val SAVE_TIMEOUT_SECONDS = 60L
        private const val RESTIC_TIMEOUT_SECONDS = 300L
        private const val HEARTBEAT_INTERVAL_MS = 15_000L
    }
}
