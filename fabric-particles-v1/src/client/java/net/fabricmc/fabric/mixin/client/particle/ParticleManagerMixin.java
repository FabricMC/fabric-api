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

import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.particle.ParticleRenderer;
import net.minecraft.client.particle.ParticleTextureSheet;

import net.fabricmc.fabric.impl.client.particle.ParticleRendererRegistryImpl;

@Mixin(ParticleManager.class)
public abstract class ParticleManagerMixin {
	@Shadow
	@Final
	@Mutable
	private static List<ParticleTextureSheet> PARTICLE_TEXTURE_SHEETS;

	@Inject(method = "<clinit>", at = @At("RETURN"))
	private static void classInit(CallbackInfo ci) {
		PARTICLE_TEXTURE_SHEETS = new ArrayList<>(PARTICLE_TEXTURE_SHEETS); // Make it mutable
	}

	@Inject(method = "createParticleRenderer", at = @At(value = "NEW", target = "(Lnet/minecraft/client/particle/ParticleManager;Lnet/minecraft/client/particle/ParticleTextureSheet;)Lnet/minecraft/client/particle/BillboardParticleRenderer;"), cancellable = true)
	private void createParticleRenderer(ParticleTextureSheet textureSheet, CallbackInfoReturnable<ParticleRenderer<?>> cir) {
		Function<ParticleManager, ParticleRenderer<?>> factory = ParticleRendererRegistryImpl.INSTANCE.getFactory(textureSheet);

		if (factory != null) {
			cir.setReturnValue(factory.apply((ParticleManager) (Object) this));
		}
	}
}
