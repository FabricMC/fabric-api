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

package net.fabricmc.fabric.test.object.builder;

import java.util.Objects;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateType;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

public class PersistentStateManagerTest implements ModInitializer {
	private boolean ranTests = false;

	@Override
	public void onInitialize() {
		ServerTickEvents.END_WORLD_TICK.register(world -> {
			if (ranTests) return;
			ranTests = true;

			TestState.getOrCreate(world).setValue("Hello!");

			if (!Objects.equals(TestState.getOrCreate(world).getValue(), "Hello!")) {
				throw new IllegalStateException();
			}
		});
	}

	private static class TestState extends PersistentState {
		/**
		 * We are testing that null can be passed as the dataFixType.
		 */
		private static final Codec<TestState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.STRING.fieldOf("value").forGetter(TestState::getValue)
		).apply(instance, TestState::new));
		private static final PersistentStateType<TestState> TYPE = new PersistentStateType<>(ObjectBuilderTestConstants.id("test_state").toString().replace(":", "_"), TestState::new, CODEC, null);

		public static TestState getOrCreate(ServerWorld world) {
			return world.getPersistentStateManager().getOrCreate(TestState.TYPE);
		}

		private String value = "";

		private TestState() {
		}

		private TestState(String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
			markDirty();
		}
	}
}
