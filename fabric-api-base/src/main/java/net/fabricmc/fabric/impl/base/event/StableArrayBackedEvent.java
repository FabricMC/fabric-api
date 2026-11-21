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

import java.util.function.Function;

/**
 * An event whose {@link #invoker()} is a {@link StableInvoker}, which never changes.
 *
 * <p>This class is a template: it is never used as is. {@link StableInvoker} loads its class file and defines it again
 * as a hidden class, because the JIT trusts the final fields of hidden classes. So when such an event is stored
 * in a {@code static final} field, {@code invoker()} is folded to the constant stable invoker.
 * Being a subclass of {@link ArrayBackedEvent}, the hidden copy is still an {@code ArrayBackedEvent}, and it has no static state.
 */
@LoadedAsHiddenClass
final class StableArrayBackedEvent<T> extends ArrayBackedEvent<T> {
	private final T invokerObject;

	StableArrayBackedEvent(Class<? super T> type, Function<T[], T> invokerFactory, StableInvoker<T> stable, T invokerObject) {
		super(type, invokerFactory, stable);
		this.invokerObject = invokerObject;
	}

	@Override
	public T invoker() {
		return invokerObject;
	}
}
