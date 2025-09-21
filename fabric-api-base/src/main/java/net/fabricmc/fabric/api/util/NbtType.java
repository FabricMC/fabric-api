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

package net.fabricmc.fabric.api.util;

import net.minecraft.nbt.NbtElement;

/**
 * NBT type ID constants. Useful for filtering by value type in a few cases.
 *
 * <p>For the current list of types, check with {@link NbtElement}.
 *
 * @see net.minecraft.nbt.NbtTypes#byId(int)
 * @deprecated Use the constants in {@link NbtElement} instead.
 */
@Deprecated
public final class NbtType {
	public static final int END = 0;
	public static final int BYTE = 1;
	public static final int SHORT = 2;
	public static final int INT = 3;
	public static final int LONG = 4;
	public static final int FLOAT = 5;
	public static final int DOUBLE = 6;
	public static final int BYTE_ARRAY = 7;
	public static final int STRING = 8;
	public static final int LIST = 9;
	public static final int COMPOUND = 10;
	public static final int INT_ARRAY = 11;
	public static final int LONG_ARRAY = 12;

	private NbtType() { }
}
