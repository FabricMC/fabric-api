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
import net.minecraft.client.gui.components.Updatable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;

// TODO: When events for listening to addition of child elements are added, fire events from this list.
public final class ButtonList extends AbstractList<AbstractWidget> {
	private final List<Renderable> renderables;
	private final List<NarratableEntry> narratables;
	private final List<GuiEventListener> children;
	private final List<Updatable> updatables;

	public ButtonList(List<Renderable> renderables, List<NarratableEntry> narratables, List<GuiEventListener> children, List<Updatable> updatables) {
		this.renderables = renderables;
		this.narratables = narratables;
		this.children = children;
		this.updatables = updatables;
	}

	@Override
	public AbstractWidget get(int index) {
		int remaining = index;

		for (Renderable renderable : renderables) {
			if (renderable instanceof AbstractWidget widget) {
				if (remaining == 0) {
					return widget;
				}

				remaining--;
			}
		}

		throw new IndexOutOfBoundsException(String.format("Index: %d, Size: %d", index, size()));
	}

	@Override
	public AbstractWidget set(int index, AbstractWidget element) {
		AbstractWidget existing = get(index);
		renderables.set(renderables.indexOf(existing), element);

		int narrationIndex = narratables.indexOf(existing);

		if (narrationIndex >= 0) {
			narratables.set(narrationIndex, element);
		}

		int childIndex = children.indexOf(existing);

		if (childIndex < 0) {
			return existing;
		}

		children.set(childIndex, element);

		if (existing instanceof Updatable oldUpdatable) {
			updatables.remove(oldUpdatable);
		}

		if (element instanceof Updatable newUpdatable) {
			updatables.add(newUpdatable);
		}

		return existing;
	}

	@Override
	public void add(int index, AbstractWidget element) {
		// Remove any existing occurrence and adjust the target index accordingly.
		int duplicateIndex = listIndexOf(element);

		if (duplicateIndex >= 0) {
			renderables.remove(element);

			if (duplicateIndex < index) {
				index--;
			}
		}

		if (index > size()) {
			throw new IndexOutOfBoundsException(String.format("Index: %d, Size: %d", index, size()));
		}

		narratables.remove(element);
		children.remove(element);
		updatables.remove(element);

		if (index == size()) {
			renderables.add(element);
			narratables.add(element);
			children.add(element);
		} else {
			// Use an anchor widget and insert before it.
			AbstractWidget anchor = get(index);

			int i = renderables.indexOf(anchor);
			renderables.add(i >= 0 ? i : renderables.size(), element);

			i = narratables.indexOf(anchor);
			narratables.add(i >= 0 ? i : narratables.size(), element);

			i = children.indexOf(anchor);
			children.add(i >= 0 ? i : children.size(), element);
		}

		if (element instanceof Updatable updatable) {
			updatables.add(updatable);
		}
	}

	private int listIndexOf(AbstractWidget element) {
		int index = 0;

		for (Renderable renderable : renderables) {
			if (renderable instanceof AbstractWidget widget) {
				if (widget == element) {
					return index;
				}

				index++;
			}
		}

		return -1;
	}

	@Override
	public AbstractWidget remove(int index) {
		AbstractWidget removedButton = get(index);

		renderables.remove(removedButton);
		narratables.remove(removedButton);
		children.remove(removedButton);
		updatables.remove(removedButton);

		return removedButton;
	}

	@Override
	public int size() {
		int size = 0;

		for (Renderable renderable : renderables) {
			if (renderable instanceof AbstractWidget) {
				size++;
			}
		}

		return size;
	}
}
