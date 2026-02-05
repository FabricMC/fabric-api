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

package net.fabricmc.fabric.impl.biome;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import com.google.common.base.Preconditions;

import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.synth.ImprovedNoise;

/**
 * Picks entries with arbitrary double weights using a binary search.
 */
public final class WeightedPicker<T> {
	private double currentTotal;
	private final List<WeightedEntry<T>> entries;

	WeightedPicker() {
		this(0, new ArrayList<>());
	}

	private WeightedPicker(double currentTotal, List<WeightedEntry<T>> entries) {
		this.currentTotal = currentTotal;
		this.entries = entries;
	}

	void add(T biome, final double weight) {
		currentTotal += weight;

		entries.add(new WeightedEntry<>(biome, weight, currentTotal));
	}

	double getCurrentWeightTotal() {
		return currentTotal;
	}

	int getEntryCount() {
		return entries.size();
	}

	public T pickFromNoise(ImprovedNoise sampler, double x, double y, double z) {
		double target = Mth.clamp(Math.abs(sampler.noise(x, y, z)), 0, 1) * getCurrentWeightTotal();

		return search(target).entry();
	}

	/**
	 * Applies a mapping function to each entry and returns a picker with otherwise equivalent settings.
	 */
	<U> WeightedPicker<U> map(Function<T, U> mapper) {
		return new WeightedPicker<U>(
				currentTotal,
				entries.stream()
						.map(e -> new WeightedEntry<>(mapper.apply(e.entry), e.weight, e.upperWeightBound))
						.toList()
		);
	}

	/**
	 * Searches with the specified target value.
	 *
	 * @param target The target value, must satisfy the constraint 0 <= target <= currentTotal
	 * @return The result of the search
	 */
	WeightedEntry<T> search(final double target) {
		// Sanity checks, fail fast if stuff is going wrong.
		Preconditions.checkArgument(target <= currentTotal, "The provided target value for entry selection must be less than or equal to the weight total");
		Preconditions.checkArgument(target >= 0, "The provided target value for entry selection cannot be negative");

		int low = 0;
		int high = entries.size() - 1;

		while (low < high) {
			int mid = (high + low) >>> 1;

			if (target < entries.get(mid).upperWeightBound()) {
				high = mid;
			} else {
				low = mid + 1;
			}
		}

		return entries.get(low);
	}

	/**
	 * Represents a modded entry in a list, and its corresponding weight.
	 *
	 * @param entry            the entry
	 * @param weight           how often an entry will be chosen
	 * @param upperWeightBound the upper weight bound within the context of the other entries, used for the binary search
	 */
	record WeightedEntry<T>(T entry, double weight, double upperWeightBound) {
	}
}
