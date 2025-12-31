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

package net.fabricmc.fabric.api.transfer.v1.storage.base;

import java.util.Collections;
import java.util.Iterator;

import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

/// A [Storage] that supports insertion, and not extraction. By default, it doesn't have any storage view either.
public interface InsertionOnlyStorage<T> extends Storage<T> {
	@Override
	default boolean supportsExtraction() {
		return false;
	}

	@Override
	default long extract(T resource, long maxAmount, TransactionContext transaction) {
		return 0;
	}

	@Override
	default Iterator<StorageView<T>> iterator() {
		return Collections.emptyIterator();
	}
}
