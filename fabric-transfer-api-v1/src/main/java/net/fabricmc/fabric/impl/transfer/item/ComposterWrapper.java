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

import com.google.common.collect.MapMaker;
import org.jspecify.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;

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
public class ComposterWrapper extends SnapshotParticipant<Float> {
	// Record is used for convenient constructor, hashcode and equals implementations.
	private record WorldLocation(Level world, BlockPos pos) {
		private BlockState getBlockState() {
			return world.getBlockState(pos);
		}

		private void setBlockState(BlockState state) {
			world.setBlockAndUpdate(pos, state);
		}

		@Override
		public String toString() {
			return DebugMessages.forGlobalPos(world, pos);
		}
	}

	// Weak values to make sure wrappers are cleaned up after use, thread-safe.
	// The two storages strongly reference the containing wrapper, so we are alright with weak values.
	private static final Map<WorldLocation, ComposterWrapper> COMPOSTERS = new MapMaker().concurrencyLevel(1).weakValues().makeMap();

	@Nullable
	public static Storage<ItemVariant> get(Level world, BlockPos pos, @Nullable Direction direction) {
		if (direction != null && direction.getAxis().isVertical()) {
			WorldLocation location = new WorldLocation(world, pos.immutable());
			ComposterWrapper composterWrapper = COMPOSTERS.computeIfAbsent(location, ComposterWrapper::new);
			return direction == UP ? composterWrapper.upStorage : composterWrapper.downStorage;
		} else {
			return null;
		}
	}

	private static final float DO_NOTHING = 0f;
	private static final float EXTRACT_BONEMEAL = -1f;

	private final WorldLocation location;
	// -1 if bonemeal was extracted, otherwise the composter increase probability of the (pending) inserted item.
	private Float increaseProbability = DO_NOTHING;
	private final TopStorage upStorage = new TopStorage();
	private final BottomStorage downStorage = new BottomStorage();

	private ComposterWrapper(WorldLocation location) {
		this.location = location;
	}

	@Override
	protected Float createSnapshot() {
		return increaseProbability;
	}

	@Override
	protected void readSnapshot(Float snapshot) {
		// Reset after unsuccessful commit.
		increaseProbability = snapshot;
	}

	@Override
	protected void onFinalCommit() {
		// Apply pending action
		if (increaseProbability == EXTRACT_BONEMEAL) {
			// Mimic ComposterBlock#emptyComposter logic.
			location.setBlockState(location.getBlockState().setValue(ComposterBlock.LEVEL, 0));
			// Play the sound
			location.world.playSound(null, location.pos, SoundEvents.COMPOSTER_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
		} else if (increaseProbability > 0) {
			BlockState state = location.getBlockState();
			// Always increment on first insert (like vanilla).
			boolean increaseSuccessful = state.getValue(ComposterBlock.LEVEL) == 0 || location.world.getRandom().nextDouble() < increaseProbability;

			if (increaseSuccessful) {
				// Mimic ComposterBlock#addToComposter logic.
				int newLevel = state.getValue(ComposterBlock.LEVEL) + 1;
				BlockState newState = state.setValue(ComposterBlock.LEVEL, newLevel);
				location.setBlockState(newState);

				if (newLevel == 7) {
					location.world.scheduleTick(location.pos, state.getBlock(), 20);
				}
			}

			location.world.levelEvent(LevelEvent.COMPOSTER_FILL, location.pos, increaseSuccessful ? 1 : 0);
		}

		// Reset after successful commit.
		increaseProbability = DO_NOTHING;
	}

	private class TopStorage implements InsertionOnlyStorage<ItemVariant> {
		@Override
		public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
			StoragePreconditions.notBlankNotNegative(resource, maxAmount);

			// Check amount.
			if (maxAmount < 1) return 0;
			// Check that no action is scheduled.
			if (increaseProbability != DO_NOTHING) return 0;
			// Check that the composter can accept items.
			if (location.getBlockState().getValue(ComposterBlock.LEVEL) >= 7) return 0;
			// Check that the item is compostable.
			float insertedIncreaseProbability = ComposterBlock.COMPOSTABLES.getFloat(resource.getItem());
			if (insertedIncreaseProbability <= 0) return 0;

			// Schedule insertion.
			updateSnapshots(transaction);
			increaseProbability = insertedIncreaseProbability;
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
			return increaseProbability == DO_NOTHING && location.getBlockState().getValue(ComposterBlock.LEVEL) == 8;
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
			increaseProbability = EXTRACT_BONEMEAL;
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
}
