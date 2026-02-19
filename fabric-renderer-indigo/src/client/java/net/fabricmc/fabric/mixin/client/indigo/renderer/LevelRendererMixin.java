package net.fabricmc.fabric.mixin.client.indigo.renderer;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BakedQuadOutput;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

import net.fabricmc.fabric.impl.client.indigo.Indigo;

@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {
	@WrapOperation(
			method = "renderBlockDestroyAnimation",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/renderer/block/BlockRenderDispatcher;renderBreakingTexture(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/BlockAndTintGetter;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/block/BakedQuadOutput;)V"
			)
	)
	private void onRenderBlockDestroyAnimation(
			BlockRenderDispatcher instance,
			BlockState state,
			BlockPos pos,
			BlockAndTintGetter level,
			PoseStack poseStack,
			BakedQuadOutput output,
			Operation<Void> original,
			@Local(name = "bufferSource") MultiBufferSource.BufferSource bufferSource
	) {
		ScopedValue.where(Indigo.LEVEL_RENDERER_BUFFER_SOURCE, bufferSource).call(original::call);
	}
}
