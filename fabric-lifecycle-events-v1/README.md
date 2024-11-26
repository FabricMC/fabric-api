# Fabric Lifecycle Events (V1)

Contains events which indicate when the lifecycle of a Minecraft client, Minecraft server and the lifecycle objects which exist on the client and server has changed.

## Organization

Events in this module belong to a single class which relates to the category of events.
The event callbacks to be implemented are nested classes within.

For example, if you are listening to `ServerLifecycleEvents.SERVER_STARTING` in `ServerLifecycleEvents` the callback interface that would be implemented is `ServerLifecycleEvents.ServerStarting`.

# Server Events

Events related to objects on a logical server.
These classes are located in `net.fabricmc.fabric.api.event.lifecycle.v1`.

## `ServerLifecycleEvents`

Contains lifecycle events related to a Minecraft server.
This includes events which indicate when a server is starting up, is in a data pack reload or shutting down.

## `ServerTickEvents`

Contains events related to the ticking of a Minecraft server.
There are events that indicate the beginning and end of the tick for the server and each `ServerWorld`.

## `ServerWorldEvents`

Events related to the lifecycle a `ServerWorld`.
Currently, this contains events related to loading and unloading `ServerWorld`s.

## `ServerChunkEvents`

Events related to the lifecycle of chunks in a `ServerWorld`.
Currently, this contains events related to loading and unloading chunks in a `ServerWorld`.

## `ServerEntityEvents`

Events related to the lifecycle of entities in a `ServerWorld`.
Currently, this only contains an event for an entity being loaded into a `ServerWorld`.
The unload event has not been implemented yet.

## `ServerBlockEntityEvents`

Events related to the lifecycle of block entities in a `ServerWorld`.
Currently, this contains events related to loading and unloading block entities in a `ServerWorld`.

# Client Events

Events related to objects on a logical client.
These classes are located in `net.fabricmc.fabric.api.client.event.lifecycle.v1`.

**Note: These events are only available on a client. Trying to access these events on a dedicated server will cause the game to crash.**

## `ClientLifecycleEvents`

Events related to the lifecycle of a Minecraft client.
Currently, this contains events related to when the Minecraft Client is starting or stopping.

## `ClientTickEvents`

Events related to ticking of a Minecraft client.
There are events that indicate the beginning and end of the tick for the client and the `ClientWorld` if in game.

## `ClientWorldEvents`

Events related to the lifecycle a `ClientWorld`.
Currently, this contains an event which is called after `ClientWorld` has been changed.

## `ClientChunkEvents`

Events related to the lifecycle of chunks on a Minecraft client.
Currently, this contains events related to loading and unloading chunks in a `ClientWorld`.

## `ClientEntityEvents`

Events related to the lifecycle of entities in a `ClientWorld`.
Currently, this contains event for an entity being loaded into a `ClientWorld`.

## `ClientBlockEntityEvents`

Events related to the lifecycle of block entities in a `ClientWorld`.
Currently, this contains events related to loading and unloading block entities in a `ClientWorld`.
