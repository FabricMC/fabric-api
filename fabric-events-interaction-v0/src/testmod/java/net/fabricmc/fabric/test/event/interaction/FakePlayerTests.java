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

package net.fabricmc.fabric.test.event.interaction;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import net.fabricmc.fabric.api.entity.FakePlayer;
import net.fabricmc.fabric.api.gametest.v1.GameTest;

public class FakePlayerTests {
	/**
	 * Try placing a sign with a fake player.
	 */
	@GameTest
	public void testFakePlayerPlaceSign(GameTestHelper context) {
		// This is for Fabric internal testing only, if you copy this to your mod you're on your own...

		BlockPos basePos = new BlockPos(0, 1, 0);
		BlockPos signPos = basePos.above();

		context.setBlock(basePos, Blocks.STONE.defaultBlockState());

		Player fakePlayer = FakePlayer.get(context.getLevel());
		context.assertFalse(fakePlayer.hasInfiniteMaterials(), Component.literal("Fake player is in creative mode"));

		BlockPos fakePlayerPos = context.absolutePos(signPos.offset(2, 0, 2));
		fakePlayer.setPos(fakePlayerPos.getX(), fakePlayerPos.getY(), fakePlayerPos.getZ());
		ItemStack signStack = Items.OAK_SIGN.getDefaultInstance();
		fakePlayer.setItemInHand(InteractionHand.MAIN_HAND, signStack);

		Vec3 hitPos = context.absolutePos(basePos).getCenter().add(0, 0.5, 0);
		BlockHitResult hitResult = new BlockHitResult(hitPos, Direction.UP, context.absolutePos(basePos), false);
		signStack.useOn(new UseOnContext(fakePlayer, InteractionHand.MAIN_HAND, hitResult));

		context.assertBlockState(signPos, x -> x.is(Blocks.OAK_SIGN), (b) -> Component.literal("Sign was not placed"));
		context.assertTrue(signStack.isEmpty(), Component.literal("Sign stack was not emptied"));
		context.succeed();
	}

	/**
	 * Try breaking a beehive with a fake player (see {@code BeehiveBlockMixin}).
	 */
	@GameTest
	public void testFakePlayerBreakBeehive(GameTestHelper context) {
		BlockPos basePos = new BlockPos(0, 1, 0);
		context.setBlock(basePos, Blocks.BEEHIVE);
		context.spawn(EntityType.BEE, basePos.above());

		ServerPlayer fakePlayer = FakePlayer.get(context.getLevel());

		BlockPos fakePlayerPos = context.absolutePos(basePos.offset(2, 0, 2));
		fakePlayer.setPos(fakePlayerPos.getX(), fakePlayerPos.getY(), fakePlayerPos.getZ());

		context.assertTrue(fakePlayer.gameMode.destroyBlock(context.absolutePos(basePos)), Component.literal("Block was not broken"));
		context.assertBlockPresent(Blocks.AIR, basePos);
		context.succeed();
	}
}
