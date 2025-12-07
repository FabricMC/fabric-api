package net.fabricmc.fabric.mixin.entity.event.effect;

import java.util.List;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ContextChain;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ContextChain.class)
public interface ContextChainAccessor<S> {
	@Accessor
	List<CommandContext<S>> getModifiers();
}
