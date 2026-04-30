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

package net.fabricmc.fabric.api.datagen.v1.advancement;

import java.util.function.Consumer;

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.resources.Identifier;

/**
 * Advancement builder extensions provided by Fabric.
 */
@ApiStatus.NonExtendable
public interface FabricAdvancementBuilder {
	/**
	 * Builds and saves the advancement.
	 * @param output The output to save the advancement to
	 * @param id The id of the advancement
	 * @return A new holder containing the saved advancement
	 */
	default AdvancementHolder save(Consumer<AdvancementHolder> output, Identifier id) {
		throw new UnsupportedOperationException("Implemented via mixin");
	}
}
