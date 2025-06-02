package net.fabricmc.fabric.impl.client.rendering.hud;

import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;

public interface HudLayer {
	Identifier id();

	HudElement element();

	static HudLayer of(Identifier id, HudElement element) {
		return new HudLayer() {
			@Override
			public Identifier id() {
				return id;
			}

			@Override
			public HudElement element() {
				return element;
			}
		};
	}
}
