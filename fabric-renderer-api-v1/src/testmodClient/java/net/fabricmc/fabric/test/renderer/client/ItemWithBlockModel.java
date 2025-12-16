package net.fabricmc.fabric.test.renderer.client;

import java.util.HashMap;
import java.util.Map;

import org.jspecify.annotations.Nullable;

import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.block.model.TextureSlots;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelDebugName;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.QuadCollection;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.UnbakedGeometry;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import net.fabricmc.fabric.api.client.model.loading.v1.ExtraModelKey;
import net.fabricmc.fabric.api.client.model.loading.v1.UnbakedExtraModel;
import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableMesh;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadAtlas;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.model.MeshBakedGeometry;
import net.fabricmc.fabric.api.renderer.v1.model.ModelBakeSettingsHelper;

public class ItemWithBlockModel implements ItemModel {
	public static final Map<Identifier, ExtraModelKey<ItemModel>> MODEL_KEYS = new HashMap<>();
	private final MeshBakedGeometry geometry;

	protected ItemWithBlockModel(MeshBakedGeometry geometry) {
		this.geometry = geometry;
	}

	@Override
	public void update(
			ItemStackRenderState itemStackRenderState,
			ItemStack itemStack,
			ItemModelResolver itemModelResolver,
			ItemDisplayContext itemDisplayContext,
			@Nullable ClientLevel clientLevel,
			@Nullable ItemOwner itemOwner,
			int seed
	) {
		ItemStackRenderState.LayerRenderState layer = itemStackRenderState.newLayer();
		// Layers require a default RenderType, else they do not render
		layer.setRenderType(Sheets.translucentItemSheet());
		layer.setRenderTypeGetter((quadAtlas, sectionLayer) -> {
			if (quadAtlas == QuadAtlas.BLOCK) {
				return switch (sectionLayer) {
				case SOLID -> Sheets.solidBlockSheet();
				case CUTOUT -> Sheets.cutoutBlockSheet();
				case null, default -> Sheets.translucentBlockItemSheet();
				};
			} else if (quadAtlas == QuadAtlas.ITEM) {
				return Sheets.translucentItemSheet();
			}

			return null;
		});
		layer.setTransform(ItemTransforms.NO_TRANSFORMS.getTransform(itemDisplayContext));
		layer.prepareTintLayers(seed);
		layer.setFoilType(itemStack.hasFoil() ? ItemStackRenderState.FoilType.STANDARD : ItemStackRenderState.FoilType.NONE);

		QuadEmitter emitter = layer.emitter();
		this.geometry.getMesh().forEach(quad -> {
			emitter.copyFrom(quad);
			emitter.emit();
		});
	}

	public record Unbaked(Identifier itemId, Identifier itemModelId, Identifier blockModelId) implements UnbakedGeometry {
		public static final Map<Identifier, MeshBakedGeometry> BAKED_GEOMETRY = new HashMap<>();

		public Unbaked(Identifier itemId, Item item, Block block) {
			this(
					itemId,
					ModelLocationUtils.getModelLocation(item),
					ModelLocationUtils.getModelLocation(block)
			);
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
			Material material = itemModel.getTopTextureSlots().getMaterial("layer0");

			if (material == null) {
				material = itemModel.getTopTextureSlots().getMaterial("missingno");
			}

			if (material != null) {
				emitter.spriteBake(modelBaker.sprites().get(material, modelDebugName), MutableQuadView.BAKE_LOCK_UV);
			}

			emitter.emit();

			emitter.popTransform();
			emitter.popTransform();
			MeshBakedGeometry meshBakedGeometry = new MeshBakedGeometry(
					mutableMesh.immutableCopy());
			BAKED_GEOMETRY.put(itemId, meshBakedGeometry);
			return meshBakedGeometry;
		}
	}

	public record UnbakedExtra(Identifier itemId, Identifier itemModelId, Identifier blockModelId) implements UnbakedExtraModel<ItemModel> {
		public UnbakedExtra(Identifier itemId, Item item, Block block) {
			this(
					itemId,
					ModelLocationUtils.getModelLocation(item),
					ModelLocationUtils.getModelLocation(block)
			);
		}

		@Override
		public ItemModel bake(ModelBaker baker) {
			// awful hack lol
			return new ItemWithBlockModel(Unbaked.BAKED_GEOMETRY.get(itemId));
		}

		@Override
		public void resolveDependencies(Resolver resolver) {
		}
	}
}
