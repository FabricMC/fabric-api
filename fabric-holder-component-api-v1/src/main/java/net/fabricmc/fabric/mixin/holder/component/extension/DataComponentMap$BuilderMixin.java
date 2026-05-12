package net.fabricmc.fabric.mixin.holder.component.extension;

import net.fabricmc.fabric.api.holder.component.v1.FabricDataComponentMapBuilder;

import net.minecraft.core.component.DataComponentMap;

import net.minecraft.core.component.DataComponentPatch;

import net.minecraft.core.component.DataComponentType;

import org.jspecify.annotations.Nullable;
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
}
