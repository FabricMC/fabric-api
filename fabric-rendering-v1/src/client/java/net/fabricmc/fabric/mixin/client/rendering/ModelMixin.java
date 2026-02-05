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

package net.fabricmc.fabric.mixin.client.rendering;

import java.util.Map;
import java.util.function.Function;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;

import net.fabricmc.fabric.api.client.rendering.v1.FabricModel;

@Mixin(Model.class)
abstract class ModelMixin<S> implements FabricModel<S> {
	@Shadow
	public abstract ModelPart root();

	@Unique
	private final Map<String, ModelPart> childPartMap = new Object2ObjectOpenHashMap<>();

	@Inject(method = "<init>", at = @At("TAIL"))
	private void fillChildPartMap(ModelPart root, Function<Identifier, RenderType> layerFactory, CallbackInfo ci) {
		((ModelPartAccessor) (Object) root).fabric$callForEachChild(childPartMap::putIfAbsent);
	}

	@Override
	@Nullable
	public ModelPart getChildPart(String name) {
		return childPartMap.get(name);
	}

	@Override
	public void copyTransforms(Model<?> model) {
		copyTransforms(model.root(), root());
		((ModelPartAccessor) (Object) model.root()).fabric$callForEachChild((name, part) -> {
			ModelPart childPart = getChildPart(name);

			if (childPart != null) {
				copyTransforms(part, childPart);
			}
		});
	}

	@Unique
	private static void copyTransforms(ModelPart from, ModelPart to) {
		to.x = from.x;
		to.y = from.y;
		to.z = from.z;
		to.xRot = from.xRot;
		to.yRot = from.yRot;
		to.zRot = from.zRot;
		to.xScale = from.xScale;
		to.yScale = from.yScale;
		to.zScale = from.zScale;
	}
}
