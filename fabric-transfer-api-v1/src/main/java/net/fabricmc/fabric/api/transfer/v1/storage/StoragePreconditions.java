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

/// Preconditions that can be used when working with storages.
///
/// In particular, [#notNegative] or [#notBlankNotNegative] can be used by implementations of
/// [Storage#insert] and [Storage#extract] to fail-fast if the arguments are invalid.
public final class StoragePreconditions {
	/// Ensure that the passed transfer variant is not blank.
	///
	/// @throws IllegalArgumentException If the variant is blank.
	public static void notBlank(TransferVariant<?> variant) {
		if (variant.isBlank()) {
			throw new IllegalArgumentException("Transfer variant may not be blank.");
		}
	}

	/// Ensure that the passed amount is not negative. That is, it must be `>= 0`.
	///
	/// @throws IllegalArgumentException If the amount is negative.
	public static void notNegative(long amount) {
		if (amount < 0) {
			throw new IllegalArgumentException("Amount may not be negative, but it is: " + amount);
		}
	}

	/// Check both for a not blank transfer variant and a not negative amount.
	public static void notBlankNotNegative(TransferVariant<?> variant, long amount) {
		notBlank(variant);
		notNegative(amount);
	}

	private StoragePreconditions() {
	}
}
