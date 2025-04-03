package net.fabricmc.fabric.mixin.networking.accessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.class_10972;
import net.minecraft.network.ClientConnection;

@Mixin(class_10972.class)
public interface AbstractNetworkHandlerAccessor {
	@Accessor("field_58317")
	ClientConnection getConnection();
}
