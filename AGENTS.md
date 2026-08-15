# Agronet Fabric

A Fabric mod (Kotlin/Gradle) that runs on Decked Out 2 dungeon instances on the Tracked Out server. Proxies in-game events to the dunga-dunga backend API, manages deck inventory, tracks player actions (crafting, inventory interactions, deaths), syncs scores, and handles task execution from the backend.

## Project Structure

```
src/main/kotlin/org/trackedout/
├── Agronet.kt                  # Mod entry point (ModInitializer), registers all commands/events
├── EventHandlers.kt            # Event handler utilities
├── EventsApiWithContext.kt     # Wrapper around EventsApi with server context
├── Extensions.kt               # Player/command source extension functions
├── RunContext.kt               # Global run state (serverName, dungeonType, gameStarted, etc.)
├── TaskManagement.kt           # Polls and executes tasks from dunga-dunga API
├── actions/                    # Deck/inventory actions (give/take shulker, dungeon items)
├── commands/                   # Slash commands (/log-event, /card-bought, /card-played, etc.)
├── data/                       # Data classes (cards, scoreboards, run types, Brilliance JSON)
├── listeners/                  # Event listeners (player connect, crafting, inventory interaction)
├── mixin/                      # Mixin stubs (Kotlin, delegates to listeners)
├── scoreboard/                 # Score syncing with dunga-dunga
├── client/                     # Generated API client (dunga-dunga REST API)
└── fs/                         # Filesystem utilities (datapack, server manager)

src/main/java/org/trackedout/mixin/
└── CraftingScreenHandlerMixin.java  # Java mixin (required for Loom AP refmap generation)
```

## Build & Deploy

Build and deploy to dungeon servers (from `~/tracked-out/davybones`, not this directory):
```bash
KUBECONTEXT=burn just build-mods
```

This runs `copy-jars.sh --build` which builds agronet-fabric via `./gradlew build` and copies `build/libs/agronet-1.0.0.jar` to `server-resources/mods/dungeon-dev/`.

Unlike Citadel (which hot-reloads via plugman), agronet is a Fabric mod — deploying requires restarting the dungeon pod. There is no reload command. After `build-mods`, sync configs and restart pods:
```bash
cd ~/tracked-out/davybones
export KUBECONTEXT=burn
just build-mods && just sync-configs && just terminate-dungeon-pods
```

Wait for the dungeon pod to come back, then verify it started cleanly:
```bash
kubectl --context burn -n davybones logs dungeon-0 --tail=50 | grep -iE "agronet|error|Done"
```

You should see `Agronet server name: d800` and `Agronet online. Flee with extra flee!` with no errors.

After verifying, commit changes with a descriptive message. Stage only the files you changed — don't use `git add .`. Use imperative mood, sentence case, no prefix, no trailing period (e.g. "Fix shift-click crafting log showing air instead of item").

## Server Architecture

The dungeon server gets its identity from the `SERVER_NAME` env var (defaults to hostname). Names like `dungeon-0` are converted to `d800`, `dungeon-1` to `d801`, etc. The `DUNGEON_TYPE` is fetched from the dunga-dunga API config at startup (e.g. `season-2`), not from an env var.

The server sends `server-online` heartbeat events every 15 seconds with the player count and `dungeon-type` in metadata. The API uses these to track available dungeons.

Player lifecycle on the dungeon:
1. API assigns player to dungeon → creates claim in `assigned` state
2. `prepare-for-player` task is sent to the dungeon server
3. Dungeon loads datapack, sets up world → notifies API preparation is complete
4. Player connects → `AgronetPlayerConnectionListener.onPlayReady` fetches claim, sets it to `in-use`, loads scoreboards/advancements, gives shulker
5. Player disconnects → `onPlayDisconnect` sends updated player count and syncs scoreboards
6. The Velocity proxy (not agronet) sends `left-network` to the API, which invalidates the claim and clears queue state

## Local Dev

Start a local Minecraft server with the mod loaded:
```bash
./gradlew runServer
```

The server runs on port 25565. The `run/` directory contains server config. EULA is already accepted.

To simulate a dungeon server that the API recognizes, set `SERVER_NAME`:
```bash
SERVER_NAME=dungeon-1 ./gradlew runServer
```
This registers as `d801` and sends `server-online` events the API will recognize.

### Server Properties for Local Dev

Edit `run/server.properties` to enable offline-mode players and command blocks:
```properties
online-mode=false
enable-command-block=true
```

### Fake Player Testing

Connect a fake player using the mineflayer bot:
```bash
cd ~/tracked-out/IrdidiumStresser
node gamer.js [PlayerName]    # defaults to "Nef"
```

The bot connects with offline auth to `192.168.88.114:25565`. For local dev, edit `gamer.js` and change `host` to `"localhost"`.

On spawn, the bot runs `just queue-fake-player <name>` (from davybones with `KUBECONTEXT=burn`) which sets `skip-door` and sends join events to the dunga-dunga API. This requires the API to be reachable — use `KUBECONTEXT=burn just api-port-forward` if needed.

### Full Game Test (on cluster)

Local dev has limitations (no proxy routing, no dungeon map). For a full game test, deploy to the cluster and use the fake player flow:

```bash
# 1. Deploy
cd ~/tracked-out/davybones && export KUBECONTEXT=burn
just build-mods && just sync-configs && just terminate-dungeon-pods

# 2. Wait for pod, then queue player (dungeon type must match the server's config)
just queue-fake-player Nef season-2

# 3. Connect fake player (goes through proxy → dungeon)
cd ~/tracked-out/IrdidiumStresser && node gamer.js Nef
```

If the player gets stuck in `in-queue` state (e.g. from a previous failed attempt), reset first:
```bash
just reset-fake-player Nef
just queue-fake-player Nef season-2
```

### Queue State Gotchas

- The player queue state and claim state are separate. A claim can be `invalid` while the player is still `in-queue`.
- Only `left-network` from `server: "velocity"` (the proxy) clears queue state. Sending it from `"lobby"` does nothing.
- `reset-fake-player` sends the correct `left-network` from velocity to clear the queue.
- `queue-fake-player` accepts an optional dungeon type (e.g. `season-2`). The dungeon type must match what the target dungeon server reports in its `server-online` events, or the API won't assign the player to it.
- After a player disconnects, the dungeon stays reserved for about a minute before the API marks it available and shuts it down if empty.

### Useful davybones Commands

All run from `~/tracked-out/davybones` with `KUBECONTEXT=burn`:
```bash
just queue-fake-player <name> [dungeon-type]  # Queue player (e.g. just queue-fake-player Nef season-2)
just reset-fake-player <name>                 # Clear stuck queue state
just prepare-for-player <player> [server]     # Prepare dungeon (default: d800)
just send-event <player> <event> [server] [count] [run-id] [run-type] [dungeon-type]
just api-port-forward                         # Port-forward to dunga-dunga API
just mongo-port-forward                       # Port-forward to MongoDB
```

## Key Dependencies

- **Fabric API** 0.91.0+1.20.1 — Mod framework for Minecraft 1.20.1
- **Fabric Language Kotlin** — Kotlin support for Fabric mods
- **Dunga Dunga API** (OkHttp/Moshi) — Generated REST client for events, inventory, scores, tasks
- **Jedis** — Redis client for cross-server messaging
- **Mixin** — Bytecode injection for hooking into Minecraft internals
- **fabric-permissions-api** — Permission checking (LuckPerms integration)

## Mixins

Mixins must be written in Java (not Kotlin) in `src/main/java/org/trackedout/mixin/`. Loom's annotation processor only generates the refmap for Java sources. Kotlin mixins will compile but fail at runtime with "No refMap loaded".

Register mixins in `src/main/resources/agronet.mixins.json`. The refmap is `agronet.refmap.json`, generated at build time.

Pattern: Java mixin captures the hook, delegates to a Kotlin singleton listener (e.g. `CraftingScreenHandlerMixin.java` → `CraftingListener.kt`).

## Conventions

- Dungeon-only features go inside the `if (!serverName.startsWith("builders"))` block in `Agronet.kt`
- Player tags: `player.commandTags.contains("do2.staff")`, `player.commandTags.contains("do2.spectating")`
- Async work: `Agronet.runAsyncTask { }` (coroutine on Dispatchers.IO)
- Events: `eventsApi.eventsPost(Event(name=..., player=..., x=..., y=..., z=..., count=..., metadata=...))`
- Player messages: `player.sendMessage(message, Formatting.RED)` (extension from Extensions.kt)
- Block names: `block.translationKey.removePrefix("block.minecraft.")`
