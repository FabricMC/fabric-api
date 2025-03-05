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

package net.fabricmc.fabric.api.client.model.loading.v1;

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.block.BlockState;
import net.minecraft.client.render.item.model.ItemModel;
import net.minecraft.client.render.model.Baker;
import net.minecraft.client.render.model.BlockStateModel;
import net.minecraft.client.render.model.ResolvableModel;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.event.Event;

/**
 * Contains interfaces for the events that can be used to modify models at different points in the loading and baking
 * process.
 *
 * <p>Example use cases:
 * <ul>
 *     <li>Overriding the model for a particular block state - check if the given block state matches the desired block
 *     state. If so, return your desired model, otherwise return the given model.</li>
 *     <li>Wrapping a model to override certain behaviors - simply return a new model instance and delegate calls
 *     to the original model as needed.</li>
 * </ul>
 *
 * <p>Phases are used to ensure that modifications occur in a reasonable order, e.g. wrapping occurs after overrides,
 * and separate phases are provided for mods that wrap their own models and mods that need to wrap models of other mods
 * or wrap models arbitrarily.
 *
 * <p>Any event may be invoked concurrently with other invocations of the same event or other events, subject to
 * reasonable constraints. For example, a block/item model and its dependencies must be loaded before the block/item
 * model is baked.
 *
 * <p>These callbacks are invoked for <b>every single model that is loaded or baked</b>, so implementations should be
 * as efficient as possible.
 */
public final class ModelModifier {
	/**
	 * Recommended phase to use when overriding models, e.g. replacing a model with another model.
	 */
	public static final Identifier OVERRIDE_PHASE = Identifier.of("fabric", "override");
	/**
	 * Recommended phase to use for transformations that need to happen before wrapping, but after model overrides.
	 */
	public static final Identifier DEFAULT_PHASE = Event.DEFAULT_PHASE;
	/**
	 * Recommended phase to use when wrapping models.
	 */
	public static final Identifier WRAP_PHASE = Identifier.of("fabric", "wrap");
	/**
	 * Recommended phase to use when wrapping models with transformations that want to happen last,
	 * e.g. for connected textures or other similar visual effects that should be the final processing step.
	 */
	public static final Identifier WRAP_LAST_PHASE = Identifier.of("fabric", "wrap_last");

	@FunctionalInterface
	public interface OnLoad {
		/**
		 * This handler is invoked to allow modification of an unbaked model right after it is first loaded.
		 *
		 * <p>For further information, see the docs of {@link ModelLoadingPlugin.Context#modifyModelOnLoad()}.
		 *
		 * @param model the current unbaked model instance
		 * @param context context with additional information about the model/loader
		 * @return the model that should be used in this scenario. If no changes are needed, just return {@code model} as-is.
		 * @see ModelLoadingPlugin.Context#modifyModelOnLoad
		 */
		UnbakedModel modifyModelOnLoad(UnbakedModel model, Context context);

		/**
		 * The context for an on load model modification event.
		 */
		@ApiStatus.NonExtendable
		interface Context {
			/**
			 * The identifier of the model that was loaded.
			 */
			Identifier id();
		}
	}

	@FunctionalInterface
	public interface OnLoadBlock {
		/**
		 * This handler is invoked to allow modification of an unbaked block model right after it is first loaded.
		 *
		 * @param model the current unbaked model instance
		 * @param context context with additional information about the model/loader
		 * @return the model that should be used in this scenario. If no changes are needed, just return {@code model} as-is.
		 * @see ModelLoadingPlugin.Context#modifyBlockModelOnLoad
		 */
		BlockStateModel.UnbakedGrouped modifyModelOnLoad(BlockStateModel.UnbakedGrouped model, Context context);

		/**
		 * The context for an on load block model modification event.
		 */
		@ApiStatus.NonExtendable
		interface Context {
			/**
			 * The corresponding block state of the model that was loaded.
			 */
			BlockState state();
		}
	}

	@FunctionalInterface
	public interface BeforeBakeBlock {
		/**
		 * This handler is invoked to allow modification of the unbaked block model instance right before it is baked.
		 *
		 * @param model the current unbaked model instance
		 * @param context context with additional information about the model/loader
		 * @return the model that should be used in this scenario. If no changes are needed, just return {@code model} as-is.
		 * @see ModelLoadingPlugin.Context#modifyBlockModelBeforeBake
		 */
		BlockStateModel.UnbakedGrouped modifyModelBeforeBake(BlockStateModel.UnbakedGrouped model, Context context);

		/**
		 * The context for a before bake block model modification event.
		 */
		@ApiStatus.NonExtendable
		interface Context {
			/**
			 * The corresponding block state of the model being baked.
			 */
			BlockState state();

			/**
			 * The baker being used to bake this model. It can be used to
			 * {@linkplain Baker#getModel get resolved models} and {@linkplain Baker#getSpriteGetter get sprites}. Note
			 * that retrieving a model which was not previously
			 * {@linkplain ResolvableModel.Resolver#markDependency discovered} will log a warning and return the missing
			 * model.
			 */
			Baker baker();
		}
	}

	@FunctionalInterface
	public interface AfterBakeBlock {
		/**
		 * This handler is invoked to allow modification of the baked block model instance right after it is baked.
		 *
		 * @param model the current baked model instance
		 * @param context context with additional information about the model/loader
		 * @return the model that should be used in this scenario. If no changes are needed, just return {@code model} as-is.
		 * @see ModelLoadingPlugin.Context#modifyBlockModelAfterBake
		 */
		BlockStateModel modifyModelAfterBake(BlockStateModel model, Context context);

		/**
		 * The context for an after bake block model modification event.
		 */
		@ApiStatus.NonExtendable
		interface Context {
			/**
			 * The corresponding block state of the model being baked.
			 */
			BlockState state();

			/**
			 * The unbaked model that is being baked.
			 */
			BlockStateModel.UnbakedGrouped sourceModel();

			/**
			 * The baker being used to bake this model. It can be used to
			 * {@linkplain Baker#getModel get resolved models} and {@linkplain Baker#getSpriteGetter get sprites}. Note
			 * that retrieving a model which was not previously
			 * {@linkplain ResolvableModel.Resolver#markDependency discovered} will log a warning and return the missing
			 * model.
			 */
			Baker baker();
		}
	}

	@FunctionalInterface
	public interface BeforeBakeItem {
		/**
		 * This handler is invoked to allow modification of the unbaked item model instance right before it is baked.
		 *
		 * @param model the current unbaked model instance
		 * @param context context with additional information about the model/loader
		 * @return the model that should be used in this scenario. If no changes are needed, just return {@code model} as-is.
		 * @see ModelLoadingPlugin.Context#modifyItemModelBeforeBake
		 */
		ItemModel.Unbaked modifyModelBeforeBake(ItemModel.Unbaked model, Context context);

		/**
		 * The context for a before bake item model modification event.
		 */
		@ApiStatus.NonExtendable
		interface Context {
			/**
			 * The corresponding item ID of the model being baked.
			 */
			Identifier itemId();

			/**
			 * The vanilla context being used to bake this model.
			 */
			ItemModel.BakeContext bakeContext();
		}
	}

	@FunctionalInterface
	public interface AfterBakeItem {
		/**
		 * This handler is invoked to allow modification of the baked item model instance right after it is baked.
		 *
		 * @param model the current baked model instance
		 * @param context context with additional information about the model/loader
		 * @return the model that should be used in this scenario. If no changes are needed, just return {@code model} as-is.
		 * @see ModelLoadingPlugin.Context#modifyItemModelAfterBake
		 */
		ItemModel modifyModelAfterBake(ItemModel model, Context context);

		/**
		 * The context for an after bake item model modification event.
		 */
		@ApiStatus.NonExtendable
		interface Context {
			/**
			 * The corresponding item ID of the model being baked.
			 */
			Identifier itemId();

			/**
			 * The unbaked model that is being baked.
			 */
			ItemModel.Unbaked sourceModel();

			/**
			 * The vanilla context being used to bake this model.
			 */
			ItemModel.BakeContext bakeContext();
		}
	}

	private ModelModifier() { }
}
