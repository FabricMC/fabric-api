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

package net.fabricmc.fabric.impl.entity.event.effect;

import java.util.Stack;

import net.fabricmc.fabric.api.entity.event.v1.effect.EffectEventContext;

public final class MobEffectUtil {
	// we must use a stack because nested commands like "/execute run" exist,
	// and we must mixin to multiple places
	private static final ThreadLocal<Stack<EffectEventContext>> CURRENT_COMMAND_CONTEXT = ThreadLocal.withInitial(() -> {
		var stack = new Stack<EffectEventContext>();
		stack.push(EffectEventContextImpl.DEFAULT);
		return stack;
	});

	private MobEffectUtil() {
	}

	public static EffectEventContext getCommandContext() {
		return CURRENT_COMMAND_CONTEXT.get().getLast();
	}

	/**
	 * @see Stack#push(Object)
	 */
	public static void pushContext(EffectEventContext context) {
		CURRENT_COMMAND_CONTEXT.get().push(context);
	}

	/**
	 * @see Stack#pop()
	 */
	public static void popContext() {
		CURRENT_COMMAND_CONTEXT.get().pop();
	}
}
