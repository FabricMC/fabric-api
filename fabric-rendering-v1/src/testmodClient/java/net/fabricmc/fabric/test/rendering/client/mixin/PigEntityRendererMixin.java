package net.fabricmc.fabric.test.rendering.client.mixin;

import net.fabricmc.fabric.api.client.rendering.v1.FabricRenderState;
import net.fabricmc.fabric.api.client.rendering.v1.RenderStateDataKey;

import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.block.MovingBlockRenderState;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.PigEntityRenderer;

import net.minecraft.client.render.entity.state.PigEntityRenderState;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.passive.PigEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Tests {@link RenderStateDataKey} and {@link FabricRenderState}. Pigs will render the block they're standing on at their location.
 */
@Mixin(PigEntityRenderer.class)
public class PigEntityRendererMixin {

	@Unique
	private static final RenderStateDataKey<MovingBlockRenderState> MOVING_BLOCK = RenderStateDataKey.create(() -> "Moving block");

	@Inject(method = "updateRenderState(Lnet/minecraft/entity/passive/PigEntity;Lnet/minecraft/client/render/entity/state/PigEntityRenderState;F)V", at = @At("TAIL"))
	private void updateRenderStateData(PigEntity entity, PigEntityRenderState state, float tickProgress, CallbackInfo ci) {
		BlockState blockState = entity.getSteppingBlockState();
		if(blockState.getRenderType() != BlockRenderType.INVISIBLE) {
			MovingBlockRenderState movingBlockRenderState = new MovingBlockRenderState();
			movingBlockRenderState.fallingBlockPos = entity.getSteppingPos();
			movingBlockRenderState.entityBlockPos = entity.getBlockPos();
			movingBlockRenderState.blockState = entity.getSteppingBlockState();
			movingBlockRenderState.biome = entity.getEntityWorld().getBiome(entity.getBlockPos());
			movingBlockRenderState.world = entity.getEntityWorld();
			state.setData(MOVING_BLOCK, movingBlockRenderState);
		}
	}

	@Inject(method = "render(Lnet/minecraft/client/render/entity/state/PigEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/MobEntityRenderer;render(Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;)V"))
	private void renderUsingRenderStateData(PigEntityRenderState state, MatrixStack matrices, OrderedRenderCommandQueue queue, CallbackInfo ci) {
		MovingBlockRenderState movingBlockRenderState = state.getData(MOVING_BLOCK);
		if (movingBlockRenderState != null) {
			queue.submitMovingBlock(matrices, movingBlockRenderState);
		}
	}

}
