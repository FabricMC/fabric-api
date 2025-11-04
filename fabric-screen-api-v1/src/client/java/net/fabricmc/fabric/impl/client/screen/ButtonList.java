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
	private final List<Renderable> drawables;
	private final List<NarratableEntry> selectables;
	private final List<GuiEventListener> children;

	public ButtonList(List<Renderable> drawables, List<NarratableEntry> selectables, List<GuiEventListener> children) {
		this.drawables = drawables;
		this.selectables = selectables;
		this.children = children;
	}

	@Override
	public AbstractWidget get(int index) {
		final int drawableIndex = translateIndex(drawables, index, false);
		return (AbstractWidget) drawables.get(drawableIndex);
	}

	@Override
	public AbstractWidget set(int index, AbstractWidget element) {
		final int drawableIndex = translateIndex(drawables, index, false);
		drawables.set(drawableIndex, element);

		final int selectableIndex = translateIndex(selectables, index, false);
		selectables.set(selectableIndex, element);

		final int childIndex = translateIndex(children, index, false);
		return (AbstractWidget) children.set(childIndex, element);
	}

	@Override
	public void add(int index, AbstractWidget element) {
		// ensure no duplicates
		final int duplicateIndex = drawables.indexOf(element);

		if (duplicateIndex >= 0) {
			drawables.remove(element);
			selectables.remove(element);
			children.remove(element);

			if (duplicateIndex <= translateIndex(drawables, index, true)) {
				index--;
			}
		}

		final int drawableIndex = translateIndex(drawables, index, true);
		drawables.add(drawableIndex, element);

		final int selectableIndex = translateIndex(selectables, index, true);
		selectables.add(selectableIndex, element);

		final int childIndex = translateIndex(children, index, true);
		children.add(childIndex, element);
	}

	@Override
	public AbstractWidget remove(int index) {
		index = translateIndex(drawables, index, false);

		final AbstractWidget removedButton = (AbstractWidget) drawables.remove(index);
		this.selectables.remove(removedButton);
		this.children.remove(removedButton);

		return removedButton;
	}

	@Override
	public int size() {
		int ret = 0;

		for (Renderable drawable : drawables) {
			if (drawable instanceof AbstractWidget) {
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
