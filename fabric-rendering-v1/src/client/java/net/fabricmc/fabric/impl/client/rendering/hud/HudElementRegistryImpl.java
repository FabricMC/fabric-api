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

package net.fabricmc.fabric.impl.client.rendering.hud;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.SequencedMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.jetbrains.annotations.VisibleForTesting;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.fabricmc.fabric.api.client.rendering.v1.hud.IdentifiedElement;

public class HudElementRegistryImpl {
	private static final List<Identifier> VANILLA_ELEMENT_IDS = List.of(
			IdentifiedElement.MISC_OVERLAYS,
			IdentifiedElement.CROSSHAIR,
			IdentifiedElement.HOTBAR_AND_BARS,
			IdentifiedElement.STATUS_EFFECTS,
			IdentifiedElement.BOSS_BAR,
			IdentifiedElement.SLEEP,
			IdentifiedElement.DEMO_TIMER,
			IdentifiedElement.DEBUG,
			IdentifiedElement.SCOREBOARD,
			IdentifiedElement.OVERLAY_MESSAGE,
			IdentifiedElement.TITLE_AND_SUBTITLE,
			IdentifiedElement.CHAT,
			IdentifiedElement.PLAYER_LIST,
			IdentifiedElement.SUBTITLES
	);
	/**
	 * A map containing vanilla elements.
	 * This map should not be modified. Modify {@link VanillaElement#elements()} instead.
	 */
	@VisibleForTesting
	static final SequencedMap<Identifier, VanillaElement> vanillaElements = VANILLA_ELEMENT_IDS.stream().map(VanillaElement::new).collect(Collectors.toMap(VanillaElement::id, Function.identity(), (a, b) -> a, LinkedHashMap::new));

	public static VanillaElement getVanillaLayer(Identifier id) {
		return vanillaElements.get(id);
	}

	public static void addFirst(IdentifiedElement element) {
		validateUnique(element);
		vanillaElements.firstEntry().getValue().elements().addFirst(element);
	}

	public static void addLast(IdentifiedElement element) {
		validateUnique(element);
		vanillaElements.lastEntry().getValue().elements().addLast(element);
	}

	public static void attachElementBefore(Identifier beforeThis, IdentifiedElement element) {
		validateUnique(element);

		boolean didChange = findLayer(beforeThis, (l, iterator) -> {
			iterator.previous();
			iterator.add(element);
			iterator.next();
			return true;
		});

		if (!didChange) {
			throw new IllegalArgumentException("Layer with identifier " + beforeThis + " not found");
		}
	}

	public static void attachElementAfter(Identifier afterThis, IdentifiedElement element) {
		validateUnique(element);

		boolean didChange = findLayer(afterThis, (l, iterator) -> {
			iterator.add(element);
			return true;
		});

		if (!didChange) {
			throw new IllegalArgumentException("Layer with identifier " + afterThis + " not found");
		}
	}

	public static void removeElement(Identifier identifier) {
		boolean didChange = findLayer(identifier, (l, iterator) -> {
			iterator.remove();
			return true;
		});

		if (!didChange) {
			throw new IllegalArgumentException("Layer with identifier " + identifier + " not found");
		}
	}

	public static void replaceElement(Identifier identifier, Function<IdentifiedElement, IdentifiedElement> replacer) {
		boolean didChange = findLayer(identifier, (l, iterator) -> {
			iterator.set(replacer.apply((IdentifiedElement) l));
			return true;
		});

		if (!didChange) {
			throw new IllegalArgumentException("Layer with identifier " + identifier + " not found");
		}
	}

	@VisibleForTesting
	static void validateUnique(IdentifiedElement layer) {
		visitLayers((l, iterator) -> {
			if (matchesIdentifier(l, layer.id())) {
				throw new IllegalArgumentException("Layer with identifier " + layer.id() + " already exists");
			}

			return false;
		});
	}

	/**
	 * @return true if an element with the given identifier was found
	 */
	@VisibleForTesting
	static boolean findLayer(Identifier identifier, ElementVisitor visitor) {
		MutableBoolean found = new MutableBoolean(false);

		visitLayers((l, iterator) -> {
			if (matchesIdentifier(l, identifier)) {
				found.setTrue();
				return visitor.visit(l, iterator);
			}

			return false;
		});

		return found.booleanValue();
	}

	@VisibleForTesting
	static boolean visitLayers(ElementVisitor visitor) {
		boolean modified = false;

		for (VanillaElement vanillaElement : vanillaElements.sequencedValues()) {
			modified |= visitLayers(vanillaElement.elements(), visitor);
		}

		return modified;
	}

	private static boolean visitLayers(List<HudElement> layers, ElementVisitor visitor) {
		MutableBoolean modified = new MutableBoolean(false);
		ListIterator<HudElement> iterator = layers.listIterator();

		while (iterator.hasNext()) {
			HudElement element = iterator.next();

			if (visitor.visit(element, iterator)) {
				modified.setTrue();
			}
		}

		return modified.booleanValue();
	}

	private static boolean matchesIdentifier(HudElement element, Identifier identifier) {
		return element instanceof IdentifiedElement ie && ie.id().equals(identifier);
	}

	@VisibleForTesting
	interface ElementVisitor {
		/**
		 * @return true if the list has been modified, false if not modified
		 */
		boolean visit(HudElement element, ListIterator<HudElement> iterator);
	}

	/**
	 * An element that wraps a vanilla element using a list, allowing for users to attach elements before or after it, replace it, or remove it.
	 */
	@VisibleForTesting
	public record VanillaElement(Identifier id, List<HudElement> elements) {
		public VanillaElement(Identifier id) {
			this(id, new ArrayList<>());
			elements().add(IdentifiedElement.of(id, (context, tickCounter) -> { }));
		}

		public void render(InGameHud instance, DrawContext context, RenderTickCounter tickCounter, Operation<Void> renderVanilla) {
			for (HudElement element : elements) {
				if (matchesIdentifier(element, id())) {
					renderVanilla.call(instance, context, tickCounter);
				} else {
					element.render(context, tickCounter);
				}
			}
		}
	}
}
