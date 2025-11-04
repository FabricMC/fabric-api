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

package net.fabricmc.fabric.api.datagen.v1;

import java.nio.file.Path;

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.data.PackOutput;

import net.fabricmc.loader.api.ModContainer;

/**
 * Extends {@link PackOutput} to keep track of the {@link ModContainer} that it originated from.
 */
public final class FabricDataOutput extends PackOutput {
	private final ModContainer modContainer;
	private final boolean strictValidation;

	@ApiStatus.Internal
	public FabricDataOutput(ModContainer modContainer, Path path, boolean strictValidation) {
		super(path);
		this.modContainer = modContainer;
		this.strictValidation = strictValidation;
	}

	/**
	 * Returns the {@link ModContainer} for the mod that this data generator has been created for.
	 *
	 * @return a {@link ModContainer} instance
	 */
	public ModContainer getModContainer() {
		return modContainer;
	}

	/**
	 * Returns the mod ID for the mod that this data generator has been created for.
	 *
	 * @return a mod ID
	 */
	public String getModId() {
		return getModContainer().getMetadata().getId();
	}

	/**
	 * When enabled data providers can do strict validation to ensure that all entries have data generated for them.
	 *
	 * @return if strict validation should be enabled
	 */
	public boolean isStrictValidationEnabled() {
		return strictValidation;
	}
}
