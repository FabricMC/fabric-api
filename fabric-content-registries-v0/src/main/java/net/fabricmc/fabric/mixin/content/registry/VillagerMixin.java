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

package net.fabricmc.fabric.mixin.content.registry;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import net.fabricmc.fabric.impl.content.registry.VillagerInteractionRegistriesImpl;

@Mixin(Villager.class)
public class VillagerMixin {
	@WrapOperation(method = "wantsToPickUp", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/tags/TagKey;)Z"))
	private boolean useGatherableItemsSet(ItemStack stack, TagKey<Item> tag, Operation<Boolean> original) {
		return VillagerInteractionRegistriesImpl.getCollectableRegistry().contains(stack.getItem()) || original.call(stack, tag);
	}
}
