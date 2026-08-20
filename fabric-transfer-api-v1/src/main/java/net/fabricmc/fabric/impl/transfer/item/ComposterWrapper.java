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

package net.fabricmc.fabric.impl.transfer.item;

import static net.minecraft.core.Direction.UP;

import java.util.Map;
import java.util.Optional;

import com.google.common.collect.MapMaker;
import org.jspecify.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.Compostable;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.providers.number.ResolvableNumber;
import net.minecraft.world.phys.Vec3;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.base.ExtractionOnlyStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.InsertionOnlyStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.fabricmc.fabric.impl.transfer.DebugMessages;

/**
 * Implementation of {@code Storage<ItemVariant>} for composters.
 */
public class ComposterWrapper extends SnapshotParticipant<ComposterWrapper.PendingAction> {
	record PendingAction(@Nullable ResolvableNumber compostProvider, boolean extractBonemeal) {
		private static final PendingAction NONE = new PendingAction(null, false);
		private static final PendingAction EXTRACT_BONEMEAL = new PendingAction(null, true);

		private static PendingAction insert(ResolvableNumber compostProvider) {
			return new PendingAction(compostProvider, false);
		}
	}

	// Record is used for convenient constructor, hashcode and equals implementations.
	private record LevelLocation(Level level, BlockPos pos) {
		private BlockState getBlockState() {
			return level.getBlockState(pos);
		}

		private void setBlockState(BlockState state) {
			level.setBlockAndUpdate(pos, state);
		}

		@Override
		public String toString() {
			return DebugMessages.forGlobalPos(level, pos);
		}
	}

	// Weak values to make sure wrappers are cleaned up after use, thread-safe.
	// The two storages strongly reference the containing wrapper, so we are alright with weak values.
	private static final Map<LevelLocation, ComposterWrapper> COMPOSTERS = new MapMaker().concurrencyLevel(1).weakValues().makeMap();

	@Nullable
	public static Storage<ItemVariant> get(Level level, BlockPos pos, @Nullable Direction direction) {
		if (direction != null && direction.getAxis().isVertical()) {
			LevelLocation location = new LevelLocation(level, pos.immutable());
			ComposterWrapper composterWrapper = COMPOSTERS.computeIfAbsent(location, ComposterWrapper::new);
			return direction == UP ? composterWrapper.upStorage : composterWrapper.downStorage;
		} else {
			return null;
		}
	}

	private final LevelLocation location;
	private PendingAction pendingAction = PendingAction.NONE;
	private final TopStorage upStorage = new TopStorage();
	private final BottomStorage downStorage = new BottomStorage();

	private ComposterWrapper(LevelLocation location) {
		this.location = location;
	}

	@Override
	protected PendingAction createSnapshot() {
		return pendingAction;
	}

	@Override
	protected void readSnapshot(PendingAction snapshot) {
		// Reset after unsuccessful commit.
		pendingAction = snapshot;
	}

	@Override
	protected void onFinalCommit() {
		// Apply pending action
		if (pendingAction.extractBonemeal) {
			// Mimic ComposterBlock#emptyComposter logic.
			BlockState newState = location.getBlockState().setValue(ComposterBlock.LEVEL, 0);
			location.setBlockState(newState);
			location.level.gameEvent(GameEvent.BLOCK_CHANGE, location.pos, GameEvent.Context.of(newState));
			// Play the sound
			location.level.playSound(null, location.pos, SoundEvents.COMPOSTER_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
		} else if (pendingAction.compostProvider != null && location.level instanceof ServerLevel serverLevel) {
			BlockState state = location.getBlockState();
			int layersToAdd = getLayersToAdd(serverLevel, location.pos, state, pendingAction.compostProvider);
			boolean increaseSuccessful = layersToAdd > 0;

			if (increaseSuccessful) {
				// Mimic ComposterBlock#addToComposter logic.
				int newLevel = Mth.clamp(state.getValue(ComposterBlock.LEVEL) + layersToAdd, 0, 7);
				BlockState newState = state.setValue(ComposterBlock.LEVEL, newLevel);
				location.setBlockState(newState);
				location.level.gameEvent(GameEvent.BLOCK_CHANGE, location.pos, GameEvent.Context.of(newState));

				if (newLevel == 7) {
					location.level.scheduleTick(location.pos, state.getBlock(), 20);
				}
			}

			location.level.levelEvent(LevelEvent.COMPOSTER_FILL, location.pos, increaseSuccessful ? 1 : 0);
		}

		// Reset after successful commit.
		pendingAction = PendingAction.NONE;
	}

	private class TopStorage implements InsertionOnlyStorage<ItemVariant> {
		@Override
		public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
			StoragePreconditions.notBlankNotNegative(resource, maxAmount);

			// Check amount.
			if (maxAmount < 1) return 0;
			// Check that no action is scheduled.
			if (pendingAction != PendingAction.NONE) return 0;
			// Check that the composter can accept items.
			if (location.getBlockState().getValue(ComposterBlock.LEVEL) >= 7) return 0;
			// Check that the item is compostable.
			Compostable compostable = resource.getComponents().get(DataComponents.COMPOSTABLE);
			if (compostable == null || !(location.level instanceof ServerLevel)) return 0;

			// Schedule insertion.
			updateSnapshots(transaction);
			pendingAction = PendingAction.insert(compostable.layers());
			return 1;
		}

		@Override
		public String toString() {
			return "ComposterWrapper[" + location + "/top]";
		}
	}

	private class BottomStorage implements ExtractionOnlyStorage<ItemVariant>, SingleSlotStorage<ItemVariant> {
		private static final ItemVariant BONE_MEAL = ItemVariant.of(Items.BONE_MEAL);

		private boolean hasBoneMeal() {
			// We only have bone meal if the level is 8 and no action was scheduled.
			return pendingAction == PendingAction.NONE && location.getBlockState().getValue(ComposterBlock.LEVEL) == 8;
		}

		@Override
		public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
			StoragePreconditions.notBlankNotNegative(resource, maxAmount);

			// Check amount.
			if (maxAmount < 1) return 0;
			// Check that the resource is bone meal.
			if (!BONE_MEAL.equals(resource)) return 0;
			// Check that there is bone meal to extract.
			if (!hasBoneMeal()) return 0;

			updateSnapshots(transaction);
			pendingAction = PendingAction.EXTRACT_BONEMEAL;
			return 1;
		}

		@Override
		public boolean isResourceBlank() {
			return getResource().isBlank();
		}

		@Override
		public ItemVariant getResource() {
			return BONE_MEAL;
		}

		@Override
		public long getAmount() {
			return hasBoneMeal() ? 1 : 0;
		}

		@Override
		public long getCapacity() {
			return 1;
		}

		@Override
		public String toString() {
			return "ComposterWrapper[" + location + "/bottom]";
		}
	}

	private static int getLayersToAdd(ServerLevel level, BlockPos pos, BlockState state, ResolvableNumber provider) {
		LootContext lootContext = new LootContext.Builder(
				new LootParams.Builder(level)
						.withParameter(LootContextParams.BLOCK_STATE, state)
						.withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos))
						.create(LootContextParamSets.BLOCK_INTERACT)
		).create(Optional.empty());
		return provider.getInt(lootContext, 0);
	}
}
