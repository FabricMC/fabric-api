/*
 * Copyright (c) 2016, 2017, 2018, 2019 FabricMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.fabricmc.fabric.mixin.networking;

import java.util.Queue;
import java.util.Set;
import java.util.function.Function;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.network.ClientConnection;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.server.network.ServerCommonNetworkHandler;
import net.minecraft.server.network.ServerConfigurationNetworkHandler;
import net.minecraft.server.network.ServerPlayerConfigurationTask;

import net.fabricmc.fabric.api.networking.v1.FabricServerConfigurationNetworkHandler;
import net.fabricmc.fabric.impl.networking.FabricRegistryByteBuf;
import net.fabricmc.fabric.impl.networking.NetworkHandlerExtensions;
import net.fabricmc.fabric.impl.networking.server.ServerConfigurationNetworkAddon;

// We want to apply a bit earlier than other mods which may not use us in order to prevent refCount issues
@Mixin(value = ServerConfigurationNetworkHandler.class, priority = 900)
public abstract class ServerConfigurationNetworkHandlerMixin extends ServerCommonNetworkHandler implements NetworkHandlerExtensions, FabricServerConfigurationNetworkHandler {
	@Shadow
	@Nullable
	private ServerPlayerConfigurationTask currentTask;

	@Shadow
	protected abstract void onTaskFinished(ServerPlayerConfigurationTask.Key key);

	@Shadow
	@Final
	private Queue<ServerPlayerConfigurationTask> tasks;

	@Shadow
	public abstract boolean isConnectionOpen();

	@Shadow
	public abstract void sendConfigurations();

	@Unique
	private ServerConfigurationNetworkAddon addon;

	@Unique
	private boolean sentConfiguration;

	@Unique
	private boolean earlyTaskExecution;

	public ServerConfigurationNetworkHandlerMixin(MinecraftServer server, ClientConnection connection, ConnectedClientData arg) {
		super(server, connection, arg);
	}

	@Inject(method = "<init>", at = @At("RETURN"))
	private void initAddon(CallbackInfo ci) {
		this.addon = new ServerConfigurationNetworkAddon((ServerConfigurationNetworkHandler) (Object) this, this.server);
		// A bit of a hack but it allows the field above to be set in case someone registers handlers during INIT event which refers to said field
		this.addon.lateInit();
	}

	@Inject(method = "sendConfigurations", at = @At("HEAD"), cancellable = true)
	private void onClientReady(CallbackInfo ci) {
		// Send the initial channel registration packet
		if (this.addon.startConfiguration()) {
			if (currentTask != null) {
				throw new IllegalStateException("A task is already running: " + currentTask.getKey().id());
			}

			ci.cancel();
			return;
		}

		// Ready to start sending packets
		if (!sentConfiguration) {
			this.addon.preConfiguration();
			sentConfiguration = true;
			earlyTaskExecution = true;
		}

		// Run the early tasks
		if (earlyTaskExecution) {
			if (pollEarlyTasks()) {
				ci.cancel();
				return;
			} else {
				earlyTaskExecution = false;
			}
		}

		// All early tasks should have been completed
		if (currentTask != null || !tasks.isEmpty()) {
			throw new IllegalStateException("All early tasks should have been completed, current: " + currentTask + ", queued: " + tasks.size());
		}

		// Run the vanilla tasks.
		this.addon.configuration();
	}

	@Unique
	private boolean pollEarlyTasks() {
		if (!earlyTaskExecution) {
			throw new IllegalStateException("Early task execution has finished");
		}

		if (this.currentTask != null) {
			throw new IllegalStateException("Task " + this.currentTask.getKey().id() + " has not finished yet");
		}

		if (!this.isConnectionOpen()) {
			return false;
		}

		final ServerPlayerConfigurationTask task = this.tasks.poll();

		if (task != null) {
			this.currentTask = task;
			task.sendPacket(this::sendPacket);
			return true;
		}

		return false;
	}

	@Override
	public ServerConfigurationNetworkAddon getAddon() {
		return addon;
	}

	@Override
	public void addTask(ServerPlayerConfigurationTask task) {
		tasks.add(task);
	}

	@Override
	public void completeTask(ServerPlayerConfigurationTask.Key key) {
		if (!earlyTaskExecution) {
			onTaskFinished(key);
			return;
		}

		final ServerPlayerConfigurationTask.Key currentKey = this.currentTask != null ? this.currentTask.getKey() : null;

		if (!key.equals(currentKey)) {
			throw new IllegalStateException("Unexpected request for task finish, current task: " + currentKey + ", requested: " + key);
		}

		this.currentTask = null;
		sendConfigurations();
	}

	@WrapOperation(method = "onReady", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/RegistryByteBuf;makeFactory(Lnet/minecraft/registry/DynamicRegistryManager;)Ljava/util/function/Function;"))
	private Function<ByteBuf, RegistryByteBuf> bindChannelInfo(DynamicRegistryManager registryManager, Operation<Function<ByteBuf, RegistryByteBuf>> original) {
		return original.call(registryManager).andThen(registryByteBuf -> {
			FabricRegistryByteBuf fabricRegistryByteBuf = (FabricRegistryByteBuf) registryByteBuf;
			fabricRegistryByteBuf.fabric_setSendableConfigurationChannels(Set.copyOf(addon.getSendableChannels()));
			return registryByteBuf;
		});
	}
}
