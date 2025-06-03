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

import java.util.ArrayList;
import java.util.List;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.client.gui.render.SpecialGuiElementRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.VertexConsumerProvider;

import net.fabricmc.fabric.api.client.rendering.v1.SpecialGuiElementRendererRegistrationCallback;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
	@ModifyExpressionValue(method = "<init>", at = @At(value = "INVOKE", target = "Ljava/util/List;of(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)Ljava/util/List;"))
	private List<SpecialGuiElementRenderer<?>> initializeSpecialElements(List<SpecialGuiElementRenderer<?>> specialElements, @Local VertexConsumerProvider.Immediate immediate) {
		specialElements = new ArrayList<>(specialElements);
		SpecialGuiElementRendererRegistrationCallback.EVENT.invoker().accept(specialElements, immediate);
		return specialElements;
	}
}
