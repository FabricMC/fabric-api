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

package net.fabricmc.fabric.mixin.tag;

import java.util.Collections;
import java.util.List;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.serialization.Codec;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.tags.TagEntry;
import net.minecraft.tags.TagFile;

import net.fabricmc.fabric.api.tag.v1.FabricTagFile;
import net.fabricmc.fabric.impl.tag.TagFileHooks;
import net.fabricmc.fabric.impl.tag.TagRemovalInternals;

@Mixin(TagFile.class)
class TagFileMixin implements FabricTagFile, TagFileHooks {
	@Unique
	private List<TagEntry> remove = Collections.emptyList();

	@ModifyExpressionValue(method = "<clinit>", at = @At(value = "INVOKE", target = "Lcom/mojang/serialization/codecs/RecordCodecBuilder;create(Ljava/util/function/Function;)Lcom/mojang/serialization/Codec;"))
	private static Codec<TagFile> modifyCodec(Codec<TagFile> original) {
		return TagRemovalInternals.modifyTagFileCodec(original);
	}

	@Override
	public List<TagEntry> remove() {
		return Collections.unmodifiableList(remove);
	}

	@Override
	public void fabric_setRemove(List<TagEntry> remove) {
		this.remove = remove;
	}
}
