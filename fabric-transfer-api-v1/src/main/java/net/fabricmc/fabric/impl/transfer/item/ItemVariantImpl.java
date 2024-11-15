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

package net.fabricmc.fabric.impl.transfer.item;

import java.util.Objects;

import org.jetbrains.annotations.Nullable;

import net.minecraft.component.ComponentChanges;
import net.minecraft.component.ComponentMap;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.entry.RegistryEntry;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.impl.transfer.TransferApiImpl;

public class ItemVariantImpl implements ItemVariant {
	public static ItemVariant of(Item item, ComponentChanges components) {
		Objects.requireNonNull(item, "Item may not be null.");
		Objects.requireNonNull(components, "Components may not be null.");

		// Only tag-less or empty item variants are cached for now.
		if (components.isEmpty() || item == Items.AIR) {
			return ((ItemVariantCache) item).fabric_getCachedItemVariant();
		} else {
			return new ItemVariantImpl(item, components);
		}
	}

	public static ItemVariant of(RegistryEntry<Item> item, ComponentChanges components) {
		return of(item.value(), components);
	}

	private final Item item;
	private final ComponentChanges components;
	private final int hashCode;
	/**
	 * Lazily computed, equivalent to calling toStack(1). <b>MAKE SURE IT IS NEVER MODIFIED!</b>
	 */
	private volatile @Nullable ItemStack cachedStack = null;

	public ItemVariantImpl(Item item, ComponentChanges components) {
		this.item = item;
		this.components = components;
		hashCode = Objects.hash(item, components);
	}

	@Override
	public Item getObject() {
		return item;
	}

	@Nullable
	@Override
	public ComponentChanges getComponents() {
		return components;
	}

	@Override
	public ComponentMap getComponentMap() {
		return getCachedStack().getComponents();
	}

	@Override
	public ItemVariant withComponentChanges(ComponentChanges changes) {
		return of(item, TransferApiImpl.mergeChanges(getComponents(), changes));
	}

	@Override
	public boolean isBlank() {
		return item == Items.AIR;
	}

	@Override
	public String toString() {
		return "ItemVariant{item=" + item + ", components=" + components + '}';
	}

	@Override
	public boolean equals(Object o) {
		// succeed fast with == check
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		ItemVariantImpl ItemVariant = (ItemVariantImpl) o;
		// fail fast with hash code
		return hashCode == ItemVariant.hashCode && item == ItemVariant.item && componentsMatch(ItemVariant.components);
	}

	@Override
	public int hashCode() {
		return hashCode;
	}

	public ItemStack getCachedStack() {
		ItemStack ret = cachedStack;

		if (ret == null) {
			// multiple stacks could be created at the same time by different threads, but that is not an issue
			cachedStack = ret = toStack();
		}

		return ret;
	}
}
