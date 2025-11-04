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

package net.fabricmc.fabric.test.item;

import org.jspecify.annotations.Nullable;

import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.block.state.BlockState;

public class UpdatingItem extends Item {
	private static final Identifier PLUS_FIVE_ID = Identifier.fromNamespaceAndPath("fabric-item-api-v1-testmod", "plus_five");
	private static final AttributeModifier PLUS_FIVE = new AttributeModifier(
			PLUS_FIVE_ID, 5, AttributeModifier.Operation.ADD_VALUE);

	private final boolean allowUpdateAnimation;

	public UpdatingItem(boolean allowUpdateAnimation, Item.Properties settings) {
		super(settings
					.component(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.builder()
						.add(Attributes.ATTACK_DAMAGE, PLUS_FIVE, EquipmentSlotGroup.MAINHAND)
						.build()
					)
		);
		this.allowUpdateAnimation = allowUpdateAnimation;
	}

	@Override
	public void inventoryTick(ItemStack stack, ServerLevel world, Entity entity, @Nullable EquipmentSlot equipmentSlot) {
		if (!world.isClientSide()) {
			stack.set(ItemUpdateAnimationTest.TICKS, Math.max(0, stack.getOrDefault(ItemUpdateAnimationTest.TICKS, 0) + 1));
		}
	}

	@Override
	public boolean allowComponentsUpdateAnimation(Player player, InteractionHand hand, ItemStack originalStack, ItemStack updatedStack) {
		return allowUpdateAnimation;
	}

	@Override
	public boolean allowContinuingBlockBreaking(Player player, ItemStack oldStack, ItemStack newStack) {
		return true; // set to false and you won't be able to break a block in survival with this item
	}

	// True for 15 seconds every 30 seconds
	private boolean isEnabled(ItemStack stack) {
		return !stack.has(ItemUpdateAnimationTest.TICKS) || stack.getOrDefault(ItemUpdateAnimationTest.TICKS, 0) % 600 < 300;
	}

	@Override
	public float getDestroySpeed(ItemStack stack, BlockState state) {
		return isEnabled(stack) ? 20 : super.getDestroySpeed(stack, state);
	}
}
