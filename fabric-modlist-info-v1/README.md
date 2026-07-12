# Fabric Mod List Info (V1)

This module automatically saves a list of all active mods and their versions into a world's save directory every time the world is saved.

## Purpose

When loading an old world save, it can be difficult or impossible to know which mods were active when the world was last played. This module solves that by writing a `fabricModList.json` file to the world root directory on every save.

## Output

A `fabricModList.json` file is written to `<world_dir>/fabricModList.json` after each save. It contains a sorted list of all mods (with their embedded child mods nested underneath):

```json
{
  "modCount": 3,
  "mods": [
    {
      "id": "fabric-api",
      "name": "Fabric API",
      "version": "0.154.2+26.2",
      "children": [
        { "id": "fabric-lifecycle-events-v1", "version": "4.1.3" }
      ]
    },
    {
      "id": "fabricloader",
      "name": "Fabric Loader",
      "version": "0.18.4"
    },
    {
      "id": "minecraft",
      "name": "Minecraft",
      "version": "26.2"
    },
    ...
  ]
}
```

## Implementation

The module hooks into [`ServerLifecycleEvents.AFTER_SAVE`](src/main/java/net/fabricmc/fabric/api/event/lifecycle/v1/ServerLifecycleEvents.java) and writes the mod list using the [Gson](https://github.com/google/gson) library bundled with Minecraft. No public API is exposed, the module works automatically with no configuration required.
