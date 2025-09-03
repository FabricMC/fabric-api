package net.fabricmc.fabric.mixin.client.rendering;

import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;

import net.fabricmc.fabric.api.client.rendering.v1.FabricRenderState;
import net.fabricmc.fabric.api.client.rendering.v1.RenderStateDataKey;

import net.minecraft.class_11954;
import net.minecraft.client.render.entity.state.EntityRenderState;

import net.minecraft.client.render.item.ItemRenderState;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.Map;

@Mixin({EntityRenderState.class, class_11954.class, ItemRenderState.class, ItemRenderState.LayerRenderState.class})
public abstract class RenderStateMixin implements FabricRenderState {

	@Unique
	@Nullable
	private Map<RenderStateDataKey<?>, Object> renderStateData;

	@Override
	@SuppressWarnings("unchecked")
	public <T> @Nullable T getData(RenderStateDataKey<T> key) {
		return renderStateData == null ? null : (T) renderStateData.get(key);
	}

	@Override
	public <T> void setData(RenderStateDataKey<T> key, T value) {
		if (renderStateData == null) {
			renderStateData = new Reference2ObjectOpenHashMap<>();
		}
		renderStateData.put(key, value);
	}

	@Override
	public void clearData() {
		if (renderStateData != null) {
			renderStateData.clear();
		}
	}
}
