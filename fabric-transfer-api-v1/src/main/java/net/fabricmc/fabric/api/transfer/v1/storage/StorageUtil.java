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

package net.fabricmc.fabric.api.transfer.v1.storage;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import org.jspecify.annotations.Nullable;

import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.AbstractContainerMenu;

import net.fabricmc.fabric.api.transfer.v1.storage.base.ResourceAmount;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

/**
 * Helper functions to work with {@link Storage}s.
 *
 * <p>Note that the functions that take a predicate iterate over the entire inventory in the worst case.
 * If the resource is known, there will generally be a more performance efficient way.
 */
public final class StorageUtil {
	private StorageUtil() {
	}

	/**
	 * Move resources between two storages, matching the passed filter, and return the amount that was successfully transferred.
	 *
	 * <p>Here is a usage example with fluid variant storages:
	 * <pre>{@code
	 * // Source
	 * Storage<FluidVariant> source;
	 * // Target
	 * Storage<FluidVariant> target;
	 *
	 * // Move up to one bucket in total from source to target, outside of a transaction:
	 * long amountMoved = StorageUtil.move(source, target, variant -> true, FluidConstants.BUCKET, null);
	 * // Move exactly one bucket in total, only of water:
	 * try (Transaction transaction = Transaction.openOuter()) {
	 *     Predicate<FluidVariant> filter = variant -> variant.isOf(Fluids.WATER);
	 *     long waterMoved = StorageUtil.move(source, target, filter, FluidConstants.BUCKET, transaction);
	 *     if (waterMoved == FluidConstants.BUCKET) {
	 *         // Only commit if exactly one bucket was moved (no less!).
	 *         transaction.commit();
	 *     }
	 * }
	 * }</pre>
	 *
	 * @param from The source storage. May be null.
	 * @param to The target storage. May be null.
	 * @param filter The filter for transferred resources.
	 *               Only resources for which this filter returns {@code true} will be transferred.
	 *               This filter will never be tested with a blank resource, and filters are encouraged to throw an
	 *               exception if this guarantee is violated.
	 * @param maxAmount The maximum amount that will be transferred.
	 * @param transaction The transaction this transfer is part of, or {@code null} if a transaction should be opened just for this transfer.
	 * @param <T> The type of resources to move.
	 * @return The total amount of resources that was successfully transferred.
	 * @throws IllegalStateException If no transaction is passed and a transaction is already active on the current thread.
	 */
	public static <T> long move(@Nullable Storage<T> from, @Nullable Storage<T> to, Predicate<T> filter, long maxAmount, @Nullable TransactionContext transaction) {
		Objects.requireNonNull(filter, "Filter may not be null");
		if (from == null || to == null) return 0;

		long totalMoved = 0;

		try (Transaction iterationTransaction = Transaction.openNested(transaction)) {
			for (StorageView<T> view : from.nonEmptyViews()) {
				T resource = view.getResource();
				if (!filter.test(resource)) continue;

				// check how much can be extracted
				long maxExtracted = simulateExtract(view, resource, maxAmount - totalMoved, iterationTransaction);

				try (Transaction transferTransaction = iterationTransaction.openNested()) {
					// check how much can be inserted
					long accepted = to.insert(resource, maxExtracted, transferTransaction);

					// extract it, or rollback if the amounts don't match
					if (view.extract(resource, accepted, transferTransaction) == accepted) {
						totalMoved += accepted;
						transferTransaction.commit();
					}
				}

				if (maxAmount == totalMoved) {
					// early return if nothing can be moved anymore
					iterationTransaction.commit();
					return totalMoved;
				}
			}

			iterationTransaction.commit();
		} catch (Exception e) {
			CrashReport report = CrashReport.forThrowable(e, "Moving resources between storages");
			report.addCategory("Move details")
					.setDetail("Input storage", from::toString)
					.setDetail("Output storage", to::toString)
					.setDetail("Filter", filter::toString)
					.setDetail("Max amount", maxAmount)
					.setDetail("Transaction", transaction);
			throw new ReportedException(report);
		}

		return totalMoved;
	}

	/**
	 * Convenient helper to simulate an insertion, i.e. get the result of insert without modifying any state.
	 * The passed transaction may be null if a new transaction should be opened for the simulation.
	 * @see Storage#insert
	 */
	public static <T> long simulateInsert(Storage<T> storage, T resource, long maxAmount, @Nullable TransactionContext transaction) {
		try (Transaction simulateTransaction = Transaction.openNested(transaction)) {
			return storage.insert(resource, maxAmount, simulateTransaction);
		}
	}

	/**
	 * Convenient helper to simulate an extraction, i.e. get the result of extract without modifying any state.
	 * The passed transaction may be null if a new transaction should be opened for the simulation.
	 * @see Storage#insert
	 */
	public static <T> long simulateExtract(Storage<T> storage, T resource, long maxAmount, @Nullable TransactionContext transaction) {
		try (Transaction simulateTransaction = Transaction.openNested(transaction)) {
			return storage.extract(resource, maxAmount, simulateTransaction);
		}
	}

	/**
	 * Convenient helper to simulate an extraction, i.e. get the result of extract without modifying any state.
	 * The passed transaction may be null if a new transaction should be opened for the simulation.
	 * @see Storage#insert
	 */
	public static <T> long simulateExtract(StorageView<T> storageView, T resource, long maxAmount, @Nullable TransactionContext transaction) {
		try (Transaction simulateTransaction = Transaction.openNested(transaction)) {
			return storageView.extract(resource, maxAmount, simulateTransaction);
		}
	}

	/**
	 * Convenient helper to simulate an extraction, i.e. get the result of extract without modifying any state.
	 * The passed transaction may be null if a new transaction should be opened for the simulation.
	 * @see Storage#insert
	 * @apiNote This function handles the method overload conflict for objects that implement both {@link Storage} and {@link StorageView}.
	 */
	// Object & is used to have a different erasure than the other overloads.
	public static <T, S extends Object & Storage<T> & StorageView<T>> long simulateExtract(S storage, T resource, long maxAmount, @Nullable TransactionContext transaction) {
		try (Transaction simulateTransaction = Transaction.openNested(transaction)) {
			return storage.extract(resource, maxAmount, simulateTransaction);
		}
	}

	/**
	 * Try to extract any resource from a storage, up to a maximum amount.
	 *
	 * <p>This function will only ever pull from one storage view of the storage, even if multiple storage views contain the same resource.
	 *
	 * @param storage The storage, may be null.
	 * @param maxAmount The maximum to extract.
	 * @param transaction The transaction this operation is part of.
	 * @return A non-blank resource and the strictly positive amount of it that was extracted from the storage,
	 * or {@code null} if none could be found.
	 */
	@Nullable
	public static <T> ResourceAmount<T> extractAny(@Nullable Storage<T> storage, long maxAmount, TransactionContext transaction) {
		StoragePreconditions.notNegative(maxAmount);

		if (storage == null) return null;

		try {
			for (StorageView<T> view : storage.nonEmptyViews()) {
				T resource = view.getResource();
				long amount = view.extract(resource, maxAmount, transaction);
				if (amount > 0) return new ResourceAmount<>(resource, amount);
			}
		} catch (Exception e) {
			CrashReport report = CrashReport.forThrowable(e, "Extracting resources from storage");
			report.addCategory("Extraction details")
					.setDetail("Storage", storage::toString)
					.setDetail("Max amount", maxAmount)
					.setDetail("Transaction", transaction);
			throw new ReportedException(report);
		}

		return null;
	}

	/**
	 * Try to insert up to some amount of a resource into a list of storage slots, trying to "stack" first,
	 * i.e. prioritizing slots that already contain the resource.
	 *
	 * @return How much was inserted.
	 * @see Storage#insert
	 */
	public static <T> long insertStacking(List<? extends SingleSlotStorage<T>> slots, T resource, long maxAmount, TransactionContext transaction) {
		StoragePreconditions.notNegative(maxAmount);
		long amount = 0;

		try {
			for (SingleSlotStorage<T> slot : slots) {
				if (!slot.isResourceBlank()) {
					amount += slot.insert(resource, maxAmount - amount, transaction);
					if (amount == maxAmount) return amount;
				}
			}

			for (SingleSlotStorage<T> slot : slots) {
				amount += slot.insert(resource, maxAmount - amount, transaction);
				if (amount == maxAmount) return amount;
			}
		} catch (Exception e) {
			CrashReport report = CrashReport.forThrowable(e, "Inserting resources into slots");
			report.addCategory("Slotted insertion details")
					.setDetail("Slots", () -> Objects.toString(slots, null))
					.setDetail("Resource", () -> Objects.toString(resource, null))
					.setDetail("Max amount", maxAmount)
					.setDetail("Transaction", transaction);
			throw new ReportedException(report);
		}

		return amount;
	}

	/**
	 * Insert resources in a storage, attempting to stack them with existing resources first if possible.
	 *
	 * @param storage The storage, may be null.
	 * @param resource The resource to insert. May not be blank.
	 * @param maxAmount The maximum amount of resource to insert. May not be negative.
	 * @param transaction The transaction this operation is part of.
	 * @return A nonnegative integer not greater than maxAmount: the amount that was inserted.
	 */
	public static <T> long tryInsertStacking(@Nullable Storage<T> storage, T resource, long maxAmount, TransactionContext transaction) {
		StoragePreconditions.notNegative(maxAmount);

		try {
			if (storage instanceof SlottedStorage<T> slottedStorage) {
				return insertStacking(slottedStorage.getSlots(), resource, maxAmount, transaction);
			} else if (storage != null) {
				return storage.insert(resource, maxAmount, transaction);
			} else {
				return 0;
			}
		} catch (Exception e) {
			CrashReport report = CrashReport.forThrowable(e, "Inserting resources into a storage");
			report.addCategory("Insertion details")
					.setDetail("Storage", () -> Objects.toString(storage, null))
					.setDetail("Resource", () -> Objects.toString(resource, null))
					.setDetail("Max amount", maxAmount)
					.setDetail("Transaction", transaction);
			throw new ReportedException(report);
		}
	}

	/**
	 * Attempt to find a resource stored in the passed storage.
	 *
	 * @see #findStoredResource(Storage, Predicate)
	 * @return A non-blank resource stored in the storage, or {@code null} if none could be found.
	 */
	@Nullable
	public static <T> T findStoredResource(@Nullable Storage<T> storage) {
		return findStoredResource(storage, r -> true);
	}

	/**
	 * Attempt to find a resource stored in the passed storage that matches the passed filter.
	 *
	 * @param storage The storage to inspect, may be null.
	 * @param filter The filter. Only a resource for which this filter returns {@code true} will be returned.
	 * @param <T> The type of the stored resources.
	 * @return A non-blank resource stored in the storage that matches the filter, or {@code null} if none could be found.
	 */
	@Nullable
	public static <T> T findStoredResource(@Nullable Storage<T> storage, Predicate<T> filter) {
		Objects.requireNonNull(filter, "Filter may not be null");
		if (storage == null) return null;

		for (StorageView<T> view : storage.nonEmptyViews()) {
			if (filter.test(view.getResource())) {
				return view.getResource();
			}
		}

		return null;
	}

	/**
	 * Attempt to find a resource stored in the passed storage that can be extracted.
	 *
	 * @see #findExtractableResource(Storage, Predicate, TransactionContext)
	 * @return A non-blank resource stored in the storage that can be extracted, or {@code null} if none could be found.
	 */
	@Nullable
	public static <T> T findExtractableResource(@Nullable Storage<T> storage, @Nullable TransactionContext transaction) {
		return findExtractableResource(storage, r -> true, transaction);
	}

	/**
	 * Attempt to find a resource stored in the passed storage that matches the passed filter and can be extracted.
	 *
	 * @param storage The storage to inspect, may be null.
	 * @param filter The filter. Only a resource for which this filter returns {@code true} will be returned.
	 * @param transaction The current transaction, or {@code null} if a transaction should be opened for this query.
	 * @param <T> The type of the stored resources.
	 * @return A non-blank resource stored in the storage that matches the filter and can be extracted, or {@code null} if none could be found.
	 */
	@Nullable
	public static <T> T findExtractableResource(@Nullable Storage<T> storage, Predicate<T> filter, @Nullable TransactionContext transaction) {
		Objects.requireNonNull(filter, "Filter may not be null");
		if (storage == null) return null;

		try (Transaction nested = Transaction.openNested(transaction)) {
			for (StorageView<T> view : storage.nonEmptyViews()) {
				// Extract below could change the resource, so we have to query it before extracting.
				T resource = view.getResource();

				if (filter.test(resource) && view.extract(resource, Long.MAX_VALUE, nested) > 0) {
					// Will abort the extraction.
					return resource;
				}
			}
		}

		return null;
	}

	/**
	 * Attempt to find a resource stored in the passed storage that can be extracted, and how much of it can be extracted.
	 *
	 * @see #findExtractableContent(Storage, Predicate, TransactionContext)
	 * @return A non-blank resource stored in the storage that can be extracted, and the strictly positive amount of it that can be extracted,
	 * or {@code null} if none could be found.
	 */
	@Nullable
	public static <T> ResourceAmount<T> findExtractableContent(@Nullable Storage<T> storage, @Nullable TransactionContext transaction) {
		return findExtractableContent(storage, r -> true, transaction);
	}

	/**
	 * Attempt to find a resource stored in the passed storage that can be extracted and matches the filter, and how much of it can be extracted.
	 *
	 * @param storage The storage to inspect, may be null.
	 * @param filter The filter. Only a resource for which this filter returns {@code true} will be returned.
	 * @param transaction The current transaction, or {@code null} if a transaction should be opened for this query.
	 * @param <T> The type of the stored resources.
	 * @return A non-blank resource stored in the storage that can be extracted and matches the filter, and the strictly positive amount of it that can be extracted,
	 * or {@code null} if none could be found.
	 */
	@Nullable
	public static <T> ResourceAmount<T> findExtractableContent(@Nullable Storage<T> storage, Predicate<T> filter, @Nullable TransactionContext transaction) {
		T extractableResource = findExtractableResource(storage, filter, transaction);

		if (extractableResource != null) {
			long extractableAmount = simulateExtract(storage, extractableResource, Long.MAX_VALUE, transaction);

			if (extractableAmount > 0) {
				return new ResourceAmount<>(extractableResource, extractableAmount);
			}
		}

		return null;
	}

	/**
	 * Compute the comparator output for a storage, similar to {@link AbstractContainerMenu#getRedstoneSignalFromContainer(Container)}.
	 *
	 * @param storage The storage for which the comparator level should be computed.
	 * @param <T> The type of the stored resources.
	 * @return An integer between 0 and 15 (inclusive): the comparator output for the passed storage.
	 */
	public static <T> int calculateComparatorOutput(@Nullable Storage<T> storage) {
		if (storage == null) return 0;

		double fillPercentage = 0;
		int viewCount = 0;
		boolean hasNonEmptyView = false;

		for (StorageView<T> view : storage) {
			viewCount++;

			if (view.getAmount() > 0) {
				fillPercentage += (double) view.getAmount() / view.getCapacity();
				hasNonEmptyView = true;
			}
		}

		return Mth.floor(fillPercentage / viewCount * 14) + (hasNonEmptyView ? 1 : 0);
	}
}
