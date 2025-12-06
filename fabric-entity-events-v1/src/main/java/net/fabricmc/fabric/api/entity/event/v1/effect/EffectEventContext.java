package net.fabricmc.fabric.api.entity.event.v1.effect;

import org.jspecify.annotations.Nullable;

/**
 * Context for {@linkplain ServerMobEffectEvents mob effect events}.
 */
public interface EffectEventContext {
	/**
	 * @return whether the caller of the event is a command
	 */
	boolean isFromCommand();

	/**
	 * @return the name of the command the event was called in
	 * or {@code null} if {@link #isFromCommand()} returns false
	 */
	@Nullable String commandName();
}
