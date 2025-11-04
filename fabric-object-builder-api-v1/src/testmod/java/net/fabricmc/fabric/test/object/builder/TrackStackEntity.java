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

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class TrackStackEntity extends Mob {
	private static final EntityDataAccessor<GlobalPos> GLOBAL_POS = SynchedEntityData.defineId(TrackStackEntity.class, EntityTrackedDataTest.GLOBAL_POS);
	private static final EntityDataAccessor<Item> ITEM = SynchedEntityData.defineId(TrackStackEntity.class, EntityTrackedDataTest.ITEM);
	private static final EntityDataAccessor<Optional<DyeColor>> OPTIONAL_DYE_COLOR = SynchedEntityData.defineId(TrackStackEntity.class, EntityTrackedDataTest.OPTIONAL_DYE_COLOR);

	public TrackStackEntity(EntityType<? extends TrackStackEntity> entityType, Level world) {
		super(entityType, world);
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return SoundEvents.CAT_AMBIENT;
	}

	public Iterable<Component> getLabelLines() {
		List<Component> lines = new ArrayList<>();

		// Get tracked data from data tracker
		GlobalPos globalPos = this.entityData.get(GLOBAL_POS);
		Item item = this.entityData.get(ITEM);
		Optional<DyeColor> optionalDyeColor = this.entityData.get(OPTIONAL_DYE_COLOR);

		// Add in reverse order
		lines.add(optionalDyeColor.map(color -> {
			return Component.literal(color.toString());
		}).orElseGet(() -> {
			return Component.literal("<empty>");
		}).withStyle(ChatFormatting.BLACK));

		lines.add(item.getName().copy().withStyle(ChatFormatting.DARK_PURPLE));
		lines.add(Component.literal(globalPos.dimension().identifier().toString()));
		lines.add(Component.translatable("chat.coordinates", globalPos.pos().getX(), globalPos.pos().getY(), globalPos.pos().getZ()).withStyle(ChatFormatting.YELLOW));

		lines.add(Component.empty());
		lines.add(this.getName().copy().withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));

		return lines;
	}

	@Override
	protected InteractionResult mobInteract(Player player, InteractionHand hand) {
		GlobalPos globalPos = GlobalPos.of(player.level().dimension(), player.blockPosition());
		this.entityData.set(GLOBAL_POS, globalPos);

		Item item = player.getItemInHand(hand).getItem();
		this.entityData.set(ITEM, item);

		if (!player.level().isClientSide()) {
			DyeColor[] colors = DyeColor.values();
			Optional<DyeColor> color = Optional.of(colors[this.getRandom().nextInt(0, colors.length)]);
			this.entityData.set(OPTIONAL_DYE_COLOR, color);
		}

		return InteractionResult.SUCCESS;
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);

		builder.define(GLOBAL_POS, GlobalPos.of(Level.OVERWORLD, BlockPos.ZERO));
		builder.define(ITEM, Items.POTATO);
		builder.define(OPTIONAL_DYE_COLOR, Optional.empty());
	}
}
