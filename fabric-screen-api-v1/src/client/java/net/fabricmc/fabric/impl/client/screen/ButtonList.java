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

package net.fabricmc.fabric.impl.client.screen;

import java.util.AbstractList;
import java.util.List;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;

// TODO: When events for listening to addition of child elements are added, fire events from this list.
public final class ButtonList extends AbstractList<AbstractWidget> {
	private final List<Renderable> renderables;
	private final List<NarratableEntry> narratables;
	private final List<GuiEventListener> children;

	public ButtonList(List<Renderable> renderables, List<NarratableEntry> narratables, List<GuiEventListener> children) {
		this.renderables = renderables;
		this.narratables = narratables;
		this.children = children;
	}

	@Override
	public AbstractWidget get(int index) {
		final int renderableIndex = translateIndex(renderables, index, false);
		return (AbstractWidget) renderables.get(renderableIndex);
	}

	@Override
	public AbstractWidget set(int index, AbstractWidget element) {
		final int renderableIndex = translateIndex(renderables, index, false);
		renderables.set(renderableIndex, element);

		final int narratableIndex = translateIndex(narratables, index, false);
		narratables.set(narratableIndex, element);

		final int childIndex = translateIndex(children, index, false);
		return (AbstractWidget) children.set(childIndex, element);
	}

	@Override
	public void add(int index, AbstractWidget element) {
		// ensure no duplicates
		final int duplicateIndex = renderables.indexOf(element);

		if (duplicateIndex >= 0) {
			renderables.remove(element);
			narratables.remove(element);
			children.remove(element);

			if (duplicateIndex <= translateIndex(renderables, index, true)) {
				index--;
			}
		}

		final int renderableIndx = translateIndex(renderables, index, true);
		renderables.add(renderableIndx, element);

		final int narratableIndex = translateIndex(narratables, index, true);
		narratables.add(narratableIndex, element);

		final int childIndex = translateIndex(children, index, true);
		children.add(childIndex, element);
	}

	@Override
	public AbstractWidget remove(int index) {
		index = translateIndex(renderables, index, false);

		final AbstractWidget removedButton = (AbstractWidget) renderables.remove(index);
		this.narratables.remove(removedButton);
		this.children.remove(removedButton);

		return removedButton;
	}

	@Override
	public int size() {
		int ret = 0;

		for (Renderable renderable : renderables) {
			if (renderable instanceof AbstractWidget) {
				ret++;
			}
		}

		return ret;
	}

	private int translateIndex(List<?> list, int index, boolean allowAfter) {
		int remaining = index;

		for (int i = 0, max = list.size(); i < max; i++) {
			if (list.get(i) instanceof AbstractWidget) {
				if (remaining == 0) {
					return i;
				}

				remaining--;
			}
		}

		if (allowAfter && remaining == 0) {
			return list.size();
		}

		throw new IndexOutOfBoundsException(String.format("Index: %d, Size: %d", index, index - remaining));
	}
}
