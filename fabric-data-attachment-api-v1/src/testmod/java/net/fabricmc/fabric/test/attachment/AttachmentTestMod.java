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

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;

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
			Identifier.of(MOD_ID, "persistent"),
			Codec.STRING
	);
	public static final AttachmentType<String> FEATURE_ATTACHMENT = AttachmentRegistry.create(
			Identifier.of(MOD_ID, "feature")
	);
	public static final AttachmentType<Boolean> SYNCED_WITH_ALL = AttachmentRegistry.create(
			Identifier.of(MOD_ID, "synced_all"),
			builder -> builder
					.initializer(() -> false)
					.persistent(Codec.BOOL)
					.syncWith(PacketCodecs.BOOLEAN, AttachmentSyncPredicate.all())
	);
	public static final AttachmentType<Boolean> SYNCED_WITH_TARGET = AttachmentRegistry.create(
			Identifier.of(MOD_ID, "synced_target"),
			builder -> builder
					.initializer(() -> false)
					.persistent(Codec.BOOL)
					.syncWith(PacketCodecs.BOOLEAN, AttachmentSyncPredicate.targetOnly())
	);
	public static final AttachmentType<Boolean> SYNCED_EXCEPT_TARGET = AttachmentRegistry.create(
			Identifier.of(MOD_ID, "synced_except_target"),
			builder -> builder
					.initializer(() -> false)
					.persistent(Codec.BOOL)
					.syncWith(PacketCodecs.BOOLEAN, AttachmentSyncPredicate.allButTarget())
	);
	public static final AttachmentType<Boolean> SYNCED_CREATIVE_ONLY = AttachmentRegistry.create(
			Identifier.of(MOD_ID, "synced_creative"),
			builder -> builder
					.initializer(() -> false)
					.persistent(Codec.BOOL)
					.syncWith(PacketCodecs.BOOLEAN, (target, player) -> player.isCreative())
	);
	public static final AttachmentType<ItemStack> SYNCED_ITEM = AttachmentRegistry.create(
			Identifier.of(MOD_ID, "synced_item"),
			builder -> builder
					.initializer(() -> ItemStack.EMPTY)
					.persistent(ItemStack.CODEC)
					.syncWith(ItemStack.OPTIONAL_PACKET_CODEC, AttachmentSyncPredicate.all())
	);
	public static final AttachmentType<Integer> SYNCED_RENDER_DISTANCE = AttachmentRegistry.create(
			Identifier.of(MOD_ID, "synced_render_distance"),
			builder -> builder
					.persistent(Codecs.NON_NEGATIVE_INT)
					.syncWith(PacketCodecs.INTEGER, AttachmentSyncPredicate.targetOnly())
	);

	@Override
	public void onInitialize() {
		Registry.register(Registries.FEATURE, Identifier.of(MOD_ID, "set_attachment"), new SetAttachmentFeature(DefaultFeatureConfig.CODEC));

		BiomeModifications.addFeature(
				BiomeSelectors.foundInOverworld(),
				GenerationStep.Feature.VEGETAL_DECORATION,
				RegistryKey.of(RegistryKeys.PLACED_FEATURE, Identifier.of(MOD_ID, "set_attachment"))
		);

		UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
			if (player.getStackInHand(hand).getItem() == Items.CARROT) {
				BlockEntity blockEntity = world.getBlockEntity(hitResult.getBlockPos());

				if (blockEntity != null) {
					blockEntity.setAttached(SYNCED_WITH_ALL, true);
					player.sendMessage(Text.literal("Attached"), false);
					return ActionResult.SUCCESS;
				}
			}

			return ActionResult.PASS;
		});

		ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
			entity.onAttachedSet(SYNCED_ITEM).register((oldValue, newValue) -> {
				if (newValue != null && !newValue.equals(oldValue) && newValue.isOf(Items.BRICK)) {
					entity.damage(world, world.getDamageSources().generic(), 1);
				}
			});
		});
	}
}
