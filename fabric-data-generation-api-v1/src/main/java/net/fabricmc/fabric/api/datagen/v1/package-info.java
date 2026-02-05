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

/**
 * The Data Generation API, version 1.
 *
 * <p>This API extends the data generation system used by Minecraft to allow for mods to generate data (json files) automatically.
 *
 * <p>Use the {@link net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint} to register {@link net.minecraft.data.DataProvider} with the {@link net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator}.
 */
@NullMarked
package net.fabricmc.fabric.api.datagen.v1;

import org.jspecify.annotations.NullMarked;
