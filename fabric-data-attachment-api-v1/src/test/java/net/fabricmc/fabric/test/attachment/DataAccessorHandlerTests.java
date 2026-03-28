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

import static net.fabricmc.fabric.test.attachment.AttachmentTestMod.MOD_ID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.Identifier;
import net.minecraft.server.Bootstrap;
import net.minecraft.server.commands.data.BlockDataAccessor;
import net.minecraft.server.commands.data.DataAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentTarget;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;

public class DataAccessorHandlerTests {
	private static final AttachmentType<Integer> INT = AttachmentRegistry.createPersistent(
			Identifier.fromNamespaceAndPath(MOD_ID, "int"),
			Codec.INT
	);
	private static final AttachmentType<String> STRING = AttachmentRegistry.createPersistent(
			Identifier.fromNamespaceAndPath(MOD_ID, "string"),
			Codec.STRING
	);
	private static final AttachmentType<Boolean> BOOL = AttachmentRegistry.createPersistent(
			Identifier.fromNamespaceAndPath(MOD_ID, "bool"),
			Codec.BOOL
	);

	@BeforeAll
	static void beforeAll() {
		SharedConstants.tryDetectVersion();
		Bootstrap.bootStrap();
	}

	BlockEntity blockEntity;
	BlockDataAccessor dataAccessor;
	AttachmentTarget.OnAttachedSet<Integer> callback;

	@BeforeEach
	void setUp() {
		RegistryAccess ra = CommonAttachmentTests.mockRA();
		this.blockEntity = new BlockEntity(BlockEntityType.CHEST, BlockPos.ZERO, Blocks.CHEST.defaultBlockState()) {};
		Level mockLevel = mock(Level.class);
		when(mockLevel.registryAccess()).thenReturn(ra);
		blockEntity.setLevel(mockLevel);
		this.dataAccessor = new BlockDataAccessor(blockEntity, BlockPos.ZERO);

		this.callback = mock(AttachmentTarget.OnAttachedSet.class);
		blockEntity.onAttachedSet(INT).register(callback);
	}

	private static void merge(DataAccessor dataAccessor, String nbt) throws CommandSyntaxException {
		CompoundTag old = dataAccessor.getData();
		CompoundTag merged = old.copy().merge(TagParser.parseCompoundFully(nbt));
		dataAccessor.setData(merged);
	}

	private static void set(DataAccessor dataAccessor, String nbt) throws CommandSyntaxException {
		dataAccessor.setData(TagParser.parseCompoundFully(nbt));
	}

	@Test
	void setAttachment() throws CommandSyntaxException {
		assertFalse(blockEntity.hasAttached(INT));
		merge(dataAccessor, "{\"fabric:attachments\": {\"fabric-data-attachment-api-v1-testmod:int\": 5}}");
		assertEquals(5, blockEntity.getAttached(INT));
		verify(callback).onAttachedSet(null, 5);
	}

	@Test
	void removeAttachment() throws CommandSyntaxException {
		blockEntity.setAttached(INT, 5);
		reset(callback);

		assertTrue(blockEntity.hasAttached(INT));
		set(dataAccessor, "{}");
		assertFalse(blockEntity.hasAttached(INT));
		verify(callback).onAttachedSet(5, null);
	}

	@Test
	void updateAttachment() throws CommandSyntaxException {
		blockEntity.setAttached(INT, 1);
		reset(callback);

		merge(dataAccessor, "{\"fabric:attachments\": {\"fabric-data-attachment-api-v1-testmod:int\": 5}}");
		assertEquals(5, blockEntity.getAttached(INT));
		verify(callback).onAttachedSet(1, 5);
	}

	@Test
	void noAttachmentsNoOp() throws CommandSyntaxException {
		assertFalse(blockEntity.hasAttached(INT));
		set(dataAccessor, "{}");
		assertFalse(blockEntity.hasAttached(INT));
		verifyNoInteractions(callback);
	}

	@Test
	void clearAllAttachmentsWithEmptyData() throws CommandSyntaxException {
		blockEntity.setAttached(INT, 5);
		reset(callback);

		assertTrue(blockEntity.hasAttached(INT));
		set(dataAccessor, "{}");
		assertFalse(blockEntity.hasAttached(INT));
		verify(callback).onAttachedSet(5, null);
	}

	@Test
	void clearAllAttachmentsWithEmptyAttachments() throws CommandSyntaxException {
		blockEntity.setAttached(INT, 5);
		reset(callback);

		assertTrue(blockEntity.hasAttached(INT));
		set(dataAccessor, "{\"fabric:attachments\": {}}");
		assertFalse(blockEntity.hasAttached(INT));
		verify(callback).onAttachedSet(5, null);
	}

	@Test
	void addMultipleAttachments() throws CommandSyntaxException {
		assertFalse(blockEntity.hasAttached(INT));
		assertFalse(blockEntity.hasAttached(STRING));
		assertFalse(blockEntity.hasAttached(BOOL));

		merge(dataAccessor, "{\"fabric:attachments\": {" +
				"\"fabric-data-attachment-api-v1-testmod:int\": 42, " +
				"\"fabric-data-attachment-api-v1-testmod:string\": \"test\", " +
				"\"fabric-data-attachment-api-v1-testmod:bool\": true" +
				"}}");

		assertEquals(42, blockEntity.getAttached(INT));
		assertEquals("test", blockEntity.getAttached(STRING));
		assertEquals(true, blockEntity.getAttached(BOOL));
	}

	@Test
	void removeSomeAttachmentsKeepOthers() throws CommandSyntaxException {
		blockEntity.setAttached(INT, 10);
		blockEntity.setAttached(STRING, "keep");
		blockEntity.setAttached(BOOL, false);
		reset(callback);

		assertTrue(blockEntity.hasAttached(INT));
		assertTrue(blockEntity.hasAttached(STRING));
		assertTrue(blockEntity.hasAttached(BOOL));

		// Only keep STRING attachment
		set(dataAccessor, "{\"fabric:attachments\": {\"fabric-data-attachment-api-v1-testmod:string\": \"keep\"}}");

		assertFalse(blockEntity.hasAttached(INT));
		assertTrue(blockEntity.hasAttached(STRING));
		assertFalse(blockEntity.hasAttached(BOOL));
		assertEquals("keep", blockEntity.getAttached(STRING));
		verify(callback).onAttachedSet(10, null);
	}

	@Test
	void mixedUpdateAddRemoveUpdate() throws CommandSyntaxException {
		blockEntity.setAttached(INT, 1);
		blockEntity.setAttached(STRING, "old");
		reset(callback);

		assertTrue(blockEntity.hasAttached(INT));
		assertTrue(blockEntity.hasAttached(STRING));
		assertFalse(blockEntity.hasAttached(BOOL));

		// Update INT, remove STRING, add BOOL
		set(dataAccessor, "{\"fabric:attachments\": {" +
				"\"fabric-data-attachment-api-v1-testmod:int\": 999, " +
				"\"fabric-data-attachment-api-v1-testmod:bool\": true" +
				"}}");

		assertEquals(999, blockEntity.getAttached(INT));
		assertFalse(blockEntity.hasAttached(STRING));
		assertEquals(true, blockEntity.getAttached(BOOL));
		verify(callback).onAttachedSet(1, 999);
	}
}
