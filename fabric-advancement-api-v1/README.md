# Fabric Advancement API (v1)

This module includes APIs for modifying, replacing, and reacting to the loading of advancements.

## [Advancement events](src/main/java/net/fabricmc/fabric/api/advancement/event/v1/AdvancementEvents.java)

This class provides three events for interacting with advancements.

`AdvancementEvents.REPLACE` runs first and lets you replace advancements completely.

`AdvancementEvents.MODIFY` runs after and lets you modify advancements, including the ones created in `REPLACE`,
with features like adding new criteria or altering their requirements.

`AdvancementEvents.ALL_LOADED` runs once all advancements have been successfully loaded, replaced, and modified.
It is useful for post-processing and reading the finalized immutable map of advancements.

### Advancement sources

Both the `REPLACE` and `MODIFY` events have access to an [advancement source](src/main/java/net/fabricmc/fabric/api/advancement/event/v1/AdvancementSource.java)
that you can use to check where an advancement is loaded from.

For example, you can use the `isBuiltin()` method to check if an advancement comes from the vanilla game or a mod (`VANILLA` or `MOD`),
and choose not to modify custom advancements provided by external data packs (`DATA_PACK`). Advancements that were successfully replaced in the `REPLACE` event will have their source marked as `REPLACED`.

## Enhanced advancement builders

[`FabricAdvancementBuilder`](src/main/java/net/fabricmc/fabric/api/advancement/event/v1/FabricAdvancementBuilder.java)
provides utilities for dealing with already-built advancements.

This interface features a static `copyOf` method for creating a mutable `Advancement.Builder` copy of an existing `Advancement` record. This allows you to easily inherit properties like parents, display info, and existing criteria when modifying an advancement.
