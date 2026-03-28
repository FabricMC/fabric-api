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

import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.Bootstrap;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Marker;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BellBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentTarget;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.impl.attachment.AttachmentSavedData;
import net.fabricmc.fabric.impl.attachment.AttachmentSerializingImpl;
import net.fabricmc.fabric.impl.attachment.AttachmentTargetImpl;
import net.fabricmc.fabric.impl.attachment.GlobalAttachmentsImpl;
import net.fabricmc.fabric.impl.attachment.sync.AttachmentChange;
import net.fabricmc.fabric.impl.attachment.sync.AttachmentSyncException;
import net.fabricmc.fabric.impl.attachment.sync.AttachmentTargetInfo;

public class CommonAttachmentTests {
	private static final String MOD_ID = "example";
	private static final AttachmentType<Integer> PERSISTENT = AttachmentRegistry.createPersistent(
			Identifier.fromNamespaceAndPath(MOD_ID, "persistent"),
			Codec.INT
	);
	private static final AttachmentType<Integer> SYNCED = AttachmentRegistry.create(
			Identifier.fromNamespaceAndPath(MOD_ID, "synced"),
			builder -> {
				builder.syncWith(ByteBufCodecs.INT, AttachmentSyncPredicate.all());
			}
	);

	private static final AttachmentType<WheelInfo> WHEEL = AttachmentRegistry.create(Identifier.fromNamespaceAndPath(AttachmentTestMod.MOD_ID, "wheel_info"),
			attachment -> attachment
					.initializer(() -> new WheelInfo(100, 5432, 37))
					.persistent(WheelInfo.CODEC)
	);

	@BeforeAll
	static void beforeAll() {
		SharedConstants.tryDetectVersion();
		Bootstrap.bootStrap();
	}

	private static <T> T mockAndDisableSync(Class<T> cl) {
		T target = mock(cl, CALLS_REAL_METHODS);
		doReturn(false).when((AttachmentTargetImpl) target).fabric_shouldTryToSync();
		return target;
	}

	@Test
	void testTargets() {
		AttachmentType<String> basic = AttachmentRegistry.create(Identifier.fromNamespaceAndPath(MOD_ID, "basic_attachment"));
		// Attachment targets
		/*
		 * CALLS_REAL_METHODS makes sense here because AttachmentTarget does not refer to anything in the underlying
		 * class, and it saves us a lot of pain trying to get the regular constructors for ServerLevel and LevelChunk to work.
		 */
		GlobalAttachmentsImpl globalAttachments = mockAndDisableSync(GlobalAttachmentsImpl.class);
		ServerLevel serverLevel = mockAndDisableSync(ServerLevel.class);
		Entity entity = mockAndDisableSync(Entity.class);
		BlockEntity blockEntity = mockAndDisableSync(BlockEntity.class);

		LevelChunk levelChunk = mockAndDisableSync(LevelChunk.class);
		levelChunk.setUnsavedListener(pos -> { });

		ProtoChunk protoChunk = mockAndDisableSync(ProtoChunk.class);

		for (AttachmentTarget target : new AttachmentTarget[]{globalAttachments, serverLevel, entity, blockEntity, levelChunk, protoChunk}) {
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
				Identifier.fromNamespaceAndPath(MOD_ID, "defaulted_attachment"),
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
				Identifier.fromNamespaceAndPath(MOD_ID, "dummy"),
				Codec.DOUBLE
		);
		var map = new IdentityHashMap<AttachmentType<?>, Object>();
		map.put(dummy, 0.5d);
		RegistryAccess ra = mockRA();
		TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, ra);

		AttachmentSerializingImpl.serializeAttachmentData(output, map);
		assertTrue(output.buildResult().contains(AttachmentTarget.NBT_ATTACHMENT_KEY));
		assertTrue(output.buildResult().getCompound(AttachmentTarget.NBT_ATTACHMENT_KEY).orElseThrow().contains(dummy.identifier().toString()));

		map = AttachmentSerializingImpl.deserializeAttachmentData(TagValueInput.create(ProblemReporter.DISCARDING, ra, output.buildResult()));
		assertEquals(1, map.size());
		Map.Entry<AttachmentType<?>, Object> entry = map.entrySet().stream().findFirst().orElseThrow();
		// in this case the key should be the exact same object
		// but in practice this is meaningless because on a dedicated server the JVM restarted
		assertEquals(dummy.identifier(), entry.getKey().identifier());
		assertEquals(0.5d, entry.getValue());
	}

	@Test
	void deserializeNull() {
		var tag = new CompoundTag();
		assertNull(AttachmentSerializingImpl.deserializeAttachmentData(null));

		tag.put(Identifier.withDefaultNamespace("test").toString(), new CompoundTag());
		assertNull(AttachmentSerializingImpl.deserializeAttachmentData(TagValueInput.create(ProblemReporter.DISCARDING, mockRA(), tag)));
	}

	@Test
	void serializeNullOrEmpty() {
		TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, mockRA());
		AttachmentSerializingImpl.serializeAttachmentData(output, null);
		assertFalse(output.buildResult().contains(AttachmentTarget.NBT_ATTACHMENT_KEY));

		output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, mockRA());
		AttachmentSerializingImpl.serializeAttachmentData(output, new IdentityHashMap<>());
		assertFalse(output.buildResult().contains(AttachmentTarget.NBT_ATTACHMENT_KEY));
	}

	@Test
	void testEntityCopy() {
		AttachmentType<Boolean> notCopiedOnRespawn = AttachmentRegistry.create(
				Identifier.fromNamespaceAndPath(MOD_ID, "not_copied_on_respawn")
		);
		AttachmentType<Boolean> copiedOnRespawn = AttachmentRegistry.create(Identifier.fromNamespaceAndPath(MOD_ID, "copied_on_respawn"),
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

	// Test https://github.com/FabricMC/fabric-api/issues/4943#issuecomment-3790935408
	@Test
	void testEntityChangeId() {
		ServerPlayer player = mockAndDisableSync(ServerPlayer.class);
		Entity entity = mockAndDisableSync(Entity.class);
		entity.setAttached(SYNCED, 456);

		entity.setId(123);

		AttachmentTargetImpl targetImpl = (AttachmentTargetImpl) entity;
		targetImpl.fabric_computeInitialSyncChanges(player, change -> {
			assertEquals(123, ((AttachmentTargetInfo.EntityTarget) change.targetInfo()).networkId());
		});
	}

	@Test
	void testEntityPersistence() {
		RegistryAccess ra = mockRA();
		Level mockLevel = mock(Level.class);
		when(mockLevel.registryAccess()).thenReturn(ra);
		Entity entity = new Marker(EntityType.MARKER, mockLevel);
		assertFalse(entity.hasAttached(PERSISTENT));

		int expected = 1;
		entity.setAttached(PERSISTENT, expected);
		TagValueOutput fakeSave = TagValueOutput.createWithoutContext(ProblemReporter.DISCARDING);
		entity.saveWithoutId(fakeSave);

		entity = new Marker(EntityType.MARKER, mockLevel); // fresh object, like on restart
		entity.setLevelCallback(mock());
		entity.load(TagValueInput.create(ProblemReporter.DISCARDING, ra, fakeSave.buildResult()));
		assertTrue(entity.hasAttached(PERSISTENT));
		assertEquals(expected, entity.getAttached(PERSISTENT));
	}

	@Test
	void testBlockEntityPersistence() {
		BlockEntity blockEntity = new BellBlockEntity(BlockPos.ZERO, Blocks.BELL.defaultBlockState());
		assertFalse(blockEntity.hasAttached(PERSISTENT));

		int expected = 1;
		blockEntity.setAttached(PERSISTENT, expected);
		CompoundTag fakeSave = blockEntity.saveWithFullMetadata(mockRA());

		blockEntity = BlockEntity.loadStatic(BlockPos.ZERO, Blocks.BELL.defaultBlockState(), fakeSave, mockRA());
		assertNotNull(blockEntity);
		assertTrue(blockEntity.hasAttached(PERSISTENT));
		assertEquals(expected, blockEntity.getAttached(PERSISTENT));
	}

	@Test
	void testLevelSavedData() {
		// Trying to simulate actual saving and loading for the world is too hard
		RegistryAccess ra = mockRA();

		ServerLevel level = mockAndDisableSync(ServerLevel.class);
		when(level.registryAccess()).thenReturn(ra);

		AttachmentSavedData state = new AttachmentSavedData(level);
		assertFalse(level.hasAttached(PERSISTENT));
		assertFalse(state.isDirty());

		int expected = 1;
		level.setAttached(PERSISTENT, expected);
		assertTrue(state.isDirty());
		CompoundTag fakeSave = (CompoundTag) AttachmentSavedData.codec(level).encodeStart(RegistryOps.create(NbtOps.INSTANCE, ra), state).getOrThrow();
		assertEquals("{\"fabric:attachments\":{\"example:persistent\":1}}", fakeSave.toString());

		level = mockAndDisableSync(ServerLevel.class);
		when(level.registryAccess()).thenReturn(ra);

		AttachmentSavedData.codec(level).decode(RegistryOps.create(NbtOps.INSTANCE, ra), fakeSave).getOrThrow();
		assertTrue(level.hasAttached(PERSISTENT));
		assertEquals(expected, level.getAttached(PERSISTENT));
	}

	@Test
	void testGlobalSavedData() {
		RegistryAccess.Frozen ra = mockFrozenRA();

		MinecraftServer server = mock(MinecraftServer.class);
		GlobalAttachmentsImpl globalAttachments = new GlobalAttachmentsImpl(server);
		when(server.registryAccess()).thenReturn(ra);
		when(server.globalAttachments()).thenReturn(globalAttachments);

		AttachmentSavedData state = new AttachmentSavedData(globalAttachments);
		assertFalse(globalAttachments.hasAttached(PERSISTENT));
		assertFalse(state.isDirty());

		int expected = 1;
		globalAttachments.setAttached(PERSISTENT, expected);
		assertTrue(state.isDirty());
		CompoundTag fakeSave = (CompoundTag) AttachmentSavedData.codec(server).encodeStart(RegistryOps.create(NbtOps.INSTANCE, ra), state).getOrThrow();
		assertEquals("{\"fabric:attachments\":{\"example:persistent\":1}}", fakeSave.toString());

		server = mock(MinecraftServer.class);
		globalAttachments = new GlobalAttachmentsImpl(server);
		when(server.registryAccess()).thenReturn(ra);
		when(server.globalAttachments()).thenReturn(globalAttachments);

		AttachmentSavedData.codec(server).decode(RegistryOps.create(NbtOps.INSTANCE, ra), fakeSave).getOrThrow();
		assertTrue(globalAttachments.hasAttached(PERSISTENT));
		assertEquals(expected, globalAttachments.getAttached(PERSISTENT));
	}

	@Test
	void applyToInvalidTarget() throws AttachmentSyncException {
		RegistryAccess ra = mockRA();

		ServerLevel level = mock(ServerLevel.class);
		when(level.registryAccess()).thenReturn(ra);
		when(level.dimension()).thenReturn(Level.END);

		BlockEntity blockEntity = new ChestBlockEntity(BlockPos.ZERO, Blocks.CHEST.defaultBlockState());

		AttachmentChange attachmentChange = new AttachmentChange(
				((AttachmentTargetImpl) blockEntity).fabric_getSyncTargetInfo(),
				SYNCED,
				new byte[]{0}
		);

		attachmentChange.tryApply(level);
	}

	/*
	 * Chunk serializing is coupled with world saving in ChunkSerializer which is too much of a pain to mock,
	 * so testing is handled by the testmod instead.
	 */

	static RegistryAccess mockRA() {
		RegistryAccess ra = mock(RegistryAccess.class);
		when(ra.createSerializationContext(any())).thenReturn((RegistryOps<Object>) (Object) RegistryOps.create(NbtOps.INSTANCE, ra));
		return ra;
	}

	private static RegistryAccess.Frozen mockFrozenRA() {
		RegistryAccess.Frozen ra = mock(RegistryAccess.Frozen.class);
		when(ra.createSerializationContext(any())).thenReturn((RegistryOps<Object>) (Object) RegistryOps.create(NbtOps.INSTANCE, ra));
		return ra;
	}
}
