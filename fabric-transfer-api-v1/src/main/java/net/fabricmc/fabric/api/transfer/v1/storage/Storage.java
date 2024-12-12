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

import java.util.Iterator;

import com.google.common.collect.Iterators;

import net.fabricmc.fabric.api.transfer.v1.storage.base.CombinedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.ExtractionOnlyStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.InsertionOnlyStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.impl.transfer.TransferApiImpl;

/**
 * An object that can store resources.
 *
 * <p>Most of the documentation that follows is quite technical.
 * For an easier introduction to the API, see the <a href="https://fabricmc.net/wiki/tutorial:transfer-api">wiki page</a>.
 *
 * <ul>
 *     <li>{@link #supportsInsertion} and {@link #supportsExtraction} can be used to tell if insertion and extraction
 *     functionality are possibly supported by this storage.</li>
 *     <li>{@link #insert} and {@link #extract} can be used to insert or extract resources from this storage.</li>
 *     <li>{@link #iterator} can be used to inspect the contents of this storage.</li>
 *     <li>{@link #getVersion()} can be used to quickly check if a storage has changed, without having to rescan its contents.</li>
 * </ul>
 *
 * <p>Users that wish to implement this interface can use the helpers in the {@code base} package:
 * <ul>
 *     <li>{@link CombinedStorage} can be used to combine multiple instances, for example to combine multiple "slots" in one big storage.</li>
 *     <li>{@link ExtractionOnlyStorage} and {@link InsertionOnlyStorage} can be used when only extraction or insertion is needed.</li>
 *     <li>Resource-specific base implementations may also be available.
 *     For example, Fabric API provides {@link SingleVariantStorage} to accelerate implementations of transfer variant storages.</li>
 * </ul>
 *
 * <p><b>Important note:</b> Unless otherwise specified, all transfer functions take a non-blank resource
 * and a non-negative maximum amount as parameters.
 * Implementations are encouraged to throw an exception if these preconditions are violated.
 * {@link StoragePreconditions} can be used for these checks.
 *
 * <p>For transfer functions, the returned amount must be non-negative, and smaller than the passed maximum amount.
 * Consumers of these functions are encourage to throw an exception if these postconditions are violated.
 *
 * @param <T> The type of the stored resources.
 * @see Transaction
 */
public interface Storage<T> extends Iterable<StorageView<T>> {
	/**
	 * Return an empty storage.
	 */
	@SuppressWarnings("unchecked")
	static <T> Storage<T> empty() {
		return (Storage<T>) TransferApiImpl.EMPTY_STORAGE;
	}

	/**
	 * Return false if calling {@link #insert} will absolutely always return 0, or true otherwise or in doubt.
	 *
	 * <p>Note: This function is meant to be used by pipes or other devices that can transfer resources to know if
	 * they should interact with this storage at all.
	 */
	default boolean supportsInsertion() {
		return true;
	}

	/**
	 * Try to insert up to some amount of a resource into this storage.
	 *
	 * @param resource The resource to insert. May not be blank.
	 * @param maxAmount The maximum amount of resource to insert. May not be negative.
	 * @param transaction The transaction this operation is part of.
	 * @return A non-negative integer not greater than maxAmount: the amount that was inserted.
	 */
	long insert(T resource, long maxAmount, TransactionContext transaction);

	/**
	 * Return false if calling {@link #extract} will absolutely always return 0, or true otherwise or in doubt.
	 *
	 * <p>Note: This function is meant to be used by pipes or other devices that can transfer resources to know if
	 * they should interact with this storage at all.
	 */
	default boolean supportsExtraction() {
		return true;
	}

	/**
	 * Try to extract up to some amount of a resource from this storage.
	 *
	 * @param resource The resource to extract. May not be blank.
	 * @param maxAmount The maximum amount of resource to extract. May not be negative.
	 * @param transaction The transaction this operation is part of.
	 * @return A non-negative integer not greater than maxAmount: the amount that was extracted.
	 */
	long extract(T resource, long maxAmount, TransactionContext transaction);

	/**
	 * Iterate through the contents of this storage.
	 * Every visited {@link StorageView} represents a stored resource and an amount.
	 * The iterator doesn't guarantee that a single resource only occurs once during an iteration.
	 * Calling {@linkplain Iterator#remove remove} on the iterator is not allowed.
	 *
	 * <p>{@link #insert} and {@link #extract} may be called safely during iteration.
	 * Extractions should be visible to an open iterator, but insertions are not required to.
	 * In particular, inventories with a fixed amount of slots may wish to make insertions visible to iterators,
	 * but inventories with a dynamic or very large amount of slots should not do that to ensure timely termination of
	 * the iteration.
	 *
	 * <p>If a modification is made to the storage during iteration, the iterator might become invalid at the end of the outermost transaction.
	 * In particular, if multiple storage views are extracted from, the entire iteration should be wrapped in a transaction.
	 *
	 * @return An iterator over the contents of this storage. Calling remove on the iterator is not allowed.
	 */
	@Override
	Iterator<StorageView<T>> iterator();

	/**
	 * Same as {@link #iterator()}, but the iterator is guaranteed to skip over empty views,
	 * i.e. views that {@linkplain StorageView#isResourceBlank() contain blank resources} or have a zero {@linkplain StorageView#getAmount() amount}.
	 *
	 * <p>This can provide a large performance benefit over {@link #iterator()} if the caller is only interested in non-empty views,
	 * for example because it is trying to extract resources from the storage.
	 *
	 * <p>This function should only be overridden if the storage is able to provide an optimized iterator over non-empty views,
	 * for example because it is keeping an index of non-empty views.
	 * Otherwise, the default implementation simply calls {@link #iterator()} and filters out empty views.
	 *
	 * <p>When implementing this function, note that the guarantees of {@link #iterator()} still apply.
	 * In particular, {@link #insert} and {@link #extract} may be called safely during iteration.
	 *
	 * @return An iterator over the non-empty views of this storage. Calling remove on the iterator is not allowed.
	 */
	default Iterator<StorageView<T>> nonEmptyIterator() {
		return Iterators.filter(iterator(), view -> view.getAmount() > 0 && !view.isResourceBlank());
	}

	/**
	 * Convenient helper to get an {@link Iterable} over the {@linkplain #nonEmptyIterator() non-empty views} of this storage, for use in for-each loops.
	 *
	 * <pre>{@code
	 * for (StorageView<T> view : storage.nonEmptyViews()) {
	 *     // Do something with the view
	 * }
	 * }</pre>
	 */
	default Iterable<StorageView<T>> nonEmptyViews() {
		return this::nonEmptyIterator;
	}

	/**
	 * Return an integer representing the current version of this storage instance to allow for fast change detection:
	 * if the version hasn't changed since the last time, <b>and the storage instance is the same</b>, the storage has the same contents.
	 * This can be used to avoid re-scanning the contents of the storage, which could be an expensive operation.
	 * It may be used like that:
	 * <pre>{@code
	 * // Store storage and version:
	 * Storage<?> firstStorage = // ...
	 * long firstVersion = firstStorage.getVersion();
	 *
	 * // Later, check if the secondStorage is the unchanged firstStorage:
	 * Storage<?> secondStorage = // ...
	 * long secondVersion = secondStorage.getVersion();
	 * // We must check firstStorage == secondStorage first, otherwise versions may not be compared.
	 * if (firstStorage == secondStorage && firstVersion == secondVersion) {
	 *     // storage contents are the same.
	 * } else {
	 *     // storage contents might have changed.
	 * }
	 * }</pre>
	 *
	 * <p>The version <b>must</b> change if the state of the storage has changed,
	 * generally after a direct modification, or at the end of a modifying transaction.
	 * The version may also change even if the state of the storage hasn't changed.
	 *
	 * <p>It is not valid to call this during a transaction,
	 * and implementations are encouraged to throw an exception if that happens.
	 */
	default long getVersion() {
		if (Transaction.isOpen()) {
			throw new IllegalStateException("getVersion() may not be called during a transaction.");
		}

		return TransferApiImpl.version.getAndIncrement();
	}

	/**
	 * Return a class instance of this interface with the desired generic type,
	 * to be used for easier registration with API lookups.
	 */
	@SuppressWarnings("unchecked")
	static <T> Class<Storage<T>> asClass() {
		return (Class<Storage<T>>) (Object) Storage.class;
	}
}
