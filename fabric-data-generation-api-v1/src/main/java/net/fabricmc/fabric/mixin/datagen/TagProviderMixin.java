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

package net.fabricmc.fabric.mixin.datagen;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.data.DataOutput;
import net.minecraft.data.DataWriter;
import net.minecraft.data.tag.TagProvider;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.tag.TagBuilder;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.fabricmc.fabric.impl.datagen.FabricTagBuilder;
import net.fabricmc.fabric.impl.datagen.TagAliasGenerator;

@Mixin(TagProvider.class)
public class TagProviderMixin<T> {
	@Shadow
	@Final
	protected RegistryKey<? extends Registry<T>> registryRef;

	@Unique
	private DataOutput.PathResolver tagAliasPathResolver;

	@Inject(method = "<init>(Lnet/minecraft/data/DataOutput;Lnet/minecraft/registry/RegistryKey;Ljava/util/concurrent/CompletableFuture;Ljava/util/concurrent/CompletableFuture;)V", at = @At("RETURN"))
	private void initPathResolver(DataOutput output, RegistryKey<? extends Registry<T>> registryRef, CompletableFuture<?> registriesFuture, CompletableFuture<?> parentTagLookupFuture, CallbackInfo info) {
		tagAliasPathResolver = output.getResolver(DataOutput.OutputType.DATA_PACK, TagAliasGenerator.getDirectory(registryRef));
	}

	@ModifyArg(method = "method_27046", at = @At(value = "INVOKE", target = "Lnet/minecraft/registry/tag/TagFile;<init>(Ljava/util/List;Z)V"), index = 1)
	private boolean addReplaced(boolean replaced, @Local TagBuilder tagBuilder) {
		if (tagBuilder instanceof FabricTagBuilder fabricTagBuilder) {
			return fabricTagBuilder.fabric_isReplaced();
		}

		return replaced;
	}

	@SuppressWarnings("unchecked")
	@WrapOperation(method = "method_49659", at = @At(value = "INVOKE", target = "Ljava/util/concurrent/CompletableFuture;allOf([Ljava/util/concurrent/CompletableFuture;)Ljava/util/concurrent/CompletableFuture;"))
	private CompletableFuture<Void> addTagAliasGroupBuilders(CompletableFuture<?>[] futures, Operation<CompletableFuture<Void>> original, @Local(argsOnly = true) DataWriter writer) {
		if ((Object) this instanceof FabricTagProvider<?>) {
			// Note: no pattern matching instanceof so that we can cast directly to FabricTagProvider<T> instead of a wildcard
			Map<Identifier, FabricTagProvider<T>.AliasGroupBuilder> builders = ((FabricTagProvider<T>) (Object) this).getAliasGroupBuilders();
			CompletableFuture<?>[] newFutures = Arrays.copyOf(futures, futures.length + builders.size());
			int index = futures.length;

			for (Map.Entry<Identifier, FabricTagProvider<T>.AliasGroupBuilder> entry : builders.entrySet()) {
				newFutures[index++] = TagAliasGenerator.writeTagAlias(writer, tagAliasPathResolver, registryRef, entry.getKey(), entry.getValue().getTags());
			}

			return original.call((Object) newFutures);
		} else {
			return original.call((Object) futures);
		}
	}
}
