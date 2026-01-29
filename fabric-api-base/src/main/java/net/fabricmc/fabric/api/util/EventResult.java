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

import com.mojang.serialization.Codec;

import net.minecraft.util.StringRepresentable;

/**
 * Represents a result of an event that controls execution of some action.
 */
public enum EventResult implements StringRepresentable {
	/**
	 * Prevents further event handling, while allowing related action.
	 */
	ALLOW("allow"),
	/**
	 * Continues execution of further events.
	 * In case of being returned by a final callback, it might either act the same as ALLOW
	 * or execute additional logic to determine the outcome.
	 */
	PASS("pass"),
	/**
	 * Prevents further event handling, while also preventing related action.
	 */
	DENY("deny");

	public static final Codec<EventResult> CODEC = StringRepresentable.fromEnum(EventResult::values);

	private final String name;

	/**
	 * Checks whatever action should be allowed.
	 * @return true if it's allowed, otherwise false.
	 */
	public boolean allowAction() {
		return this != DENY;
	}

	/**
	 * Checks whatever action should be allowed, with custom return value for {@link EventResult#PASS}.
	 * @return true if it's allowed, otherwise false.
	 */
	public boolean allowAction(boolean passResult) {
		return switch (this) {
		case PASS -> passResult;
		case ALLOW -> true;
		case DENY -> false;
		};
	}

	/**
	 * Value of this enum as string.
	 *
	 * @return lowercase name of the value.
	 */
	@Override
	public String getSerializedName() {
		return this.name;
	}

	EventResult(String name) {
		this.name = name;
	}
}
