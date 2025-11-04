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

import com.mojang.serialization.Codec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;

public class AttachmentTestMod implements ModInitializer {
	public static final String MOD_ID = "fabric-data-attachment-api-v1-testmod";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static final AttachmentType<String> PERSISTENT = AttachmentRegistry.createPersistent(
			Identifier.fromNamespaceAndPath(MOD_ID, "persistent"),
			Codec.STRING
	);
	public static final AttachmentType<String> FEATURE_ATTACHMENT = AttachmentRegistry.create(
			Identifier.fromNamespaceAndPath(MOD_ID, "feature")
	);
	public static final AttachmentType<Boolean> SYNCED_WITH_ALL = AttachmentRegistry.create(
			Identifier.fromNamespaceAndPath(MOD_ID, "synced_all"),
			builder -> builder
					.initializer(() -> false)
					.persistent(Codec.BOOL)
					.syncWith(ByteBufCodecs.BOOL, AttachmentSyncPredicate.all())
	);
	public static final AttachmentType<Boolean> SYNCED_WITH_TARGET = AttachmentRegistry.create(
			Identifier.fromNamespaceAndPath(MOD_ID, "synced_target"),
			builder -> builder
					.initializer(() -> false)
					.persistent(Codec.BOOL)
					.syncWith(ByteBufCodecs.BOOL, AttachmentSyncPredicate.targetOnly())
	);
	public static final AttachmentType<Boolean> SYNCED_EXCEPT_TARGET = AttachmentRegistry.create(
			Identifier.fromNamespaceAndPath(MOD_ID, "synced_except_target"),
			builder -> builder
					.initializer(() -> false)
					.persistent(Codec.BOOL)
					.syncWith(ByteBufCodecs.BOOL, AttachmentSyncPredicate.allButTarget())
	);
	public static final AttachmentType<Boolean> SYNCED_CREATIVE_ONLY = AttachmentRegistry.create(
			Identifier.fromNamespaceAndPath(MOD_ID, "synced_creative"),
			builder -> builder
					.initializer(() -> false)
					.persistent(Codec.BOOL)
					.syncWith(ByteBufCodecs.BOOL, (target, player) -> player.isCreative())
	);
	public static final AttachmentType<ItemStack> SYNCED_ITEM = AttachmentRegistry.create(
			Identifier.fromNamespaceAndPath(MOD_ID, "synced_item"),
			builder -> builder
					.initializer(() -> ItemStack.EMPTY)
					.persistent(ItemStack.CODEC)
					.syncWith(ItemStack.OPTIONAL_STREAM_CODEC, AttachmentSyncPredicate.all())
	);
	public static final AttachmentType<Integer> SYNCED_RENDER_DISTANCE = AttachmentRegistry.create(
			Identifier.fromNamespaceAndPath(MOD_ID, "synced_render_distance"),
			builder -> builder
					.persistent(ExtraCodecs.NON_NEGATIVE_INT)
					.syncWith(ByteBufCodecs.INT, AttachmentSyncPredicate.targetOnly())
	);

	@Override
	public void onInitialize() {
		Registry.register(BuiltInRegistries.FEATURE, Identifier.fromNamespaceAndPath(MOD_ID, "set_attachment"), new SetAttachmentFeature(NoneFeatureConfiguration.CODEC));

		BiomeModifications.addFeature(
				BiomeSelectors.foundInOverworld(),
				GenerationStep.Decoration.VEGETAL_DECORATION,
				ResourceKey.create(Registries.PLACED_FEATURE, Identifier.fromNamespaceAndPath(MOD_ID, "set_attachment"))
		);

		UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
			if (player.getItemInHand(hand).getItem() == Items.CARROT) {
				BlockEntity blockEntity = world.getBlockEntity(hitResult.getBlockPos());

				if (blockEntity != null) {
					blockEntity.setAttached(SYNCED_WITH_ALL, true);
					player.displayClientMessage(Component.literal("Attached"), false);
					return InteractionResult.SUCCESS;
				}
			}

			return InteractionResult.PASS;
		});

		ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
			entity.onAttachedSet(SYNCED_ITEM).register((oldValue, newValue) -> {
				if (newValue != null && !newValue.equals(oldValue) && newValue.is(Items.BRICK)) {
					entity.hurtServer(world, world.damageSources().generic(), 1);
				}
			});
		});
	}
}
