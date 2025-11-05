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

package net.fabricmc.fabric.mixin.client.particle;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleGroup;
import net.minecraft.client.particle.ParticleRenderType;

import net.fabricmc.fabric.impl.client.particle.ParticleRendererRegistryImpl;

@Mixin(ParticleEngine.class)
public abstract class ParticleEngineMixin {
	@Shadow
	@Final
	@Mutable
	private static List<ParticleRenderType> RENDER_ORDER;

	@Inject(method = "<clinit>", at = @At("RETURN"))
	private static void classInit(CallbackInfo ci) {
		RENDER_ORDER = new ArrayList<>(RENDER_ORDER); // Make it mutable
	}

	@Inject(method = "createParticleGroup", at = @At(value = "NEW", target = "(Lnet/minecraft/client/particle/ParticleEngine;Lnet/minecraft/client/particle/ParticleRenderType;)Lnet/minecraft/client/particle/QuadParticleGroup;"), cancellable = true)
	private void createParticleRenderer(ParticleRenderType textureSheet, CallbackInfoReturnable<ParticleGroup<?>> cir) {
		Function<ParticleEngine, ParticleGroup<?>> factory = ParticleRendererRegistryImpl.INSTANCE.getFactory(textureSheet);

		if (factory != null) {
			cir.setReturnValue(factory.apply((ParticleEngine) (Object) this));
		}
	}
}
