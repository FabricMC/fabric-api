package net.fabricmc.fabric.mixin.entity.event.effect;

import com.mojang.brigadier.context.ContextChain;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.commands.execution.tasks.BuildContexts;

@Mixin(BuildContexts.class)
public interface BuildContextsAccessor<S> {
	@Accessor
	ContextChain<S> getCommand();
}
