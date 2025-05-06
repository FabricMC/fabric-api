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

package net.fabricmc.fabric.mixin.transfer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import net.minecraft.component.ComponentChanges;
import net.minecraft.item.Item;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.impl.transfer.item.ItemVariantCache;
import net.fabricmc.fabric.impl.transfer.item.ItemVariantImpl;

/**
 * Cache the ItemVariant with a null tag inside each Item directly.
 */
@Mixin(Item.class)
public class ItemMixin implements ItemVariantCache {
	@Unique
	@SuppressWarnings("ConstantConditions")
	private final ItemVariant cachedItemVariant = new ItemVariantImpl((Item) (Object) this, ComponentChanges.EMPTY);

	@Override
	public ItemVariant fabric_getCachedItemVariant() {
		return cachedItemVariant;
	}
}
