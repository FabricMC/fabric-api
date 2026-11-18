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

package net.fabricmc.fabric.mixin.entity.event;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.impl.entity.event.effect.HorseFoodImpl;

@Mixin(AbstractHorse.class)
abstract class AbstractHorseMixin extends Animal {
	@Shadow
	public abstract boolean isTamed();

	protected AbstractHorseMixin(EntityType<? extends Animal> type, Level level) {
		super(type, level);
	}

	@Inject(method = "handleEating", at = @At("HEAD"))
	void createFoodInstance(Player player, ItemStack itemStack, CallbackInfoReturnable<Boolean> cir, @Share("food") LocalRef<ServerLivingEntityEvents.HorseFed.HorseFood> food) {
		food.set(new HorseFoodImpl(0, 0, 0, false));
	}

	@WrapOperation(method = "handleEating", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/animal/equine/AbstractHorse;setInLove(Lnet/minecraft/world/entity/player/Player;)V"))
	void defferInLove(AbstractHorse instance, Player player, Operation<Void> original, @Share("food") LocalRef<ServerLivingEntityEvents.HorseFed.HorseFood> food) {
		food.get().inLove(true);
	}

	@Inject(method = "handleEating", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/animal/equine/AbstractHorse;canHealNaturally()Z", shift = At.Shift.BY, by = -1))
	void modifyFoodResults(Player player, ItemStack itemStack, CallbackInfoReturnable<Boolean> cir, @Local(name = "itemUsed") LocalBooleanRef itemUsed, @Local(name = "heal") LocalFloatRef heal, @Local(name = "ageUp") LocalIntRef ageUp, @Local(name = "temper") LocalIntRef temper, @Share("food") LocalRef<ServerLivingEntityEvents.HorseFed.HorseFood> food) {
		food.get().health(heal.get());
		food.get().ageUp(ageUp.get());
		food.get().temper(temper.get());

		ServerLivingEntityEvents.HORSE_FED.invoker().onFed((AbstractHorse) (Object) this, itemStack, player, food.get());

		heal.set(food.get().health());
		ageUp.set(food.get().ageUp());
		temper.set(food.get().temper());

		if (food.get().inLove() && !this.level().isClientSide() && this.isTamed() && this.getAge() == 0 && !this.isInLove()) {
			itemUsed.set(true);
			this.setInLove(player);
		}
	}
}
