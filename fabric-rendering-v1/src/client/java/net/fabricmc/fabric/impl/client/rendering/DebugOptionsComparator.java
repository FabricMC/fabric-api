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

package net.fabricmc.fabric.impl.client.rendering;

import java.util.Comparator;

import net.minecraft.util.Identifier;

public class DebugOptionsComparator implements Comparator<Identifier> {
	public static final DebugOptionsComparator INSTANCE = new DebugOptionsComparator();

	@Override
	public int compare(Identifier o1, Identifier o2) {
		// Sort 'minecraft' namespace first, then alphabetically by namespace, then path.
		boolean o1IsMinecraft = Identifier.DEFAULT_NAMESPACE.equals(o1.getNamespace());
		boolean o2IsMinecraft = Identifier.DEFAULT_NAMESPACE.equals(o2.getNamespace());

		if (o1IsMinecraft && !o2IsMinecraft) {
			return -1;
		}

		if (!o1IsMinecraft && o2IsMinecraft) {
			return 1;
		}

		int c = o1.getNamespace().compareTo(o2.getNamespace());

		if (c != 0) {
			return c;
		}

		return o1.getPath().compareTo(o2.getPath());
	}
}
