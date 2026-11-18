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

package net.fabricmc.fabric.test.base;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.impl.base.event.EventFactoryImpl;

/**
 * Checks that events of other modules that were converted to generated invokers actually get one.
 */
public class ConvertedEventsTest {
	@Test
	void serverTickEventIsUnrolled() {
		List<Integer> calls = new ArrayList<>();
		ServerTickEvents.END_SERVER_TICK.register(server -> calls.add(1));
		ServerTickEvents.END_SERVER_TICK.register(server -> calls.add(2));
		ServerTickEvents.END_SERVER_TICK.register(server -> calls.add(3));
		EventFactoryImpl.awaitInvokerGeneration();

		assertTrue(EventFactoryImpl.underlyingInvoker(ServerTickEvents.END_SERVER_TICK).getClass().getName().contains("FabricEvent$Unrolled3"));
		ServerTickEvents.END_SERVER_TICK.invoker().onEndTick(null);
		assertEquals(List.of(1, 2, 3), calls);
	}
}
