# Fabric Advancement API (v1)

This module includes APIs for modifying, replacing, and reacting to the loading of advancements.

## [Advancement events](src/main/java/net/fabricmc/fabric/api/advancement/event/v1/AdvancementEvents.java)

This class provides three events for interacting with advancements. All events provide a `HolderLookup.Provider` which can be used to access registries (items, loot tables, etc.) for advanced data-driven modifications.

* `AdvancementEvents.REPLACE` runs first and lets you replace advancements completely.
* `AdvancementEvents.MODIFY` runs after and lets you modify advancements, including the ones created in `REPLACE`.
* `AdvancementEvents.ALL_LOADED` runs once all advancements have been successfully loaded, replaced, and modified. It is useful for post-processing and reading the finalized map.

## Enhanced advancement builders

Thanks to **Interface Injection**, the standard `Advancement.Builder` is enhanced with new methods that allow you to modify advancements easily without manual casting.

You can directly call these methods on any `Advancement.Builder`:

* `removeCriterion(String name)`: Removes a criterion from an existing advancement.
* `getCriteria()`: Returns the current map of criteria.

### Example: Modifying an advancement

Here is how you can use the API to modify an existing advancement by removing an old criterion and adding a new one:

```java
AdvancementEvents.MODIFY.register((id, builder, source, registries) -> {
    if (id.equals(Identifier.withDefaultNamespace("husbandry/tactical_fishing"))) {
        
        // Remove an existing criterion directly using the injected method
        builder.removeCriterion("some_criterion");

        // Add a new criterion
        builder.addCriterion("stone_pickaxe", InventoryChangeTrigger.TriggerInstance.hasItems(Items.STONE_PICKAXE));
        
        // Update requirements so either the fish or the pickaxe completes it
        builder.requirements(AdvancementRequirements.Strategy.OR);
    }
});

```

## Advancement sources

Both the `REPLACE` and `MODIFY` events have access to an [advancement source](https://www.google.com/search?q=src/main/java/net/fabricmc/fabric/api/advancement/event/v1/AdvancementSource.java) that you can use to check where an advancement is loaded from.

For example, you can use the `isBuiltin()` method to check if an advancement comes from the vanilla game or a mod (`VANILLA` or `MOD`), and choose not to modify custom advancements provided by external data packs (`DATA_PACK`). Advancements that were successfully replaced in the `REPLACE` event will have their source marked as `REPLACED`.

## Utilities

[`FabricAdvancementBuilder`](https://www.google.com/search?q=src/main/java/net/fabricmc/fabric/api/advancement/event/v1/FabricAdvancementBuilder.java) provides a static `copyOf` method for creating a mutable `Advancement.Builder` copy of an existing `Advancement` record. This allows you to easily inherit properties like parents, display info, and existing criteria when modifying an advancement.
