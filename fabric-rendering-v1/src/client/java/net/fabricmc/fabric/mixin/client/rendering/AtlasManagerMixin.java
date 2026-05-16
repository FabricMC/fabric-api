package net.fabricmc.fabric.mixin.client.rendering;

import com.google.common.collect.ImmutableList;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import net.fabricmc.fabric.impl.client.rendering.AtlasRegistryImpl;

import net.minecraft.client.resources.model.sprite.AtlasManager;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Mixin(AtlasManager.class)
class AtlasManagerMixin {
	@ModifyExpressionValue(method = "<clinit>", at = @At(value = "INVOKE", target = "Ljava/util/List;of([Ljava/lang/Object;)Ljava/util/List;"))
	private static List<AtlasManager.AtlasConfig> addAtlases(List<AtlasManager.AtlasConfig> original) {
		final var builder = ImmutableList.<AtlasManager.AtlasConfig>builder();
		builder.addAll(original);
		builder.addAll(AtlasRegistryImpl.getConfigs());
		return builder.build();
	}
}
