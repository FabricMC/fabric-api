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

package net.fabricmc.fabric.impl.base.event;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MutableCallSite;

/**
 * The invoker of an event that never changes.
 *
 * <p>{@link #object} is a generated class, whose method is a single {@code invokedynamic} onto a {@link MutableCallSite}.
 * When the listeners of the event change, only the target of the call site does. The JIT treats the target of
 * a call site as a constant, and inlines it into the code that fires the event, with a dependency that
 * recompiles that code if the target ever changes.
 *
 * <p>The event holds {@link #object} in a final field of a hidden class, see {@link StableEvents},
 * which the JIT also treats as a constant when the event is stored in a {@code static final} field.
 */
final class StableInvoker<T> {
	/**
	 * The generated invoker, implementing the listener interface.
	 */
	final T object;
	private final MutableCallSite site;
	/**
	 * The listener method, with the receiver as an additional first parameter.
	 */
	private final MethodHandle virtual;
	/**
	 * What the event does without listeners, as a constant method handle: no class is needed for it.
	 */
	private final MethodHandle empty;

	StableInvoker(T object, MutableCallSite site, MethodHandle virtual, MethodHandle empty) {
		this.object = object;
		this.site = site;
		this.virtual = virtual;
		this.empty = empty;
	}

	/**
	 * Makes the stable invoker do what the event does without listeners.
	 */
	void retargetEmpty() {
		site.setTarget(empty);
		MutableCallSite.syncAll(new MutableCallSite[] {site});
	}

	/**
	 * Makes the stable invoker call the given invoker from now on.
	 */
	void retarget(T invoker) {
		site.setTarget(virtual.bindTo(invoker));
		MutableCallSite.syncAll(new MutableCallSite[] {site});
	}
}
