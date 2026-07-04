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

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.data.DataWriter;
import net.minecraft.data.server.tag.TagProvider;
import net.minecraft.registry.tag.TagBuilder;
import net.minecraft.registry.tag.TagEntry;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.impl.datagen.TagBuilderHooks;

@Mixin(TagProvider.class)
public class TagProviderMixin {
	@ModifyArg(method = "method_27046", at = @At(value = "INVOKE", target = "Lnet/minecraft/data/DataProvider;writeToPath(Lnet/minecraft/data/DataWriter;Lcom/google/gson/JsonElement;Ljava/nio/file/Path;)Ljava/util/concurrent/CompletableFuture;"), index = 1)
	private JsonElement addRemove(JsonElement jsonElement, @Local TagBuilder builder) {
		if (builder instanceof TagBuilderHooks) {
			JsonArray jsonArray = new JsonArray();
			((TagBuilderHooks) builder).fabric_getRemove().forEach(entry -> jsonArray.add((entry.tag ? "#" : "") + entry.id.toString()));

			if (!jsonArray.isEmpty()) {
				jsonElement.getAsJsonObject().add("fabric:remove", jsonArray);
			}
		}

		return jsonElement;
	}

	@Inject(method = "method_27046", at = @At(value = "INVOKE", target = "Lnet/minecraft/data/DataProvider;writeToPath(Lnet/minecraft/data/DataWriter;Lcom/google/gson/JsonElement;Ljava/nio/file/Path;)Ljava/util/concurrent/CompletableFuture;"), locals = LocalCapture.CAPTURE_FAILHARD)
	public void addReplaced(Predicate<Identifier> predicate, Predicate<Identifier> predicate2, DataWriter dataWriter, Map.Entry<Identifier, TagBuilder> entry, CallbackInfoReturnable<CompletableFuture<?>> cir, Identifier identifier, TagBuilder builder, List<TagEntry> list, List<TagEntry> list2, JsonElement jsonElement, Path path) {
		if (builder instanceof TagBuilderHooks) {
			jsonElement.getAsJsonObject().addProperty("replace", ((TagBuilderHooks) builder).fabric_isReplaced());
		}
	}
}
