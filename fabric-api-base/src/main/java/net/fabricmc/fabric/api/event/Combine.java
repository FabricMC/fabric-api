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

package net.fabricmc.fabric.api.event;

import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

/**
 * Describes how the listeners of an event are combined into one invoker, for listener interfaces with a single method.
 * Pass it to {@link EventFactory#createArrayBacked(Class, Combine)} or
 * {@link EventFactory#createWithPhases(Class, Combine, net.minecraft.resources.Identifier...)}.
 *
 * <p>Fabric API generates the invoker for you, and replaces it with a faster, unrolled version
 * (one call site per listener, which the JIT can inline) once the listeners are registered.
 * The behavior is always the one documented on the constants and factory methods below.
 *
 * <p>For anything that does not fit, for example listeners that must be wrapped in a {@code try}/{@code catch},
 * keep using {@link EventFactory#createArrayBacked(Class, java.util.function.Function)} with your own invoker factory.
 */
public final class Combine {
	/**
	 * Call every listener in registration (and phase) order. For listener methods returning {@code void}.
	 *
	 * <pre>{@code
	 * for (Listener listener : listeners) listener.call(args);
	 * }</pre>
	 */
	public static final Combine CALL_ALL = new Combine(Mode.VOID, null, -1);

	/**
	 * Call the listeners in order until one returns {@code false}, which is returned.
	 * Returns {@code true} if all listeners returned {@code true}, or if there are none.
	 * For listener methods returning {@code boolean}, typically "allow" events.
	 *
	 * <pre>{@code
	 * for (Listener listener : listeners) {
	 *     if (!listener.call(args)) return false;
	 * }
	 * return true;
	 * }</pre>
	 */
	public static final Combine STOP_AT_FALSE = new Combine(Mode.SHORT_CIRCUIT, Boolean.TRUE, -1);

	/**
	 * Call the listeners in order until one returns {@code true}, which is returned.
	 * Returns {@code false} if all listeners returned {@code false}, or if there are none.
	 * For listener methods returning {@code boolean}, typically "cancel" or "handled" events.
	 *
	 * <pre>{@code
	 * for (Listener listener : listeners) {
	 *     if (listener.call(args)) return true;
	 * }
	 * return false;
	 * }</pre>
	 */
	public static final Combine STOP_AT_TRUE = new Combine(Mode.SHORT_CIRCUIT, Boolean.FALSE, -1);

	/**
	 * Call <b>every</b> listener, and return whether all of them returned {@code true}
	 * ({@code true} if there are none). Unlike {@link #STOP_AT_FALSE}, listeners are still called after one returned {@code false}.
	 * For listener methods returning {@code boolean}.
	 *
	 * <pre>{@code
	 * boolean result = true;
	 * for (Listener listener : listeners) result &= listener.call(args);
	 * return result;
	 * }</pre>
	 */
	public static final Combine ALL_TRUE = new Combine(Mode.ALL_MATCH, null, -1);

	/**
	 * Call the listeners in order until one returns something other than {@code null}, which is returned.
	 * Returns {@code null} if all listeners returned {@code null}, or if there are none.
	 * For listener methods returning a reference type.
	 *
	 * <pre>{@code
	 * for (Listener listener : listeners) {
	 *     Result result = listener.call(args);
	 *     if (result != null) return result;
	 * }
	 * return null;
	 * }</pre>
	 */
	public static final Combine FIRST_NON_NULL = new Combine(Mode.SHORT_CIRCUIT, null, -1);

	private final Mode mode;
	private final @Nullable Object passValue;
	private final int threadedParam;

	private Combine(Mode mode, @Nullable Object passValue, int threadedParam) {
		this.mode = mode;
		this.passValue = passValue;
		this.threadedParam = threadedParam;
	}

	/**
	 * Call the listeners in order until one returns something other than {@code passValue}, which is returned.
	 * Returns {@code passValue} if all listeners returned it, or if there are none.
	 * Results are compared by identity ({@code !=}).
	 * For listener methods returning a reference type, typically with a constant like {@code InteractionResult.PASS}.
	 *
	 * <pre>{@code
	 * for (Listener listener : listeners) {
	 *     Result result = listener.call(args);
	 *     if (result != passValue) return result;
	 * }
	 * return passValue;
	 * }</pre>
	 *
	 * @param passValue The result that lets the next listener run.
	 * @return the combination
	 */
	public static Combine firstOtherThan(@Nullable Object passValue) {
		return passValue == null ? FIRST_NON_NULL : new Combine(Mode.SHORT_CIRCUIT, passValue, -1);
	}

	/**
	 * Pass the result of each listener as one of the parameters of the next, and return the result of the last.
	 * With no listeners, that parameter is returned unchanged.
	 * For listener methods returning the same type as the parameter at the given index.
	 *
	 * <pre>{@code
	 * for (Listener listener : listeners) value = listener.call(other, value);
	 * return value;
	 * }</pre>
	 *
	 * @param threadedParam The index of the parameter that receives the previous result, starting at 0.
	 * @return the combination
	 */
	public static Combine chain(int threadedParam) {
		if (threadedParam < 0) throw new IllegalArgumentException("Negative parameter index: " + threadedParam);
		return new Combine(Mode.CHAIN, null, threadedParam);
	}

	/**
	 * Implementation detail.
	 */
	@ApiStatus.Internal
	public enum Mode {
		VOID,
		SHORT_CIRCUIT,
		ALL_MATCH,
		CHAIN
	}

	@ApiStatus.Internal
	public Mode mode() {
		return mode;
	}

	@ApiStatus.Internal
	public @Nullable Object passValue() {
		return passValue;
	}

	@ApiStatus.Internal
	public int threadedParam() {
		return threadedParam;
	}
}
