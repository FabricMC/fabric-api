package net.fabricmc.fabric.impl.client.rendering;

import java.util.ArrayList;
import java.util.function.Function;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.EntityType;

import net.fabricmc.fabric.api.client.rendering.v1.RenderStateDataExtractor;
import net.fabricmc.fabric.api.client.rendering.v1.RenderStateExtractionCallback;

import org.jspecify.annotations.Nullable;

public record RenderStateExtractionCallbackContextImpl(@Nullable EntityType<?> type, EntityRenderer<?, ?> renderer, EntityRendererProvider.Context rendererContext, ArrayList<Function<EntityRendererProvider.Context, RenderStateDataExtractor>> factories) implements RenderStateExtractionCallback.Context {
	@Override
	public void add(Function<EntityRendererProvider.Context, RenderStateDataExtractor> extractorFactory) {
		factories.add(extractorFactory);
	}
}
