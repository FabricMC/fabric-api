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
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.renderer.ScreenEffectRenderer;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(ScreenEffectRenderer.class)
abstract class ScreenEffectRendererMixin {
	@Unique
	@Nullable
	private static BlockPos pos;

	@Redirect(method = "renderScreenEffect", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/block/BlockModelShaper;getParticleIcon(Lnet/minecraft/world/level/block/state/BlockState;)Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;"))
	private static TextureAtlasSprite getModelParticleSpriteProxy(BlockModelShaper models, BlockState state, @Local Player playerEntity) {
		if (pos != null) {
			TextureAtlasSprite sprite = models.getModelParticleSprite(state, playerEntity.level(), pos);
			pos = null;
			return sprite;
		}

		return models.getParticleIcon(state);
	}

	@Inject(method = "getViewBlockingState", at = @At("RETURN"))
	private static void onReturnGetInWallBlockState(CallbackInfoReturnable<@Nullable BlockState> cir, @Local BlockPos.MutableBlockPos mutable) {
		if (cir.getReturnValue() != null) {
			pos = mutable.immutable();
		} else {
			pos = null;
		}
	}
}
