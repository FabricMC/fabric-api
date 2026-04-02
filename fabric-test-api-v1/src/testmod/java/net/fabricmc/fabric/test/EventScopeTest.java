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

package net.fabricmc.fabric.test;

import net.minecraft.gametest.framework.GameTestHelper;

import net.fabricmc.fabric.api.test.v1.EventScope;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.fabricmc.fabric.api.gametest.v1.GameTest;

public class EventScopeTest {
	private static final Event<Foo> EVENT = EventFactory.createArrayBacked(
			Foo.class,
			listeners -> () -> {
				for (Foo listener : listeners) {
					if (!listener.doSomething()) {
						return false;
					}
				}

				return true;
			}
	);

	@GameTest
	public void testEventScope(GameTestHelper helper) {
		Foo foo = () -> false;

		try (EventScope _ = EventScope.register(EVENT, foo)) {
			helper.assertFalse(EVENT.invoker().doSomething(), "Event Foo in EventScope was not registered.");
		}

		helper.assertTrue(EVENT.invoker().doSomething(), "EventScope did not unregister event Foo after closing.");
		helper.succeed();
	}

	private interface Foo {
		boolean doSomething();
	}
}
