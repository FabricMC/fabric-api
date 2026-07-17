package net.fabricmc.fabric.mixin.advancement;

import net.fabricmc.fabric.api.advancement.event.v1.AdvancementEvents;
import net.fabricmc.fabric.api.advancement.event.v1.AdvancementSource;
import net.fabricmc.fabric.api.advancement.event.v1.AdvancementUtil;
import net.fabricmc.fabric.api.advancement.event.v1.FabricAdvancementBuilder;
import net.minecraft.advancements.Advancement;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Mixin(ServerAdvancementManager.class)
public class ServerAdvancementManagerMixin {
	@Unique
	private static final String ADVANCEMENT_PATH = Registries.ADVANCEMENT.identifier().getPath();

	@Final
	@Shadow
	private HolderLookup.Provider registries;

	@Inject(method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V", at = @At("HEAD"))
	private void onApply(Map<Identifier, Advancement> preparations, ResourceManager manager, ProfilerFiller profiler, CallbackInfo ci) {
		Map<Identifier, Advancement> modifiedAdvancements = new HashMap<>();

		preparations.forEach((id, advancement) -> {
			Optional<Resource> resource = manager.getResource(Identifier.fromNamespaceAndPath(id.getNamespace(), ADVANCEMENT_PATH + "/" + id.getPath() + ".json"));

			// Map the resource to its AdvancementSource enum, defaulting to DATA_PACK
			AdvancementSource source = resource.map(AdvancementUtil::determineSource).orElse(AdvancementSource.DATA_PACK);

			// replace event
			Advancement replacement = AdvancementEvents.REPLACE.invoker().replaceAdvancement(id, advancement, source, registries);
			if (replacement != null) {
				advancement = replacement;
				source = AdvancementSource.REPLACED;
			}

			// Turn the current advancement into a modifiable builder and then modify event
			Advancement.Builder builder = FabricAdvancementBuilder.copyOf(advancement);
			AdvancementEvents.MODIFY.invoker().modifyAdvancement(id, builder, source, registries);

			// Build the advancement and store it
			modifiedAdvancements.put(id, builder.build(id).value());
		});

		// Replace the original map contents with the modified ones
		preparations.clear();
		preparations.putAll(modifiedAdvancements);
	}

	@Inject(method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V", at = @At("RETURN"))
	private void onLoaded(Map<Identifier, Advancement> preparations, ResourceManager manager, ProfilerFiller profiler, CallbackInfo ci) {
		// After everything, so all advancements are loaded
		AdvancementEvents.ALL_LOADED.invoker().onAdvancementsLoaded(manager, preparations, registries);
	}
}
