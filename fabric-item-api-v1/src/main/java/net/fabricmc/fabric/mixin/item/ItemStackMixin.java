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

import java.util.HashSet;
import java.util.List;
import java.util.function.Consumer;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;

import net.fabricmc.fabric.api.item.v1.CustomDamageHandler;
import net.fabricmc.fabric.api.item.v1.FabricItemStack;
import net.fabricmc.fabric.impl.item.ComponentTooltipAppenderRegistryImpl;
import net.fabricmc.fabric.impl.item.ItemExtensions;
import net.fabricmc.fabric.impl.item.VanillaTooltipAppenderOrder;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin implements FabricItemStack {
	@Shadow
	public abstract Item getItem();

	@Shadow
	public abstract void decrement(int amount);

	@WrapOperation(method = "damage(ILnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/EquipmentSlot;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;damage(ILnet/minecraft/server/world/ServerWorld;Lnet/minecraft/server/network/ServerPlayerEntity;Ljava/util/function/Consumer;)V"))
	private void hookDamage(ItemStack instance, int amount, ServerWorld serverWorld, ServerPlayerEntity serverPlayerEntity, Consumer<Item> consumer, Operation<Void> original, @Local(argsOnly = true) LivingEntity entity, @Local(argsOnly = true) EquipmentSlot slot) {
		CustomDamageHandler handler = ((ItemExtensions) getItem()).fabric_getCustomDamageHandler();

		/*
			This is called by creative mode players, post-24w21a.
			The other damage method (which original.call discards) handles the creative mode check.
			Since it doesn't make sense to call an event to calculate a to-be-discarded value
			(and to prevent mods from breaking item stacks in Creative mode),
			we preserve the pre-24w21a behavior of not calling in creative mode.
		*/

		if (handler != null && !entity.isInCreativeMode()) {
			// Track whether an item has been broken by custom handler
			MutableBoolean mut = new MutableBoolean(false);
			amount = handler.damage((ItemStack) (Object) this, amount, entity, slot, () -> {
				mut.setTrue();
				this.decrement(1);
				consumer.accept(this.getItem());
			});

			// If item is broken, there's no reason to call the original.
			if (mut.booleanValue()) return;
		}

		original.call(instance, amount, serverWorld, serverPlayerEntity, consumer);
	}

	@ModifyArg(method = "appendTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;appendComponentTooltip(Lnet/minecraft/component/ComponentType;Lnet/minecraft/item/Item$TooltipContext;Lnet/minecraft/component/type/TooltipDisplayComponent;Ljava/util/function/Consumer;Lnet/minecraft/item/tooltip/TooltipType;)V"))
	private ComponentType<?> preAppendComponentTooltip(
			ComponentType<?> componentType,
			@Local(argsOnly = true) Item.TooltipContext context,
			@Local(argsOnly = true) TooltipDisplayComponent displayComponent,
			@Local(argsOnly = true) TooltipType type,
			@Local(argsOnly = true) Consumer<Text> textConsumer,
			@Share("index") LocalIntRef index
	) {
		preAppendTooltip(componentType, context, displayComponent, textConsumer, type, index);
		return componentType;
	}

	@ModifyArg(method = "appendTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/component/type/TooltipDisplayComponent;shouldDisplay(Lnet/minecraft/component/ComponentType;)Z"))
	private ComponentType<?> preShouldDisplay(
			ComponentType<?> componentType,
			@Local(argsOnly = true) Item.TooltipContext context,
			@Local(argsOnly = true) TooltipDisplayComponent displayComponent,
			@Local(argsOnly = true) TooltipType type,
			@Local(argsOnly = true) Consumer<Text> textConsumer,
			@Share("index") LocalIntRef index
	) {
		preAppendTooltip(componentType, context, displayComponent, textConsumer, type, index);
		return componentType;
	}

	@Inject(method = "appendTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;appendAttributeModifiersTooltip(Ljava/util/function/Consumer;Lnet/minecraft/component/type/TooltipDisplayComponent;Lnet/minecraft/entity/player/PlayerEntity;)V"))
	private void preAttributeModifiers(
			Item.TooltipContext context,
			TooltipDisplayComponent displayComponent,
			@Nullable PlayerEntity player,
			TooltipType type,
			Consumer<Text> textConsumer,
			CallbackInfo ci,
			@Share("index") LocalIntRef index
	) {
		// Special case: attribute modifiers are extracted into a separate method
		preAppendTooltip(DataComponentTypes.ATTRIBUTE_MODIFIERS, context, displayComponent, textConsumer, type, index);
	}

	@Inject(method = "appendTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/registry/DefaultedRegistry;getId(Ljava/lang/Object;)Lnet/minecraft/util/Identifier;"))
	private void postTooltipsAdvanced(
			Item.TooltipContext context,
			TooltipDisplayComponent displayComponent,
			@Nullable PlayerEntity player,
			TooltipType type,
			Consumer<Text> textConsumer,
			CallbackInfo ci,
			@Share("index") LocalIntRef index
	) {
		preAppendTooltip(null, context, displayComponent, textConsumer, type, index);
	}

	@ModifyExpressionValue(method = "appendTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/tooltip/TooltipType;isAdvanced()Z"))
	private boolean postTooltipsNonAdvanced(
			boolean isAdvanced,
			Item.TooltipContext context,
			TooltipDisplayComponent displayComponent,
			@Nullable PlayerEntity player,
			TooltipType type,
			Consumer<Text> textConsumer,
			@Share("index") LocalIntRef index
	) {
		if (!isAdvanced) {
			preAppendTooltip(null, context, displayComponent, textConsumer, type, index);
		}

		return isAdvanced;
	}

	@Unique
	private void preAppendTooltip(
			@Nullable ComponentType<?> componentType,
			Item.TooltipContext context,
			TooltipDisplayComponent displayComponent,
			Consumer<Text> textConsumer,
			TooltipType tooltipType,
			LocalIntRef index
	) {
		if (!ComponentTooltipAppenderRegistryImpl.hasModdedEntries()) {
			return;
		}

		if (index.get() == 0) {
			ComponentTooltipAppenderRegistryImpl.onFirst((ItemStack) (Object) this, context, displayComponent, textConsumer, tooltipType);
		}

		List<ComponentType<?>> vanillaOrder = VanillaTooltipAppenderOrder.getVanillaOrder();

		if (index.get() > vanillaOrder.size()) {
			return;
		}

		// Find out which vanilla tooltip appenders we may have skipped over and run their anchored appenders first

		while (true) {
			if (index.get() > 0) {
				ComponentType<?> prevComponentInOrder = vanillaOrder.get(index.get() - 1);
				HashSet<ComponentType<?>> cycleDetector = new HashSet<>();
				cycleDetector.add(prevComponentInOrder);
				ComponentTooltipAppenderRegistryImpl.onAfter((ItemStack) (Object) this, prevComponentInOrder, context, displayComponent, textConsumer, tooltipType, cycleDetector);
			}

			if (index.get() == vanillaOrder.size()) {
				index.set(index.get() + 1);
				break;
			}

			ComponentType<?> componentInOrder = vanillaOrder.get(index.get());
			HashSet<ComponentType<?>> cycleDetector = new HashSet<>();
			cycleDetector.add(componentInOrder);
			ComponentTooltipAppenderRegistryImpl.onBefore((ItemStack) (Object) this, componentInOrder, context, displayComponent, textConsumer, tooltipType, cycleDetector);
			index.set(index.get() + 1);

			if (componentInOrder == componentType) {
				break;
			}
		}

		if (componentType == null) {
			ComponentTooltipAppenderRegistryImpl.onLast((ItemStack) (Object) this, context, displayComponent, textConsumer, tooltipType);
		}
	}
}
