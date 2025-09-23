package net.fabricmc.fabric.impl.recipe.sync;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.recipe.PreparedRecipes;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.RecipeType;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public record FabricSyncRecipesPayload(Set<RecipeType<?>> recipeTypes,
                                       List<RecipeEntry<?>> recipeEntries) implements CustomPayload {
	public static final Id<FabricSyncRecipesPayload> ID = new Id<>(Identifier.of("fabric-recipe-api-v1", "recipe_content"));
	public static final PacketCodec<RegistryByteBuf, FabricSyncRecipesPayload> PACKET_CODEC = PacketCodec.tuple(
			PacketCodecs.registryValue(RegistryKeys.RECIPE_TYPE)
					.collect(PacketCodecs.toCollection(HashSet::new)),
			FabricSyncRecipesPayload::recipeTypes,
			RecipeEntry.PACKET_CODEC.collect(PacketCodecs.toList()),
			FabricSyncRecipesPayload::recipeEntries,
			FabricSyncRecipesPayload::new
	);

	public static FabricSyncRecipesPayload create(Collection<RecipeType<?>> recipeTypes, PreparedRecipes recipes) {
		var recipeTypeSet = Set.copyOf(recipeTypes);
		if (recipeTypeSet.isEmpty()) {
			return new FabricSyncRecipesPayload(recipeTypeSet, List.of());
		} else {
			var recipeSubset = recipes.recipes().stream().filter(h -> recipeTypeSet.contains(h.value().getType())).toList();
			return new FabricSyncRecipesPayload(recipeTypeSet, recipeSubset);
		}
	}

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}
}
