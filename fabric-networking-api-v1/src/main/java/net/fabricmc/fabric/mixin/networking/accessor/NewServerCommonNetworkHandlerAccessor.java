package net.fabricmc.fabric.mixin.networking.accessor;

import com.mojang.authlib.GameProfile;

import net.minecraft.class_10972;
import net.minecraft.network.ClientConnection;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(class_10972.class)
public interface NewServerCommonNetworkHandlerAccessor {
	@Accessor
	ClientConnection getField_58317();

	@Invoker("getProfile")
	GameProfile invokeGetProfile();
}
