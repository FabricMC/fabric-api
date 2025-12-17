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

package net.fabricmc.fabric.test.renderer.client;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import org.jspecify.annotations.Nullable;

import net.minecraft.client.resources.model.UnbakedGeometry;
import net.minecraft.resources.Identifier;

public class OverlayedUnbakedModelDeserializer extends SimpleUnbakedModelDeserializer {
	@Override
	protected @Nullable UnbakedGeometry getElements(JsonDeserializationContext context, JsonObject object) {
		Identifier parentLocation = getParentLocation(object);

		if (parentLocation == null) {
			return null;
		}

		return new OverlayedGeometry(parentLocation);
	}
}
