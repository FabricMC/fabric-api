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

import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.WrapperBakedModel;

/**
 * An interface to be implemented by models that wrap and replace another model, such as {@link WrapperBakedModel}.
 * This allows mods to access the wrapped model without having to know the exact type of the wrapper model.
 *
 * <p>If you need to access data stored in one of your {@link BakedModel} subclasses,
 * and you would normally access the model by its identifier and then cast it:
 * call {@link #unwrap(BakedModel, Predicate)} on the model first, in case another
 * mod is wrapping your model to alter its rendering.
 */
public interface UnwrappableBakedModel {
	/**
	 * Return the wrapped model, if there is one at the moment, or {@code null} otherwise.
	 *
	 * <p>If there are multiple layers of wrapping, this method does not necessarily return the innermost model.
	 */
	@Nullable
	BakedModel getWrappedModel();

	/**
	 * Iteratively unwrap the given model until the given condition returns true or all models in the hierarchy have
	 * been tested. If no model passes the condition, null is returned.
	 *
	 * <p>A good use of this method is to safely cast a model to an expected type, by passing
	 * {@code model -> model instanceof MyCustomBakedModel} as the condition.
	 */
	@Nullable
	static BakedModel unwrap(BakedModel model, Predicate<BakedModel> condition) {
		while (!condition.test(model)) {
			if (model instanceof UnwrappableBakedModel wrapper) {
				BakedModel wrapped = wrapper.getWrappedModel();

				if (wrapped == null) {
					return null;
				} else if (wrapped == model) {
					throw new IllegalArgumentException("Model " + model + " is wrapping itself!");
				} else {
					model = wrapped;
				}
			} else {
				return null;
			}
		}

		return model;
	}

	/**
	 * Fully unwrap a model, i.e. return the innermost model.
	 */
	static BakedModel unwrap(BakedModel model) {
		while (model instanceof UnwrappableBakedModel wrapper) {
			BakedModel wrapped = wrapper.getWrappedModel();

			if (wrapped == null) {
				return model;
			} else if (wrapped == model) {
				throw new IllegalArgumentException("Model " + model + " is wrapping itself!");
			} else {
				model = wrapped;
			}
		}

		return model;
	}
}
