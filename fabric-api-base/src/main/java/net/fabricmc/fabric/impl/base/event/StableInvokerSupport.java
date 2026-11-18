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

import java.lang.invoke.CallSite;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

/**
 * Bootstrap of the {@code invokedynamic} instruction of the stable invokers, see {@link StableInvoker}.
 * Public because the generated classes live in the package of the listener interface.
 */
public final class StableInvokerSupport {
	private StableInvokerSupport() { }

	/**
	 * Hands out the call site that is the class data of the generated class.
	 */
	public static CallSite bootstrap(MethodHandles.Lookup lookup, String name, MethodType type) throws IllegalAccessException {
		return MethodHandles.classData(lookup, "_", CallSite.class);
	}
}
