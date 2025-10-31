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

package net.fabricmc.fabric.impl.gamerule;

import org.jspecify.annotations.Nullable;

import net.fabricmc.fabric.impl.gamerule.rpc.FabricGameRuleType;

public interface RuleTypeExtensions {
	@Nullable
	FabricGameRuleType fabric_getType();

	void fabric_setType(FabricGameRuleType type);

	default <E extends Enum<E>> E fabric_enumCycle(E currentValue) {
		throw new UnsupportedOperationException("Non-enum rules cannot be cycled!");
	}

	default <E extends Enum<E>> Iterable<E> fabric_getSupportedEnumValues() {
		throw new UnsupportedOperationException("Non-enum rules cannot have supported enum values!");
	}

	default <E extends Enum<E>> void fabric_setSupportedEnumValues(E[] supportedValues) {
		throw new UnsupportedOperationException("Non-enum rules cannot have supported enum values!");
	}
}
