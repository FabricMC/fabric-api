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

package net.fabricmc.fabric.test.attachment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.util.dynamic.Codecs;

public record WheelInfo(float wheelDiameter, float tireDiameter, float tireThickness) {
	public static final Codec<WheelInfo> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codecs.POSITIVE_FLOAT.fieldOf("wheelDiameter").forGetter(WheelInfo::wheelDiameter),
			Codecs.POSITIVE_FLOAT.fieldOf("tireDiameter").forGetter(WheelInfo::tireDiameter),
			Codecs.POSITIVE_FLOAT.fieldOf("tireThickness").forGetter(WheelInfo::tireThickness)
	).apply(instance, WheelInfo::new));
}
