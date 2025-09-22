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

package net.fabricmc.fabric.api.gamerule.v1.rule;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.mojang.brigadier.context.CommandContext;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.world.GameRules;

import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;

public final class EnumRule<E extends Enum<E>> extends GameRules.Rule<EnumRule<E>> {
	private static final Logger LOGGER = LoggerFactory.getLogger(GameRuleRegistry.class);

	private final Class<E> classType;
	private final List<E> supportedValues;
	private E value;

	/**
	 * @deprecated You should not be calling this constructor!
	 */
	@Deprecated
	public EnumRule(GameRules.Type<EnumRule<E>> type, E value, E[] supportedValues) {
		this(type, value, Arrays.asList(supportedValues));
	}

	/**
	 * You should not be calling this constructor!
	 */
	@Deprecated
	public EnumRule(GameRules.Type<EnumRule<E>> type, E value, Collection<E> supportedValues) {
		super(type);
		this.classType = value.getDeclaringClass();
		this.value = value;
		this.supportedValues = new ArrayList<>(supportedValues);

		if (!this.supports(value)) {
			throw new IllegalArgumentException("Cannot set default value");
		}
	}

	@Override
	protected void setFromArgument(CommandContext<ServerCommandSource> context, String name) {
		// Do nothing. We use a different system for application with literals
	}

	@Override
	protected void deserialize(String value) {
		try {
			final E deserialized = Enum.valueOf(this.classType, value);

			if (!this.supports(deserialized)) {
				LOGGER.warn("Failed to parse rule of value {} for rule of type {}. Since the value {}, is unsupported.", value, this.classType, value);
			}

			this.set(deserialized, null);
		} catch (IllegalArgumentException e) {
			LOGGER.warn("Failed to parse rule of value {} for rule of type {}", value, this.classType);
		}
	}

	@Override
	public String serialize() {
		return this.value.name();
	}

	@Override
	public int getCommandResult() {
		// For now we are gonna use the ordinal as the command result. Could be changed or set to relate to something else entirely.
		return this.value.ordinal();
	}

	@Override
	protected EnumRule<E> getThis() {
		return this;
	}

	public Class<E> getEnumClass() {
		return this.classType;
	}

	@Override
	public String toString() {
		return this.value.toString();
	}

	@Override
	protected EnumRule<E> copy() {
		return new EnumRule<>(this.type, this.value, this.supportedValues);
	}

	@Override
	public void setValue(EnumRule<E> rule, MinecraftServer minecraftServer) {
		if (!this.supports(rule.value)) {
			throw new IllegalArgumentException(String.format("Rule does not support value: %s", rule.value));
		}

		this.value = rule.value;
		this.changed(minecraftServer);
	}

	public E get() {
		return this.value;
	}

	/**
	 * Cycles the value of this enum rule to the next supported value.
	 */
	public void cycle() {
		int index = this.supportedValues.indexOf(this.value);

		if (index < 0) {
			throw new IllegalArgumentException(String.format("Invalid value: %s", this.value));
		}

		this.set(this.supportedValues.get((index + 1) % this.supportedValues.size()), null);
	}

	public boolean supports(E value) {
		return this.supportedValues.contains(value);
	}

	public void set(E value, @Nullable MinecraftServer server) throws IllegalArgumentException {
		checkNotNull(value);

		if (!this.supports(value)) {
			throw new IllegalArgumentException("Tried to set an unsupported value: " + value.toString());
		}

		this.value = value;
		this.changed(server);
	}

	public void set(String value, @Nullable MinecraftServer server) throws IllegalArgumentException {
		for (E constant : getEnumClass().getEnumConstants()) {
			if (constant.name().equalsIgnoreCase(value)) {
				this.set(constant, server);
				return;
			}
		}

		throw new IllegalArgumentException("Tried to set an unsupported value: " + value);
	}
}
