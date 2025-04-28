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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.World;

public class TrackStackEntity extends MobEntity {
	private static final TrackedData<GlobalPos> GLOBAL_POS = DataTracker.registerData(TrackStackEntity.class, EntityTrackedDataTest.GLOBAL_POS);
	private static final TrackedData<Item> ITEM = DataTracker.registerData(TrackStackEntity.class, EntityTrackedDataTest.ITEM);
	private static final TrackedData<Optional<DyeColor>> OPTIONAL_DYE_COLOR = DataTracker.registerData(TrackStackEntity.class, EntityTrackedDataTest.OPTIONAL_DYE_COLOR);

	public TrackStackEntity(EntityType<? extends TrackStackEntity> entityType, World world) {
		super(entityType, world);
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return SoundEvents.ENTITY_CAT_AMBIENT;
	}

	public Iterable<Text> getLabelLines() {
		List<Text> lines = new ArrayList<>();

		// Get tracked data from data tracker
		GlobalPos globalPos = this.dataTracker.get(GLOBAL_POS);
		Item item = this.dataTracker.get(ITEM);
		Optional<DyeColor> optionalDyeColor = this.dataTracker.get(OPTIONAL_DYE_COLOR);

		// Add in reverse order
		lines.add(optionalDyeColor.map(color -> {
			return Text.literal(color.toString());
		}).orElseGet(() -> {
			return Text.literal("<empty>");
		}).formatted(Formatting.BLACK));

		lines.add(item.getName().copy().formatted(Formatting.DARK_PURPLE));
		lines.add(Text.literal(globalPos.dimension().getValue().toString()));
		lines.add(Text.translatable("chat.coordinates", globalPos.pos().getX(), globalPos.pos().getY(), globalPos.pos().getZ()).formatted(Formatting.YELLOW));

		lines.add(Text.empty());
		lines.add(this.getName().copy().formatted(Formatting.GOLD, Formatting.BOLD));

		return lines;
	}

	@Override
	protected ActionResult interactMob(PlayerEntity player, Hand hand) {
		GlobalPos globalPos = GlobalPos.create(player.getWorld().getRegistryKey(), player.getBlockPos());
		this.dataTracker.set(GLOBAL_POS, globalPos);

		Item item = player.getStackInHand(hand).getItem();
		this.dataTracker.set(ITEM, item);

		if (!player.getWorld().isClient) {
			DyeColor[] colors = DyeColor.values();
			Optional<DyeColor> color = Optional.of(colors[this.getRandom().nextBetweenExclusive(0, colors.length)]);
			this.dataTracker.set(OPTIONAL_DYE_COLOR, color);
		}

		return ActionResult.SUCCESS;
	}

	@Override
	protected void initDataTracker(DataTracker.Builder builder) {
		super.initDataTracker(builder);

		builder.add(GLOBAL_POS, GlobalPos.create(World.OVERWORLD, BlockPos.ORIGIN));
		builder.add(ITEM, Items.POTATO);
		builder.add(OPTIONAL_DYE_COLOR, Optional.empty());
	}
}
