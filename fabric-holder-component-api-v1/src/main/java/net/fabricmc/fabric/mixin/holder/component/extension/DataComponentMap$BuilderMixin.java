package net.fabricmc.fabric.mixin.holder.component.extension;

import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;

import net.fabricmc.fabric.api.holder.component.v1.FabricDataComponentMapBuilder;

import net.minecraft.core.component.DataComponentMap;

import net.minecraft.core.component.DataComponentPatch;

import net.minecraft.core.component.DataComponentType;

import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;
import java.util.Optional;

@Mixin(DataComponentMap.Builder.class)
public abstract class DataComponentMap$BuilderMixin implements FabricDataComponentMapBuilder {
	@Shadow
	public abstract <T> DataComponentMap.Builder set(DataComponentType<T> type, @Nullable T value);

	@Shadow
	abstract <T> void setUnchecked(DataComponentType<T> type, @Nullable Object value);

	@Shadow
	@Final
	private Reference2ObjectMap<DataComponentType<?>, Object> map;

	@Override
	public DataComponentMap.Builder apply(DataComponentPatch patch) {
		for (Map.Entry<DataComponentType<?>, Optional<?>> entry : patch.entrySet()) {
			entry.getValue().ifPresentOrElse(
					value -> this.setUnchecked(entry.getKey(), value),
					() -> this.set(entry.getKey(), null)
			);
		}

		return (DataComponentMap.Builder) (Object) this;
	}

	@Override
	public DataComponentMap.Builder clear() {
		map.clear();

		return (DataComponentMap.Builder) (Object) this;
	}
}
