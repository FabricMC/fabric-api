# Fabric Mod Protocol API (v1)

Mod Protocol is an additional syncing system allowing mods to easily prevent players from connecting with
incompatible or missing mod versions, when they are fully required. It also provides a way of checking the remote
version of mods that opted it into this system.

## Defining a mod protocol
### In *fabric.mod.json* (recommended)
This is the simplest way to define it. Can be useful when you don't need to change it depending on mods configuration
or external dependencies. It is set within custom field of that file under "fabric:mod_protocol_v1" key.

It can be defined in multiple ways:
- The inline definition. This will automatically use mods id with "mod" namespace as the protocol identifier. 
  It makes it so the mod is required on both client and server to match provided version predicate.
  The predicate itself uses the same format as dependency declarations.

  Example usage: `"fabric:mod_protocol_v1": "1.2.x"`
- Full object definition. All fields are optional, defaulting to existing properties in fabric.mods.json or in case of require to allow all   
    - `id` is the protocols identifier, which can have any namespace and path, as long as it's valid.
      It is optional and defaults to an ID with "mod" namespace and path equal to mod's id.
    - `name` is a name displayed if protocol doesn't match. It's optional and by default it uses one from mod's metadata.   
    - `version` is a version displayed if protocol doesn't match. It's optional and by default it uses one from mod's metadata.   
    - `require` controls the version requirement for both client and server, setting this is equal to setting both `require_client` and `require_server` 
    - `require_client` controls the required versions for the clients, defaults to allowing all connections   
    - `require_server` controls the required versions for the servers, defaults to allowing all connections   
  
  Example usage:
  ```
  "fabric:mod_protocol_v1": {
    "id": "custom:id",
    "name": "Mod Name",
    "version": "1.2.3",
    "require": "1.2.x",
    "require_client": ">=1.2.3",
    "require_server": "~1.2.0"
  }
  ```
- Array of full objects. Allows to define multiple versions of the protocol. The inner objects use the same format as single-full object format,
  with main exception being that fields `id`, `name` and `version` aren't defaulted and need to be always set

  Example usage:
  ```
  "fabric:mod_protocol_v1": [{
    "id": "custom:id",
    "name": "Mod Name",
    "version": "1.2.3",
    "require": "1.2.x",
    "require_client": ">=1.2.3",
    "require_server": "~1.2.0"
  }, {
    "id": "custom:another_one",
    "name": "Mod Name Experimental",
    "version": "0.5.1",
    "require": "0.5.1"
  }]
  ```
### In code.
The api provides a `ModProtocolRegistry` class which contains both registration and lookup methods for Mod Protocol definitions.
Main usage for this is when you need to define protocols conditionally depending on some runtime / config state.

You can also look up remote protocol version (of connected client and/or server) by using methods provided by `RemoteModVersionLookup`

## Networking definition / Extended usage.
This section is primary intended for usage outside of regular Fabric servers, where you might need to define
try to match players modpack against a server.

### The base protocol.
Mod Protocol handling is done in the Configuration networking stage after supported custom payload are synced,
but before any of the Fabric's Registry Synchronization and further vanilla handling. The protocol consists of
two custom payload packets:

- Clientbound `fabric:mod_protocol/request/v1` - Contains list of server protocols with requirements and a boolean indicating
  if client should automatically disconnect in case of a mismatch (always true for Fabric servers).
  Do note that in case of a matching failure, the client will still disconnect if disconnect on failure is set to false,
  just at a later point instead!
  
  Format of the payload looks as follows:
  - Number of entries (VarInt)
  - ModProtocol entries:
    - Identifier (UTF8 String)
    - Display name of the mod protocol (UTF8 String)
    - Server side version of the mod protocol (UTF8 String)
    - Required version of the mod protocol on the client or empty (UTF8 String)
  - Disconnect on failure (boolean).
- Serverbound `fabric:mod_protocol/response/v1` - Contains a map of supported client side protocols (id to version) 
  and a list of missing server protocols. This packet is only sent in case of a success (all protocols matched) or if
  the disconnect field of request packet is set to false.
  
  Format of the payload looks as follows:
  - Number of client side supported entries (VarInt)
  - Supported Protocol entries:
    - Identifier (UTF8 String)
    - Client side version of the mod protocol (UTF8 String)
  - Number of missing required server mod protocols (VarInt)
  - Identifiers of missing mod protocols. (multiple UTF8 String)

### Server status extensions (ClientboundStatusResponsePacket):
When Mod Protocol entries are defined, server will send additional values encoded in the json object send
by server when handling server list pings. This is used to mark server compatibility status in similar way
as if the server ran on incompatible Minecraft version (just with the text saying `Incompatible mods` rather than `Incompatible version`).

The provided fields are:
- `fabric:mod_protocol/versions/v1` - A list of Mod Protocols with version and requirements, using compacted string format.
  A Mod Protocol is encoded as an Identifier, server protocol version and optionally required client versions joined together with `\u0000`/ NUL
  as a separator.
- `fabric:mod_protocol/always_compatible/v1` - A boolean that forces the server to be always compatible in case of Mod Protocol checks,
  even if that's not the case. When it's set to true, you can skip setting the value of `fabric:mod_protocol/versions/v1`, as it won't be used.
