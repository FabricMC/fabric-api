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

package net.fabricmc.fabric.test.recipe.book;

import java.util.function.IntFunction;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;

import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;

public enum BookCraftingBookCategory implements StringRepresentable {
	BOOK("book", 0),
	ENCHANTED_BOOK("enchanted_book", 1),
	KNOWLEDGE_BOOK("knowledge_book", 2);

	public static final Codec<BookCraftingBookCategory> CODEC = StringRepresentable.fromEnum(BookCraftingBookCategory::values);
	public static final IntFunction<BookCraftingBookCategory> BY_ID = ByIdMap.continuous(BookCraftingBookCategory::id, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
	public static final StreamCodec<ByteBuf, BookCraftingBookCategory> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, BookCraftingBookCategory::id);
	private final String name;
	private final int id;

	BookCraftingBookCategory(final String name, final int id) {
		this.name = name;
		this.id = id;
	}

	@Override
	public String getSerializedName() {
		return this.name;
	}

	private int id() {
		return this.id;
	}
}
