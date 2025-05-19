/*
 * Copyright (c) 2016, 2017, 2018, 2019 FabricMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.fabricmc.fabric.mixin.loot;

import java.util.List;
import java.util.function.Consumer;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContext;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;

import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.fabricmc.fabric.impl.loot.FabricLootTable;

@Mixin(value = LootTable.class, priority = 3000 /* arbitrary, but requires mods to explicit set a priority to wrap the fabric event.*/)
public class LootTableMixin implements FabricLootTable {
	/*
	 * the key of this loot table, if initialized.
	 */
	@Unique
	@Nullable
	RegistryKey<LootTable> key = null;

	@WrapMethod(method = "generateUnprocessedLoot(Lnet/minecraft/loot/context/LootContext;Ljava/util/function/Consumer;)V")
	private void fabric$modifyDrops(LootContext context, Consumer<ItemStack> lootConsumer, Operation<Void> original) {
		checkKeyInitOrInit(context);
		List<ItemStack> list = new ObjectArrayList<>();
		original.call(context, (Consumer<ItemStack>) list::add);
		LootTableEvents.MODIFY_DROPS.invoker().modifyLootTableDrops(
				this.key,
				context,
				list
		);
		list.forEach(lootConsumer);
	}

	@Override
	public void fabric$setRegistryKey(RegistryKey<LootTable> key) {
		this.key = key;
	}

	@Unique
	private void checkKeyInitOrInit(LootContext context) {
		if (key != null) {
			return;
		}

		RegistryWrapper.WrapperLookup wrapperLookup = context.getWorld()
				.getServer()
				.getReloadableRegistries()
				.createRegistryLookup();

		RegistryWrapper<LootTable> lootTableRegistryWrapper = wrapperLookup
				.getOptional(RegistryKeys.LOOT_TABLE)
				.orElseThrow(() -> new IllegalStateException("Failed to fetch LootTable wrapper from WrapperLookup"));

		// noinspection EqualsBetweenInconvertibleTypes
		this.key = lootTableRegistryWrapper
				.streamEntries()
				.filter(it -> it.value().equals(this))
				.findFirst()
				.map(RegistryEntry.Reference::registryKey)
				.orElseThrow(
						() -> new IllegalStateException("LootTable appears to be unregistered, but has been asked to generate loot")
				);
	}
}
