package net.fabricmc.fabric.mixin.client.rendering;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.client.resources.model.sprite.AtlasManager;

import net.fabricmc.fabric.impl.client.rendering.AtlasRegistryImpl;

@Mixin(AtlasManager.class)
class AtlasManagerMixin {
	@ModifyExpressionValue(method = "<init>", at = @At(value = "FIELD", target = "Lnet/minecraft/client/resources/model/sprite/AtlasManager;KNOWN_ATLASES:Ljava/util/List;", opcode = Opcodes.GETSTATIC))
	private static List<AtlasManager.AtlasConfig> addAtlases(List<AtlasManager.AtlasConfig> original) {
		final ImmutableList.Builder<AtlasManager.AtlasConfig> builder = ImmutableList.builder();
		builder.addAll(original);
		builder.addAll(AtlasRegistryImpl.finalizeConfigs());
		return builder.build();
	}
}
