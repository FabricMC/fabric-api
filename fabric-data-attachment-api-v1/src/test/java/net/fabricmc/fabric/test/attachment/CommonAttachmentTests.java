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

package net.fabricmc.fabric.test.attachment;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.UnaryOperator;

import com.mojang.serialization.Codec;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.Bootstrap;
import net.minecraft.SharedConstants;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BellBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MarkerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryOps;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ProtoChunk;
import net.minecraft.world.chunk.WorldChunk;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentTarget;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.impl.attachment.AttachmentPersistentState;
import net.fabricmc.fabric.impl.attachment.AttachmentSerializingImpl;
import net.fabricmc.fabric.impl.attachment.AttachmentTargetImpl;

public class CommonAttachmentTests {
	private static final String MOD_ID = "example";
	private static final AttachmentType<Integer> PERSISTENT = AttachmentRegistry.createPersistent(
			Identifier.of(MOD_ID, "persistent"),
			Codec.INT
	);

	private static final AttachmentType<WheelInfo> WHEEL = AttachmentRegistry.create(Identifier.of(AttachmentTestMod.MOD_ID, "wheel_info"),
			attachment -> attachment
					.initializer(() -> new WheelInfo(100, 5432, 37))
					.persistent(WheelInfo.CODEC)
	);

	@BeforeAll
	static void beforeAll() {
		SharedConstants.createGameVersion();
		Bootstrap.initialize();
	}

	private static <T> T mockAndDisableSync(Class<T> cl) {
		T target = mock(cl, CALLS_REAL_METHODS);
		doReturn(false).when((AttachmentTargetImpl) target).fabric_shouldTryToSync();
		return target;
	}

	@Test
	void testTargets() {
		AttachmentType<String> basic = AttachmentRegistry.create(Identifier.of(MOD_ID, "basic_attachment"));
		// Attachment targets
		/*
		 * CALLS_REAL_METHODS makes sense here because AttachmentTarget does not refer to anything in the underlying
		 * class, and it saves us a lot of pain trying to get the regular constructors for ServerWorld and WorldChunk to work.
		 */
		ServerWorld serverWorld = mockAndDisableSync(ServerWorld.class);
		Entity entity = mockAndDisableSync(Entity.class);
		BlockEntity blockEntity = mockAndDisableSync(BlockEntity.class);

		WorldChunk worldChunk = mockAndDisableSync(WorldChunk.class);
		worldChunk.setUnsavedListener(pos -> { });

		ProtoChunk protoChunk = mockAndDisableSync(ProtoChunk.class);

		for (AttachmentTarget target : new AttachmentTarget[]{serverWorld, entity, blockEntity, worldChunk, protoChunk}) {
			testForTarget(target, basic);
		}
	}

	private void testForTarget(AttachmentTarget target, AttachmentType<String> basic) {
		assertFalse(target.hasAttached(basic));
		assertEquals("", target.getAttachedOrElse(basic, ""));
		assertNull(target.getAttached(basic));

		String value = "attached";
		assertEquals(value, target.getAttachedOrSet(basic, value));
		assertTrue(target.hasAttached(basic));
		assertEquals(value, target.getAttached(basic));
		assertDoesNotThrow(() -> target.getAttachedOrThrow(basic));

		UnaryOperator<String> modifier = s -> s + '_';
		String modified = modifier.apply(value);
		target.modifyAttached(basic, modifier);
		assertEquals(modified, target.getAttached(basic));
		assertEquals(modified, target.removeAttached(basic));
		assertFalse(target.hasAttached(basic));
		assertThrows(NullPointerException.class, () -> target.getAttachedOrThrow(basic));
	}

	@Test
	void testDefaulted() {
		AttachmentType<Integer> defaulted = AttachmentRegistry.createDefaulted(
				Identifier.of(MOD_ID, "defaulted_attachment"),
				() -> 0
		);
		Entity target = mockAndDisableSync(Entity.class);

		assertFalse(target.hasAttached(defaulted));
		assertEquals(0, target.getAttachedOrCreate(defaulted));
		target.removeAttached(defaulted);
		assertFalse(target.hasAttached(defaulted));
	}

	@Test
	void testStaticReadWrite() {
		AttachmentType<Double> dummy = AttachmentRegistry.createPersistent(
				Identifier.of(MOD_ID, "dummy"),
				Codec.DOUBLE
		);
		var map = new IdentityHashMap<AttachmentType<?>, Object>();
		map.put(dummy, 0.5d);
		var fakeSave = new NbtCompound();

		AttachmentSerializingImpl.serializeAttachmentData(fakeSave, mockDRM(), map);
		assertTrue(fakeSave.contains(AttachmentTarget.NBT_ATTACHMENT_KEY, NbtElement.COMPOUND_TYPE));
		assertTrue(fakeSave.getCompound(AttachmentTarget.NBT_ATTACHMENT_KEY).contains(dummy.identifier().toString()));

		map = AttachmentSerializingImpl.deserializeAttachmentData(fakeSave, mockDRM());
		assertEquals(1, map.size());
		Map.Entry<AttachmentType<?>, Object> entry = map.entrySet().stream().findFirst().orElseThrow();
		// in this case the key should be the exact same object
		// but in practice this is meaningless because on a dedicated server the JVM restarted
		assertEquals(dummy.identifier(), entry.getKey().identifier());
		assertEquals(0.5d, entry.getValue());
	}

	@Test
	void deserializeNull() {
		var nbt = new NbtCompound();
		assertNull(AttachmentSerializingImpl.deserializeAttachmentData(nbt, mockDRM()));

		nbt.put(Identifier.ofVanilla("test").toString(), new NbtCompound());
		assertNull(AttachmentSerializingImpl.deserializeAttachmentData(nbt, mockDRM()));
	}

	@Test
	void serializeNullOrEmpty() {
		var nbt = new NbtCompound();
		AttachmentSerializingImpl.serializeAttachmentData(nbt, mockDRM(), null);
		assertFalse(nbt.contains(AttachmentTarget.NBT_ATTACHMENT_KEY));

		AttachmentSerializingImpl.serializeAttachmentData(nbt, mockDRM(), new IdentityHashMap<>());
		assertFalse(nbt.contains(AttachmentTarget.NBT_ATTACHMENT_KEY));
	}

	@Test
	void testEntityCopy() {
		AttachmentType<Boolean> notCopiedOnRespawn = AttachmentRegistry.create(
				Identifier.of(MOD_ID, "not_copied_on_respawn")
		);
		AttachmentType<Boolean> copiedOnRespawn = AttachmentRegistry.create(Identifier.of(MOD_ID, "copied_on_respawn"),
				AttachmentRegistry.Builder::copyOnDeath);

		Entity original = mockAndDisableSync(Entity.class);
		original.setAttached(notCopiedOnRespawn, true);
		original.setAttached(copiedOnRespawn, true);

		Entity respawnTarget = mockAndDisableSync(Entity.class);
		Entity nonRespawnTarget = mockAndDisableSync(Entity.class);

		AttachmentTargetImpl.transfer(original, respawnTarget, true);
		AttachmentTargetImpl.transfer(original, nonRespawnTarget, false);
		assertTrue(respawnTarget.hasAttached(copiedOnRespawn));
		assertFalse(respawnTarget.hasAttached(notCopiedOnRespawn));
		assertTrue(nonRespawnTarget.hasAttached(copiedOnRespawn));
		assertTrue(nonRespawnTarget.hasAttached(notCopiedOnRespawn));
	}

	@Test
	void testEntityPersistence() {
		DynamicRegistryManager drm = mockDRM();
		World mockWorld = mock(World.class);
		when(mockWorld.getRegistryManager()).thenReturn(drm);
		Entity entity = new MarkerEntity(EntityType.MARKER, mockWorld);
		assertFalse(entity.hasAttached(PERSISTENT));

		int expected = 1;
		entity.setAttached(PERSISTENT, expected);
		NbtCompound fakeSave = new NbtCompound();
		entity.writeNbt(fakeSave);

		entity = new MarkerEntity(EntityType.MARKER, mockWorld); // fresh object, like on restart
		entity.setChangeListener(mock());
		entity.readNbt(fakeSave);
		assertTrue(entity.hasAttached(PERSISTENT));
		assertEquals(expected, entity.getAttached(PERSISTENT));
	}

	@Test
	void testBlockEntityPersistence() {
		BlockEntity blockEntity = new BellBlockEntity(BlockPos.ORIGIN, Blocks.BELL.getDefaultState());
		assertFalse(blockEntity.hasAttached(PERSISTENT));

		int expected = 1;
		blockEntity.setAttached(PERSISTENT, expected);
		NbtCompound fakeSave = blockEntity.createNbtWithId(mockDRM());

		blockEntity = BlockEntity.createFromNbt(BlockPos.ORIGIN, Blocks.BELL.getDefaultState(), fakeSave, mockDRM());
		assertNotNull(blockEntity);
		assertTrue(blockEntity.hasAttached(PERSISTENT));
		assertEquals(expected, blockEntity.getAttached(PERSISTENT));
	}

	@Test
	void testWorldPersistentState() {
		// Trying to simulate actual saving and loading for the world is too hard
		ServerWorld world = mockAndDisableSync(ServerWorld.class);
		AttachmentPersistentState state = new AttachmentPersistentState(world);
		assertFalse(world.hasAttached(PERSISTENT));

		int expected = 1;
		world.setAttached(PERSISTENT, expected);
		NbtCompound fakeSave = state.writeNbt(new NbtCompound(), mockDRM());

		world = mockAndDisableSync(ServerWorld.class);
		AttachmentPersistentState.read(world, fakeSave, mockDRM());
		assertTrue(world.hasAttached(PERSISTENT));
		assertEquals(expected, world.getAttached(PERSISTENT));
	}

	/*
	 * Chunk serializing is coupled with world saving in ChunkSerializer which is too much of a pain to mock,
	 * so testing is handled by the testmod instead.
	 */

	private static DynamicRegistryManager mockDRM() {
		DynamicRegistryManager drm = mock(DynamicRegistryManager.class);
		when(drm.getOps(any())).thenReturn((RegistryOps<Object>) (Object) RegistryOps.of(NbtOps.INSTANCE, drm));
		return drm;
	}
}
