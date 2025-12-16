package net.fabricmc.fabric.test.renderer.client;

import org.jspecify.annotations.NonNull;

import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.TextureSlots;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelDebugName;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.QuadCollection;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.UnbakedGeometry;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;

import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableMesh;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.model.MeshBakedGeometry;
import net.fabricmc.fabric.api.renderer.v1.model.ModelBakeSettingsHelper;

public record ItemWithBlockGeometry(
		Identifier itemId,
		Identifier itemModelId,
		Identifier blockModelId
) implements UnbakedGeometry {
	public ItemWithBlockGeometry(Identifier itemId, Item item, Block block) {
		this(
				itemId,
				getModelLocation(item),
				ModelLocationUtils.getModelLocation(block)
		);
	}

	private static @NonNull Identifier getModelLocation(ItemLike item) {
		if (item instanceof BlockItem blockItem) {
			return ModelLocationUtils.getModelLocation(blockItem.getBlock());
		}

		return ModelLocationUtils.getModelLocation((Item) item);
	}

	@Override
	public QuadCollection bake(
			TextureSlots textureSlots,
			ModelBaker modelBaker,
			ModelState modelState,
			ModelDebugName modelDebugName
	) {
		MutableMesh mutableMesh = Renderer.get().mutableMesh();
		QuadEmitter emitter = mutableMesh.emitter();
		ResolvedModel blockModel = modelBaker.getModel(blockModelId);
		ResolvedModel itemModel = modelBaker.getModel(itemModelId);

		emitter.pushTransform(quad -> {
			// Scale the model down
			for (int vertex = 0; vertex < 4; ++vertex) {
				float x = quad.x(vertex) * 0.4f + 0.35f;
				float y = quad.y(vertex) * 0.4f + 0.35f;
				float z = quad.z(vertex) * 0.4f + 0.1f;
				quad.pos(vertex, x, y, z);
			}

			return true;
		});
		QuadCollection quads = blockModel.bakeTopGeometry(
				textureSlots,
				modelBaker,
				modelState
		);

		for (BakedQuad bakedQuad : quads.getAll()) {
			emitter
					.fromBakedQuad(bakedQuad)
					.emit();
		}

		emitter.popTransform();

		emitter.pushTransform(quad -> {
			for (int i = 0; i < 4; i++) {
				float x = quad.x(i) * 0.4f + 0.35f;
				float y = quad.y(i) * 0.4f + 0.35f;
				float z = quad.z(i) * 0.4f + 0.125f;
				quad.pos(i, x, y, z);
			}

			return true;
		});
		emitter
				.square(Direction.SOUTH, 0.0f, 0.0f, 1.0f, 1.0f, 0.01f)
				.emissive(true);

		emitter.pushTransform(ModelBakeSettingsHelper.asQuadTransform(
				modelState,
				atlas -> modelBaker.sprites().spriteFinder(atlas.getTextureId())
		));
		Material material = itemModel.getTopTextureSlots()
				.getMaterial("layer0");

		if (material == null) {
			material = itemModel.getTopTextureSlots().getMaterial("missingno");
		}

		if (material != null) {
			emitter.spriteBake(
					modelBaker.sprites()
							.get(material, modelDebugName),
					MutableQuadView.BAKE_LOCK_UV
			);
		}

		emitter.emit();

		emitter.popTransform();
		emitter.popTransform();
		return new MeshBakedGeometry(mutableMesh.immutableCopy());
	}
}
