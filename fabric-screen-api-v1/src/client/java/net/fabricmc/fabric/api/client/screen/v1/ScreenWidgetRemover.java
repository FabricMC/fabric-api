package net.fabricmc.fabric.api.client.screen.v1;

import java.util.function.Predicate;

import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;

public interface ScreenWidgetRemover {
	default <T extends GuiEventListener & Renderable & NarratableEntry> void removeRenderableWidget(T widget) {
		throw new UnsupportedOperationException("Implemented via mixin!");
	}

	default <T extends Renderable> void removeRenderableOnly(T renderable) {
		throw new UnsupportedOperationException("Implemented via mixin!");
	}

	default <T extends GuiEventListener & NarratableEntry> void removeWidget(T widget) {
		throw new UnsupportedOperationException("Implemented via mixin!");
	}

	default <T extends GuiEventListener & Renderable & NarratableEntry> void removeRenderableWidget(Predicate<T> filter) {
		throw new UnsupportedOperationException("Implemented via mixin!");
	}

	default <T extends Renderable> void removeRenderableOnly(Predicate<T> filter) {
		throw new UnsupportedOperationException("Implemented via mixin!");
	}

	default <T extends GuiEventListener & NarratableEntry> void removeWidget(Predicate<T> filter) {
		throw new UnsupportedOperationException("Implemented via mixin!");
	}
}
