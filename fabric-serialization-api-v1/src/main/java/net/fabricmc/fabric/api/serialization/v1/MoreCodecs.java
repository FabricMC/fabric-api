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

package net.fabricmc.fabric.api.serialization.v1;

import com.mojang.serialization.Codec;
import org.jetbrains.annotations.ApiStatus;

import net.fabricmc.fabric.impl.serialization.SpecialCodecs;

/**
 * Additional codecs that can be used by modders.
 */
@ApiStatus.NonExtendable
public interface MoreCodecs {
	Codec<long[]> LONG_ARRAY = SpecialCodecs.LONG_ARRAY;
	Codec<byte[]> BYTE_ARRAY = SpecialCodecs.BYTE_ARRAY;
}
