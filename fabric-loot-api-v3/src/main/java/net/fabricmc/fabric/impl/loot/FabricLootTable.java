package net.fabricmc.fabric.impl.loot;

import net.minecraft.loot.LootTable;
import net.minecraft.registry.RegistryKey;

public interface FabricLootTable {
	void fabric$setRegistryKey(RegistryKey<LootTable> key);
}
