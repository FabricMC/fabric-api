package net.fabricmc.fabric.api.client.rendering.v1;

import java.util.List;
import java.util.function.Function;

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.EntityType;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

import org.jspecify.annotations.Nullable;

public interface RenderStateExtractionCallback {

	Event<RenderStateExtractionCallback> EVENT = EventFactory.createArrayBacked(
			RenderStateExtractionCallback.class, listeners -> ctx -> {
				for (RenderStateExtractionCallback callback : listeners) {
					callback.onRenderStateExtraction(ctx);
					for (Function<EntityRendererProvider.Context, RenderStateDataExtractor> factory : ctx.factories()) {
						ctx.renderer().addExtractor(factory.apply(ctx.rendererContext()));
					}
				}
			});

	void onRenderStateExtraction(RenderStateExtractionCallback.Context ctx);

	@ApiStatus.NonExtendable
	interface Context {

		@Nullable EntityType<?> type();

		EntityRenderer<?, ?> renderer();

		EntityRendererProvider.Context rendererContext();

		void add(Function<EntityRendererProvider.Context, RenderStateDataExtractor> extractorFactory);

		List<Function<EntityRendererProvider.Context, RenderStateDataExtractor>> factories();

	}
}
