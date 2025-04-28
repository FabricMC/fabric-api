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

package net.fabricmc.fabric.test.object.builder;

import java.util.Optional;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.data.TrackedDataHandler;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.Item;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.GlobalPos;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityType;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricTrackedDataRegistry;
import net.fabricmc.loader.api.FabricLoader;

public class EntityTrackedDataTest implements ModInitializer {
	private static final Identifier GLOBAL_POS_ID = ObjectBuilderTestConstants.id("global_pos");
	static TrackedDataHandler<GlobalPos> GLOBAL_POS = TrackedDataHandler.create(GlobalPos.PACKET_CODEC);

	private static final Identifier ITEM_ID = ObjectBuilderTestConstants.id("item");
	static TrackedDataHandler<Item> ITEM = TrackedDataHandler.create(PacketCodecs.registryValue(RegistryKeys.ITEM));

	private static final Identifier OPTIONAL_DYE_COLOR_ID = ObjectBuilderTestConstants.id("optional_dye_color");
	static TrackedDataHandler<Optional<DyeColor>> OPTIONAL_DYE_COLOR = TrackedDataHandler.create(DyeColor.PACKET_CODEC.collect(PacketCodecs::optional));

	private static final RegistryKey<EntityType<?>> TRACK_STACK_KEY = RegistryKey.of(RegistryKeys.ENTITY_TYPE, ObjectBuilderTestConstants.id("track_stack"));
	public static EntityType<TrackStackEntity> TRACK_STACK_ENTITY = FabricEntityType.Builder.createMob(TrackStackEntity::new, SpawnGroup.MISC, builder -> builder.defaultAttributes(MobEntity::createMobAttributes))
			.dimensions(0.4f, 2.8f)
			.maxTrackingRange(10)
			.build(TRACK_STACK_KEY);

	@Override
	public void onInitialize() {
		// Register in a different order between a client and dedicated server
		if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
			FabricTrackedDataRegistry.register(GLOBAL_POS_ID, GLOBAL_POS);
			FabricTrackedDataRegistry.register(ITEM_ID, ITEM);
			FabricTrackedDataRegistry.register(OPTIONAL_DYE_COLOR_ID, OPTIONAL_DYE_COLOR);
		} else {
			FabricTrackedDataRegistry.register(ITEM_ID, ITEM);
			FabricTrackedDataRegistry.register(OPTIONAL_DYE_COLOR_ID, OPTIONAL_DYE_COLOR);
			FabricTrackedDataRegistry.register(GLOBAL_POS_ID, GLOBAL_POS);
		}

		Registry.register(Registries.ENTITY_TYPE, TRACK_STACK_KEY, TRACK_STACK_ENTITY);
	}
}
