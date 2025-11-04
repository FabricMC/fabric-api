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

import net.minecraft.core.GlobalPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityType;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricTrackedDataRegistry;
import net.fabricmc.loader.api.FabricLoader;

public class EntityTrackedDataTest implements ModInitializer {
	private static final Identifier GLOBAL_POS_ID = ObjectBuilderTestConstants.id("global_pos");
	static EntityDataSerializer<GlobalPos> GLOBAL_POS = EntityDataSerializer.forValueType(GlobalPos.STREAM_CODEC);

	private static final Identifier ITEM_ID = ObjectBuilderTestConstants.id("item");
	static EntityDataSerializer<Item> ITEM = EntityDataSerializer.forValueType(ByteBufCodecs.registry(Registries.ITEM));

	private static final Identifier OPTIONAL_DYE_COLOR_ID = ObjectBuilderTestConstants.id("optional_dye_color");
	static EntityDataSerializer<Optional<DyeColor>> OPTIONAL_DYE_COLOR = EntityDataSerializer.forValueType(DyeColor.STREAM_CODEC.apply(ByteBufCodecs::optional));

	private static final ResourceKey<EntityType<?>> TRACK_STACK_KEY = ResourceKey.create(Registries.ENTITY_TYPE, ObjectBuilderTestConstants.id("track_stack"));
	public static EntityType<TrackStackEntity> TRACK_STACK_ENTITY = FabricEntityType.Builder.createMob(TrackStackEntity::new, MobCategory.MISC, builder -> builder.defaultAttributes(Mob::createMobAttributes))
			.sized(0.4f, 2.8f)
			.clientTrackingRange(10)
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

		Registry.register(BuiltInRegistries.ENTITY_TYPE, TRACK_STACK_KEY, TRACK_STACK_ENTITY);
	}
}
