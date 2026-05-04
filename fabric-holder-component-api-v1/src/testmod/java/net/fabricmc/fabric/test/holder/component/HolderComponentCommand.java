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

package net.fabricmc.fabric.test.holder.component;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.serialization.Codec;

import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.commands.arguments.ResourceKeyArgument;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

public class HolderComponentCommand {
	private static final DynamicCommandExceptionType INVALID_REGISTRY = new DynamicCommandExceptionType(
			value -> Component.literal("Invalid Registry '" + value + "'")
	);

	private static final ResourceKey<Registry<Registry<?>>> ROOT_KEY = ResourceKey.createRegistryKey(Registries.ROOT_REGISTRY_NAME);

	private static CompletableFuture<Suggestions> suggestRegistries(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
		return SharedSuggestionProvider.suggestResource(
				context.getSource().registryAccess().registries().map(registryEntry -> registryEntry.key().identifier()),
				builder
		);
	}

	private static CompletableFuture<Suggestions> suggestHolders(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) throws CommandSyntaxException {
		ResourceKey<Registry<?>> key = ResourceKeyArgument.getRegistryKey(
				context,
				"registry",
				ROOT_KEY,
				INVALID_REGISTRY
		);

		return SharedSuggestionProvider.suggestResource(
				context.getSource().registryAccess().lookupOrThrow(key)
						.listElementIds()
						.map(ResourceKey::identifier),
				builder
		);
	}

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext buildContext) {
		dispatcher.register(literal("holder_component")
				.requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
				.then(literal("get_holder")
						.then(argument("registry", ResourceKeyArgument.key(ROOT_KEY))
								.suggests(HolderComponentCommand::suggestRegistries)
								.then(argument("holder", IdentifierArgument.id())
										.suggests(HolderComponentCommand::suggestHolders)
										.executes(context -> get(
												context,
												ResourceKeyArgument.getRegistryKey(
														context,
														"registry",
														ROOT_KEY,
														INVALID_REGISTRY
												),
												IdentifierArgument.getId(context, "holder")
										)))))
				.then(literal("dump_registry")
						.then(argument("component", ResourceArgument.resource(buildContext, Registries.DATA_COMPONENT_TYPE))
								.then(argument("registry", ResourceKeyArgument.key(ROOT_KEY))
										.suggests(HolderComponentCommand::suggestRegistries)
										.executes(context -> dump(
												context,
												ResourceArgument.getResource(
														context,
														"component",
														Registries.DATA_COMPONENT_TYPE
												).value(),
												ResourceKeyArgument.getRegistryKey(
														context,
														"registry",
														ROOT_KEY,
														INVALID_REGISTRY
												)
										)))))
		);
	}

	private static final Codec<Map<Identifier, DataComponentPatch>> CODEC = Codec.unboundedMap(
			Identifier.CODEC,
			DataComponentPatch.CODEC
	);

	private static int dump(CommandContext<CommandSourceStack> context, DataComponentType<?> component, ResourceKey<Registry<?>> registryKey) {
		RegistryAccess registryManager = context.getSource().registryAccess();
		Registry<?> registry = registryManager.lookupOrThrow(registryKey);

		Map<Identifier, DataComponentPatch.Builder> builders = new HashMap<>();

		for (Holder<?> holder : registry.asHolderIdMap()) {
			holder.unwrapKey().ifPresent(
					key -> {
						Object val = holder.components().get(component);

						if (val != null) {
							DataComponentPatch.Builder builder = builders.computeIfAbsent(key.identifier(), _ -> DataComponentPatch.builder());

							builder.set(holder.components());
						}
					}
			);
		}

		Map<Identifier, DataComponentPatch> patches = builders.entrySet().stream().collect(Collectors.toMap(
				Map.Entry::getKey,
				entry -> entry.getValue().build()
		));

		context.getSource().sendSuccess(() ->
				NbtUtils.toPrettyComponent(CODEC.encodeStart(registryManager.createSerializationContext(NbtOps.INSTANCE), patches)
						.getOrThrow()), true);
		return Command.SINGLE_SUCCESS;
	}

	private static int get(CommandContext<CommandSourceStack> context, ResourceKey<Registry<?>> registryKey, Identifier holderId) {
		RegistryAccess registryManager = context.getSource().registryAccess();
		Registry<?> registry = registryManager.lookupOrThrow(registryKey);

		Holder.Reference<?> holder = registry.get(holderId).orElseThrow();

		context.getSource().sendSuccess(() ->
				NbtUtils.toPrettyComponent(DataComponentMap.CODEC.encodeStart(registryManager.createSerializationContext(NbtOps.INSTANCE), holder.components())
						.getOrThrow()), true);
		return Command.SINGLE_SUCCESS;
	}
}
