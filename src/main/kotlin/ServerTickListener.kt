import com.google.common.collect.Maps
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.minecraft.scoreboard.ScoreboardObjective
import net.minecraft.scoreboard.ScoreboardPlayerScore
import net.minecraft.server.MinecraftServer
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentMap

class ServerTickListener : ServerTickEvents.EndTick {
    private val logger = LoggerFactory.getLogger("agronet")
    private val lastKnownObjectives: ConcurrentMap<String, Map<ScoreboardObjective, ScoreboardPlayerScore>> = Maps.newConcurrentMap()


    override fun onEndTick(minecraftServer: MinecraftServer) {
        val sc = minecraftServer.scoreboard

        for (s in minecraftServer.playerNames) {
            val playerObjectives = sc.getPlayerObjectives(s)
            if (lastKnownObjectives[s].hashCode() != playerObjectives.hashCode()) {
                logger.info("Objectives for $s changed!")
                lastKnownObjectives[s] = playerObjectives
            }
        }
    }
}