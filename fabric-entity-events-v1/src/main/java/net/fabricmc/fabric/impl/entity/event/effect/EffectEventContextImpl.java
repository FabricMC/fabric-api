package net.fabricmc.fabric.impl.entity.event.effect;

import net.fabricmc.fabric.api.entity.event.v1.effect.EffectEventContext;

public record EffectEventContextImpl(
		boolean isFromCommand,
		String commandName
) implements EffectEventContext {
	public static final EffectEventContext DEFAULT = new EffectEventContextImpl(
			false,
			null
	);
}
