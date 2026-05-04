package net.fabricmc.fabric.mixin.holder.component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;

import net.fabricmc.fabric.impl.holder.component.FabricDataComponentInitializersImpl;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.packs.resources.ResourceManager;

@Mixin(ReloadableServerResources.class)
public class ReloadableServerResourcesMixin {
	@WrapOperation(method = "lambda$loadResources$0", at = @At(value = "INVOKE", target = "Ljava/util/concurrent/CompletableFuture;supplyAsync(Ljava/util/function/Supplier;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;"))
	private static CompletableFuture<?> wrapWithScopedValue(
			Supplier<?> supplier,
			Executor executor,
			Operation<CompletableFuture<?>> original,
			@Local(argsOnly = true, name = "resourceManager") ResourceManager resourceManager
	) {
		Supplier<?> wrappedSupplier = () -> ScopedValue.where(
				FabricDataComponentInitializersImpl.RESOURCE_MANAGER,
				resourceManager
		).call(supplier::get);

		return original.call(wrappedSupplier, executor);
	}
}
