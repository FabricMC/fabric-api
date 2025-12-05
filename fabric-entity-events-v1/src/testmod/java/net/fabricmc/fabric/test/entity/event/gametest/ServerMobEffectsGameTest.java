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

package net.fabricmc.fabric.test.entity.event.gametest;

import net.minecraft.core.Holder;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.fish.Salmon;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.consume_effects.ClearAllStatusEffectsConsumeEffect;
import net.minecraft.world.phys.Vec3;

import net.fabricmc.fabric.api.entity.event.v1.ServerMobEffectEvents;
import net.fabricmc.fabric.api.gametest.v1.GameTest;

public class ServerMobEffectsGameTest {
	@GameTest
	public void allowAdd(GameTestHelper context) {
		ServerMobEffectEvents.ALLOW_ADD.register((effectInstance, entity) -> {
			// If the entity wants to regenerate and is holding a potato,
			// deny them regeneration privileges.
			// This is specific enough since events aren't scoped for
			// GameTests.
			return !(effectInstance.is(MobEffects.REGENERATION) && isThisTheSalmon(entity));
		});
		Salmon theSalmon = summonTheSalmon(context);
		theSalmon.addEffect(createEffect(MobEffects.REGENERATION));
		context.assertTrue(theSalmon.getMainHandItem().is(Items.POTATO), "The Salmon must be holding (how!?) a potato");
		context.assertFalse(theSalmon.hasEffect(MobEffects.REGENERATION), "The Salmon must not have regeneration");
		context.succeed();
	}

	@GameTest
	public void beforeAfterAdd(GameTestHelper context) {
		var obj = new Object() { // Scoped events at home
			GameTestHelper contextRef = context;
		};
		ServerMobEffectEvents.BEFORE_ADD.register((effectInstance, entity) -> {
			if (!isThisTheSalmon(entity) || obj.contextRef == null) return;
			obj.contextRef.assertFalse(entity.hasEffect(MobEffects.ABSORPTION), "The Salmon mustn't have absorption yet");
		});
		ServerMobEffectEvents.AFTER_ADD.register((effectInstance, entity) -> {
			if (!isThisTheSalmon(entity) || obj.contextRef == null) return;
			obj.contextRef.assertTrue(entity.hasEffect(MobEffects.ABSORPTION), "The Salmon must have absorption at this point");
		});
		Salmon theSalmon = summonTheSalmon(context);
		theSalmon.addEffect(createEffect(MobEffects.ABSORPTION));
		context.succeed();
		obj.contextRef = null;
	}

	@GameTest
	public void allowEarlyRemove(GameTestHelper context) {
		ServerMobEffectEvents.ALLOW_EARLY_REMOVE.register((effectInstance, entity) -> {
			// Same thing as ALLOW_ADD.
			return !(effectInstance.is(MobEffects.BLINDNESS) && isThisTheSalmon(entity));
		});
		Salmon theSalmon = summonTheSalmon(context);
		theSalmon.addEffect(createEffect(MobEffects.BLINDNESS));
		context.assertFalse(ClearAllStatusEffectsConsumeEffect.INSTANCE.apply(context.getLevel(), null, theSalmon), "ClearAllStatusEffectsConsumeEffect#apply != false");
		context.assertTrue(theSalmon.hasEffect(MobEffects.BLINDNESS), "The Salmon must have blindness");
		context.succeed();
	}

	@GameTest
	public void beforeAfterRemove(GameTestHelper context) {
		var obj = new Object() { // Scoped events at home
			GameTestHelper contextRef = context;
		};
		ServerMobEffectEvents.BEFORE_REMOVE.register((effectInstance, entity) -> {
			if (!isThisTheSalmon(entity) || obj.contextRef == null) return;
			obj.contextRef.assertTrue(entity.hasEffect(MobEffects.SATURATION), "The Salmon must have saturation as it should not yet have been removed");
		});
		ServerMobEffectEvents.AFTER_REMOVE.register((effectInstance, entity) -> {
			if (!isThisTheSalmon(entity) || obj.contextRef == null) return;
			obj.contextRef.assertFalse(entity.hasEffect(MobEffects.SATURATION), "The Salmon mustn't have saturation as it should have been removed by now");
		});
		Salmon theSalmon = summonTheSalmon(context);
		theSalmon.addEffect(createEffect(MobEffects.SATURATION));
		theSalmon.removeEffect(MobEffects.SATURATION);
		context.succeed();
		obj.contextRef = null;
	}

	private static Salmon summonTheSalmon(GameTestHelper context) {
		Salmon theSalmon = context.spawnWithNoFreeWill(EntityType.SALMON, context.relativeVec(new Vec3(0.0, 1.0, 0.0)));
		theSalmon.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.POTATO));
		return theSalmon;
	}

	private static boolean isThisTheSalmon(LivingEntity livingEntity) {
		return livingEntity instanceof Salmon && livingEntity.getMainHandItem().is(Items.POTATO);
	}

	private static MobEffectInstance createEffect(Holder<MobEffect> effect) {
		return new MobEffectInstance(effect, 600, 1);
	}
}
