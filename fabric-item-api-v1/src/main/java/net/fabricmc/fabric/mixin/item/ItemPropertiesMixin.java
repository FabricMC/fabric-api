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

package net.fabricmc.fabric.mixin.item;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.resources.DependantName;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;

import net.fabricmc.fabric.api.item.v1.FabricItem;

@Mixin(Item.Properties.class)
public class ItemPropertiesMixin implements FabricItem.Settings {
	@Final
	@Shadow
	@Mutable
	private DependantName<Item, Identifier> model;

	@Override
	public Item.Properties modelId(Identifier modelId) {
		this.model = DependantName.fixed(modelId);
		return FabricItem.Settings.super.modelId(modelId);
	}
}
