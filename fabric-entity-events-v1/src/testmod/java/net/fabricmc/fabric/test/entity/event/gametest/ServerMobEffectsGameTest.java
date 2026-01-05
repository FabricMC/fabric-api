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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;

import net.fabricmc.fabric.api.entity.event.v1.effect.ServerMobEffectEvents;
import net.fabricmc.fabric.api.gametest.v1.GameTest;

public class ServerMobEffectsGameTest {
	@GameTest
	public void allowAdd(GameTestHelper context) {
		ServerMobEffectEvents.ALLOW_ADD.register((effectInstance, entity, ctx) -> {
			if (ctx.isFromCommand()) return true;
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
		ServerMobEffectEvents.BEFORE_ADD.register((effectInstance, entity, ctx) -> {
			if (!isThisTheSalmon(entity) || obj.contextRef == null) return;
			obj.contextRef.assertFalse(entity.hasEffect(MobEffects.ABSORPTION), "The Salmon mustn't have absorption yet");
		});
		ServerMobEffectEvents.AFTER_ADD.register((effectInstance, entity, ctx) -> {
			if (!isThisTheSalmon(entity) || obj.contextRef == null) return;
			obj.contextRef.assertTrue(entity.hasEffect(MobEffects.ABSORPTION), "The Salmon must have absorption at this point");
		});
		Salmon theSalmon = summonTheSalmon(context);
		theSalmon.addEffect(createEffect(MobEffects.ABSORPTION));
		context.succeed();
		obj.contextRef = null;
	}

	@GameTest(
			maxTicks = 150
	)
	public void allowEarlyRemove(GameTestHelper context) {
		ServerMobEffectEvents.ALLOW_EARLY_REMOVE.register((effectInstance, entity, ctx) -> {
			if (ctx.isFromCommand()) return true;
			// Same thing as ALLOW_ADD.
			boolean isThisTheEntity = isThisTheSalmon(entity) || isThisThePlayer(entity);
			boolean cannotRemove = effectInstance.is(MobEffects.BLINDNESS) || effectInstance.is(MobEffects.POISON);
			return !(cannotRemove && isThisTheEntity);
		});

		// Regular Salmon testing
		Salmon theSalmon = summonTheSalmon(context);
		theSalmon.addEffect(createEffect(MobEffects.BLINDNESS));
		context.assertTrue(theSalmon.hasEffect(MobEffects.BLINDNESS), "The Salmon must have blindness");

		// Player milk testing
		Player thePlayer = summonThePlayer(context);
		thePlayer.addEffect(createEffect(MobEffects.WEAVING));
		thePlayer.addEffect(createEffect(MobEffects.BLINDNESS));
		useItem(thePlayer);

		context.assertFalse(thePlayer.isUsingItem(), "The Player mustn't be using an item at this point; this is a bug with the test"); // Sanity check so you don't go insane
		context.assertFalse(thePlayer.hasEffect(MobEffects.WEAVING), "The Player mustn't have weaving as it should have been cleared after drinking milk");
		context.assertTrue(thePlayer.hasEffect(MobEffects.BLINDNESS), "The Player must still have blindness after drinking milk");

		// Player honey/poison testing
		thePlayer.addEffect(createEffect(MobEffects.POISON));
		thePlayer.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.HONEY_BOTTLE));
		useItem(thePlayer);

		context.assertTrue(thePlayer.hasEffect(MobEffects.POISON), "The Player must still have poison after drinking a honey bottle");

		context.succeed();
	}

	@GameTest
	public void beforeAfterRemove(GameTestHelper context) {
		var obj = new Object() { // Scoped events at home
			GameTestHelper contextRef = context;
		};
		ServerMobEffectEvents.BEFORE_REMOVE.register((effectInstance, entity, ctx) -> {
			if (!isThisTheSalmon(entity) || obj.contextRef == null) return;
			obj.contextRef.assertTrue(entity.hasEffect(MobEffects.SATURATION), "The Salmon must have saturation as it should not yet have been removed");
		});
		ServerMobEffectEvents.AFTER_REMOVE.register((effectInstance, entity, ctx) -> {
			if (!isThisTheSalmon(entity) || obj.contextRef == null) return;
			obj.contextRef.assertFalse(entity.hasEffect(MobEffects.SATURATION), "The Salmon mustn't have saturation as it should have been removed by now");
		});
		Salmon theSalmon = summonTheSalmon(context);
		theSalmon.addEffect(createEffect(MobEffects.SATURATION));
		theSalmon.removeEffect(MobEffects.SATURATION);
		context.succeed();
		obj.contextRef = null;
	}

	// Regression test for https://github.com/FabricMC/fabric-api/issues/5121
	@GameTest
	public void removeNoneExistentEffect(GameTestHelper context) {
		var obj = new Object() { // Scoped events at home
			GameTestHelper contextRef = context;
		};
		ServerMobEffectEvents.BEFORE_REMOVE.register((effectInstance, entity, ctx) -> {
			if (!isThisTheSalmon(entity) || obj.contextRef == null) return;
			obj.contextRef.fail("The BEFORE_REMOVE event must not be called when removing a non-existent effect");
		});
		ServerMobEffectEvents.AFTER_REMOVE.register((effectInstance, entity, ctx) -> {
			if (!isThisTheSalmon(entity) || obj.contextRef == null) return;
			obj.contextRef.fail("The AFTER_REMOVE event must not be called when removing a non-existent effect");
		});
		Salmon theSalmon = summonTheSalmon(context);
		theSalmon.removeEffect(MobEffects.SATURATION);
		context.succeed();
		obj.contextRef = null;
	}

	private static Salmon summonTheSalmon(GameTestHelper context) {
		Salmon theSalmon = context.spawnWithNoFreeWill(EntityType.SALMON, context.relativeVec(new Vec3(0.0, 1.0, 0.0)));
		theSalmon.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.POTATO));
		return theSalmon;
	}

	private static Player summonThePlayer(GameTestHelper context) {
		Player thePlayer = context.makeMockPlayer(GameType.SURVIVAL);
		var itemStack = new ItemStack(Items.MILK_BUCKET);
		thePlayer.setItemInHand(InteractionHand.MAIN_HAND, itemStack);
		thePlayer.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.POTATO));
		return thePlayer;
	}

	private static boolean isThisTheSalmon(LivingEntity livingEntity) {
		return livingEntity instanceof Salmon && livingEntity.getMainHandItem().is(Items.POTATO);
	}

	private static boolean isThisThePlayer(LivingEntity livingEntity) {
		return livingEntity instanceof Player && livingEntity.getOffhandItem().is(Items.POTATO);
	}

	private static MobEffectInstance createEffect(Holder<MobEffect> effect) {
		return new MobEffectInstance(effect, 600, 1);
	}

	private static void useItem(Player thePlayer) {
		thePlayer.startUsingItem(InteractionHand.MAIN_HAND);

		for (int i = 0; i < thePlayer.getMainHandItem().getUseDuration(thePlayer) + 1; i++) {
			thePlayer.tick();
		}
	}
}
