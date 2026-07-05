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

package net.fabricmc.fabric.test.gamerule;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Locale;

import org.junit.jupiter.api.Test;

import net.minecraft.world.level.gamerules.GameRule;

import net.fabricmc.fabric.api.gamerule.v1.GameRuleBuilder;

public class GameRuleTest {
	@Test
	public void testEnumSerializationRoundTripWithCustomToString() {
		GameRule<EnumWithCustomToString> rule = GameRuleBuilder.forEnum(EnumWithCustomToString.A).build();
		String serialized = rule.serialize(EnumWithCustomToString.B);
		EnumWithCustomToString deserialized = rule.deserialize(serialized).getOrThrow();
		assertEquals(EnumWithCustomToString.B, deserialized);
	}

	private enum EnumWithCustomToString {
		A, B;

		@Override
		public String toString() {
			return super.toString().toLowerCase(Locale.ROOT);
		}
	}
}
