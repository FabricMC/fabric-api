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

package net.fabricmc.fabric.impl.client.keybinding;

import java.util.Comparator;

import net.minecraft.client.option.KeyBinding;
import net.minecraft.util.Identifier;

public class CategoryComparator implements Comparator<KeyBinding.Category> {
	public static final CategoryComparator INSTANCE = new CategoryComparator();

	@Override
	public int compare(KeyBinding.Category o1, KeyBinding.Category o2) {
		boolean o1Vanilla = o1.id().getNamespace().equals(Identifier.DEFAULT_NAMESPACE);
		boolean o2Vanilla = o2.id().getNamespace().equals(Identifier.DEFAULT_NAMESPACE);

		// If both are from vanilla, don't reorder them. Assumes sort is stable.
		if (o1Vanilla && o2Vanilla) {
			return 0;
		}

		// If exactly one is from vanilla, sort the one from vanilla first.
		if (o1Vanilla) {
			return -1;
		} else if (o2Vanilla) {
			return 1;
		}

		// If neither is from vanilla, sort alphabetically by namespace and then path.
		int c = o1.id().getNamespace().compareTo(o2.id().getNamespace());

		if (c != 0) {
			return c;
		}

		return o1.id().getPath().compareTo(o2.id().getPath());
	}
}
