package net.fabricmc.fabric.api.networking.v1;

import net.minecraft.nbt.NbtElement;
import net.minecraft.server.network.ServerPlayerEntity;

import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface CustomClickActionListener {
	void handleCustomClickAction(ServerPlayerEntity player, @Nullable NbtElement payload);
}
