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

package net.fabricmc.fabric.test.attachment.gametest;

import java.util.Objects;
import java.util.function.IntSupplier;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.GameTest;
import net.minecraft.test.GameTestException;
import net.minecraft.test.TestContext;
import net.minecraft.util.Identifier;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.fabricmc.fabric.test.attachment.AttachmentTestMod;

public class AttachmentCopyTests implements FabricGameTest {
	// using a lambda type because serialization shouldn't play a role in this
	public static AttachmentType<IntSupplier> DUMMY = AttachmentRegistry.create(
			Identifier.of(AttachmentTestMod.MOD_ID, "dummy")
	);
	public static AttachmentType<IntSupplier> COPY_ON_DEATH = AttachmentRegistry.create(
			Identifier.of(AttachmentTestMod.MOD_ID, "copy_test"),
			AttachmentRegistry.Builder::copyOnDeath
	);

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testCrossWorldTeleport(TestContext context) {
		MinecraftServer server = context.getWorld().getServer();
		ServerWorld overworld = server.getOverworld();
		ServerWorld end = server.getWorld(World.END);
		// using overworld and end to avoid portal code related to the nether

		Entity entity = EntityType.PIG.create(overworld);
		Objects.requireNonNull(entity, "entity was null");
		entity.setAttached(DUMMY, () -> 10);
		entity.setAttached(COPY_ON_DEATH, () -> 10);

		Entity moved = entity.teleportTo(new TeleportTarget(end, entity, TeleportTarget.NO_OP));
		if (moved == null) throw new GameTestException("Cross-world teleportation failed");

		IntSupplier attached1 = moved.getAttached(DUMMY);
		IntSupplier attached2 = moved.getAttached(COPY_ON_DEATH);

		if (attached1 == null || attached1.getAsInt() != 10 || attached2 == null || attached2.getAsInt() != 10) {
			throw new GameTestException("Attachment copying failed during cross-world teleportation");
		}

		moved.discard();
		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testMobConversion(TestContext context) {
		MobEntity mob = Objects.requireNonNull(EntityType.ZOMBIE.create(context.getWorld()));
		mob.setAttached(DUMMY, () -> 42);
		mob.setAttached(COPY_ON_DEATH, () -> 42);
		MobEntity converted = mob.convertTo(EntityType.DROWNED, false);
		if (converted == null) throw new GameTestException("Conversion failed");

		if (converted.hasAttached(DUMMY)) {
			throw new GameTestException("Attachment shouldn't have been copied on mob conversion");
		}

		IntSupplier attached = converted.getAttached(COPY_ON_DEATH);

		if (attached == null || attached.getAsInt() != 42) {
			throw new GameTestException("Attachment copying failed during mob conversion");
		}

		converted.discard();
		context.complete();
	}
}
