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

package net.fabricmc.fabric.mixin.renderer.client.block.particle;

import com.llamalad7.mixinextras.sugar.Local;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.BlockState;
import net.minecraft.client.gui.hud.InGameOverlayRenderer;
import net.minecraft.client.render.block.BlockModels;
import net.minecraft.client.texture.Sprite;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;

@Mixin(InGameOverlayRenderer.class)
abstract class InGameOverlayRendererMixin {
	@Unique
	@Nullable
	private static BlockPos pos;

	@Redirect(method = "renderOverlays", at = @At(value = "INVOKE", target = "net/minecraft/client/render/block/BlockModels.getModelParticleSprite(Lnet/minecraft/block/BlockState;)Lnet/minecraft/client/texture/Sprite;"))
	private static Sprite getModelParticleSpriteProxy(BlockModels models, BlockState state, @Local PlayerEntity playerEntity) {
		if (pos != null) {
			Sprite sprite = models.getModelParticleSprite(state, playerEntity.getEntityWorld(), pos);
			pos = null;
			return sprite;
		}

		return models.getModelParticleSprite(state);
	}

	@Inject(method = "getInWallBlockState", at = @At("RETURN"))
	private static void onReturnGetInWallBlockState(CallbackInfoReturnable<@Nullable BlockState> cir, @Local BlockPos.Mutable mutable) {
		if (cir.getReturnValue() != null) {
			pos = mutable.toImmutable();
		} else {
			pos = null;
		}
	}
}
