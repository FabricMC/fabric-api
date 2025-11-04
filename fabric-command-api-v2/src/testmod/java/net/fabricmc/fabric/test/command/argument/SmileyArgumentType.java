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

package net.fabricmc.fabric.test.command.argument;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;

public class SmileyArgumentType implements ArgumentType<SmileyArgument> {
	private static final Collection<String> SMILEY_VALUES = Arrays.stream(SmileyArgument.values()).map(s -> s.smiley).toList();
	public static final DynamicCommandExceptionType INVALID_COLOR_EXCEPTION = new DynamicCommandExceptionType(smiley -> {
		return Component.literal("Invalid smiley: " + smiley); // use Text.translatable in your mod!
	});

	public static SmileyArgumentType smiley() {
		return new SmileyArgumentType();
	}

	@Override
	public SmileyArgument parse(StringReader reader) throws CommandSyntaxException {
		// Note: normally we would use reader.readUnquotedString(), but that won't allow : that we want for our smiley!
		String string = "" + reader.read();

		if (reader.canRead()) {
			string += reader.read();

			for (SmileyArgument possibleSmiley : SmileyArgument.values()) {
				if (string.equals(possibleSmiley.smiley)) {
					return possibleSmiley;
				}
			}
		}

		throw INVALID_COLOR_EXCEPTION.create(string);
	}

	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
		return SharedSuggestionProvider.suggest(SMILEY_VALUES, builder);
	}

	@Override
	public Collection<String> getExamples() {
		return SMILEY_VALUES;
	}
}
