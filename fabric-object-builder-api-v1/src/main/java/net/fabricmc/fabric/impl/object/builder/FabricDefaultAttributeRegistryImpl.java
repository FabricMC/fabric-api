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

package net.fabricmc.fabric.impl.object.builder;

import java.util.function.Predicate;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.mixin.object.builder.AttributeSupplierAccessor;
import net.fabricmc.fabric.mixin.object.builder.AttributeSupplierBuilderAccessor;
import net.fabricmc.fabric.mixin.object.builder.DefaultAttributesAccessor;

public final class FabricDefaultAttributeRegistryImpl {
	// a single bulk modification is applied when the registries are frozen since at that point
	// everything should be registered
	public static void invokeModify() {
		FabricDefaultAttributeRegistry.MODIFY.invoker().modify(new ModifyContextImpl());
	}

	private static AttributeSupplier.Builder createFromExistingSupplier(AttributeSupplier supplier) {
		AttributeSupplier.Builder builder = AttributeSupplier.builder();
		((AttributeSupplierBuilderAccessor) builder).getBuilder().putAll(((AttributeSupplierAccessor) supplier).getInstances());
		return builder;
	}

	static class ModifyContextImpl implements FabricDefaultAttributeRegistry.ModifyContext {
		@Override
		public void modify(Predicate<EntityType<? extends LivingEntity>> entityTypePredicate, FabricDefaultAttributeRegistry.ModifyConsumer consumer) {
			DefaultAttributesAccessor.getRegistry().forEach((type, supplier) -> {
				if (entityTypePredicate.test(type)) {
					AttributeSupplier.Builder builder = createFromExistingSupplier(supplier);
					consumer.accept(type, builder);
					DefaultAttributesAccessor.getRegistry().put(type, builder.build());
				}
			});
		}
	}

	private FabricDefaultAttributeRegistryImpl() {
	}
}
