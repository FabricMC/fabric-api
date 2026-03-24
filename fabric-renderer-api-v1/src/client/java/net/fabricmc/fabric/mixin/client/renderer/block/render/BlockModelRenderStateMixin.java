package net.fabricmc.fabric.mixin.client.renderer.block.render;

import static net.minecraft.client.renderer.block.BlockModelRenderState.EMPTY_TINTS;

import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.ints.IntList;
import org.joml.Matrix4fc;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;

import net.fabricmc.fabric.api.client.renderer.v1.Renderer;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.MutableMesh;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.client.renderer.v1.render.FabricBlockModelRenderState;

@Mixin(BlockModelRenderState.class)
public abstract class BlockModelRenderStateMixin implements FabricBlockModelRenderState {
	@Shadow
	private @Nullable IntList tintLayers;
	@Shadow
	private @Nullable Matrix4fc transformation;
	@Shadow
	private @Nullable RenderType renderType;
	@Shadow
	private @Nullable RandomSource randomSource;
	@Unique
	@Nullable
	private MutableMesh mesh;

	@Shadow
	@Nullable
	private static Matrix4fc identityToNull(Matrix4fc transformation) {
		return null;
	}

	@Inject(method = "submit", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/block/BlockModelRenderState;submitModel(Lnet/minecraft/client/renderer/rendertype/RenderType;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;III)V", shift = At.Shift.AFTER))
	private void submitMesh(
			PoseStack poseStack,
			SubmitNodeCollector submitNodeCollector,
			int lightCoords,
			int overlayCoords,
			int outlineColor,
			CallbackInfo ci
	) {
		if (this.mesh != null) {
			RenderType renderType = this.renderType;
			int[] tints = this.tintLayers != null ? this.tintLayers.toArray(EMPTY_TINTS) : EMPTY_TINTS;

			if (this.transformation != null) {
				poseStack.pushPose();
				poseStack.mulPose(this.transformation);
				submitNodeCollector.submitBlockModel(poseStack, _ -> renderType, renderType, this.randomSource, this.mesh, tints, lightCoords, overlayCoords, outlineColor, BlockAndTintGetter.EMPTY, BlockPos.ZERO, Blocks.AIR.defaultBlockState());
				poseStack.popPose();
			} else {
				submitNodeCollector.submitBlockModel(poseStack, _ -> renderType, renderType, this.randomSource, this.mesh, tints, lightCoords, overlayCoords, outlineColor, BlockAndTintGetter.EMPTY, BlockPos.ZERO, Blocks.AIR.defaultBlockState());
			}
		}
	}

	@Override
	public QuadEmitter setupMesh(
			Matrix4fc transformation,
			boolean hasTranslucency
	) {
		this.transformation = identityToNull(transformation);
		this.renderType = hasTranslucency ? Sheets.translucentBlockSheet() : Sheets.cutoutBlockSheet();

		if (this.mesh == null) {
			this.mesh = Renderer.get().mutableMesh();
		} else {
			this.mesh.clear();
		}

		return this.mesh.emitter();
	}
}
