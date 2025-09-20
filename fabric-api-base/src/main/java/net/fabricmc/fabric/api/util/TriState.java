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

import java.util.Objects;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import com.mojang.serialization.Codec;
import org.jetbrains.annotations.Nullable;

import net.minecraft.util.StringIdentifiable;

/**
 * Represents a boolean value which can be true, false or refer to a default value.
 */
public enum TriState implements StringIdentifiable {
	/**
	 * Represents the boolean value of {@code false}.
	 */
	FALSE("false"),
	/**
	 * Represents a value that refers to a "default" value, often as a fallback.
	 */
	DEFAULT("default"),
	/**
	 * Represents the boolean value of {@code true}.
	 */
	TRUE("true");

	public static final Codec<TriState> CODEC = StringIdentifiable.createCodec(TriState::values);

	private final String name;

	/**
	 * Gets the corresponding tri-state from a boolean value.
	 *
	 * @param bool the boolean value
	 * @return {@link TriState#TRUE} or {@link TriState#FALSE} depending on the value of the boolean.
	 */
	public static TriState of(boolean bool) {
		return bool ? TRUE : FALSE;
	}

	/**
	 * Gets a tri-state from a nullable boxed boolean.
	 *
	 * @param bool the boolean value
	 * @return {@link TriState#DEFAULT} if {@code null}.
	 * Otherwise {@link TriState#TRUE} or {@link TriState#FALSE} depending on the value of the boolean.
	 */
	public static TriState of(@Nullable Boolean bool) {
		return bool == null ? DEFAULT : of(bool.booleanValue());
	}

	/**
	 * Gets the value of the tri-state.
	 *
	 * @return true if the tri-state is {@link TriState#TRUE},
	 * otherwise false.
	 */
	public boolean get() {
		return this == TRUE;
	}

	/**
	 * Gets the value of the tri-state as a boxed, nullable boolean.
	 *
	 * @return {@code null} if {@link TriState#DEFAULT}.
	 * Otherwise {@code true} if {@link TriState#TRUE} or {@code false} if {@link TriState#FALSE}.
	 */
	@Nullable
	public Boolean getBoxed() {
		return this == DEFAULT ? null : this.get();
	}

	/**
	 * Gets the value of this tri-state.
	 * If the value is {@link TriState#DEFAULT} then use the supplied value.
	 *
	 * @param value the value to fall back to
	 * @return the value of the tri-state or the supplied value if {@link TriState#DEFAULT}.
	 */
	public boolean orElse(boolean value) {
		return this == DEFAULT ? value : this.get();
	}

	/**
	 * Gets the value of this tri-state.
	 * If the value is {@link TriState#DEFAULT} then use the supplied value.
	 *
	 * @param supplier the supplier used to get the value to fall back to
	 * @return the value of the tri-state or the value of the supplier if the tri-state is {@link TriState#DEFAULT}.
	 */
	public boolean orElseGet(BooleanSupplier supplier) {
		return this == DEFAULT ? supplier.getAsBoolean() : this.get();
	}

	/**
	 * Maps the boolean value of this tri-state if it is {@link TriState#TRUE} or {@link TriState#FALSE}.
	 *
	 * @param mapper the mapper to use
	 * @param <T> the type of object being supplier by the mapper
	 * @return an optional containing the mapped value; {@link Optional#empty()} if the tri-state is {@link TriState#DEFAULT} or the value provided by the mapper is {@code null}.
	 */
	public <T> Optional<T> map(BooleanFunction<@Nullable ? extends T> mapper) {
		Objects.requireNonNull(mapper, "Mapper function cannot be null");

		if (this == DEFAULT) {
			return Optional.empty();
		}

		return Optional.ofNullable(mapper.apply(this.get()));
	}

	/**
	 * Gets the value of this tri-state, or throws an exception if this tri-state's value is {@link TriState#DEFAULT}.
	 *
	 * @param exceptionSupplier the supplying function that produces an exception to be thrown
	 * @param <X> Type of the exception to be thrown
	 * @return the value
	 * @throws X if the value is {@link TriState#DEFAULT}
	 */
	public <X extends Throwable> boolean orElseThrow(Supplier<X> exceptionSupplier) throws X {
		if (this != DEFAULT) {
			return this.get();
		}

		throw exceptionSupplier.get();
	}

	/**
	 * {@return a parsed TriState from a system property}
	 *
	 * @param property the system property
	 */
	public static TriState fromSystemProperty(String property) {
		String value = System.getProperty(property);

		if (value != null) {
			return Boolean.parseBoolean(value) ? TRUE : FALSE;
		}

		return DEFAULT;
	}

	/**
	 * Value of this enum as string.
	 *
	 * @return lowercase name of the value.
	 */
	@Override
	public String asString() {
		return this.name;
	}

	TriState(String name) {
		this.name = name;
	}
}
