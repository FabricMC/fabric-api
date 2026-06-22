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

import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.widget.ClickableWidget;

// TODO: When events for listening to addition of child elements are added, fire events from this list.
public final class ButtonList extends AbstractList<ClickableWidget> {
	private final List<Drawable> drawables;
	private final List<Selectable> selectables;
	private final List<Element> children;

	public ButtonList(List<Drawable> drawables, List<Selectable> selectables, List<Element> children) {
		this.drawables = drawables;
		this.selectables = selectables;
		this.children = children;
	}

	@Override
	public ClickableWidget get(int index) {
		int remaining = index;

		for (Drawable renderable : drawables) {
			if (renderable instanceof ClickableWidget widget) {
				if (remaining == 0) {
					return widget;
				}

				remaining--;
			}
		}

		throw new IndexOutOfBoundsException(String.format("Index: %d, Size: %d", index, size()));
	}

	@Override
	public ClickableWidget set(int index, ClickableWidget element) {
		ClickableWidget existing = get(index);

		int i = drawables.indexOf(existing);
		if (i >= 0) drawables.set(i, element);

		i = selectables.indexOf(existing);
		if (i >= 0) selectables.set(i, element);

		i = children.indexOf(existing);
		if (i >= 0) children.set(i, element);

		return existing;
	}

	@Override
	public void add(int index, ClickableWidget element) {
		// Remove any existing occurrence and adjust the target index accordingly.
		int duplicateIndex = listIndexOf(element);

		if (duplicateIndex >= 0) {
			drawables.remove(element);
			selectables.remove(element);
			children.remove(element);

			if (duplicateIndex < index) {
				index--;
			}
		}

		if (index > size()) {
			throw new IndexOutOfBoundsException(String.format("Index: %d, Size: %d", index, size()));
		} else if (index == size()) {
			drawables.add(element);
			selectables.add(element);
			children.add(element);
		} else {
			// Use an anchor widget and insert before it.
			ClickableWidget anchor = get(index);

			int i = drawables.indexOf(anchor);
			drawables.add(i >= 0 ? i : drawables.size(), element);

			i = selectables.indexOf(anchor);
			selectables.add(i >= 0 ? i : selectables.size(), element);

			i = children.indexOf(anchor);
			children.add(i >= 0 ? i : children.size(), element);
		}
	}

	private int listIndexOf(ClickableWidget element) {
		int index = 0;

		for (Drawable renderable : drawables) {
			if (renderable instanceof ClickableWidget widget) {
				if (widget == element) {
					return index;
				}

				index++;
			}
		}

		return -1;
	}

	@Override
	public ClickableWidget remove(int index) {
		ClickableWidget removedButton = get(index);

		drawables.remove(removedButton);
		selectables.remove(removedButton);
		children.remove(removedButton);

		return removedButton;
	}

	@Override
	public int size() {
		int size = 0;

		for (Drawable renderable : drawables) {
			if (renderable instanceof ClickableWidget) {
				size++;
			}
		}

		return size;
	}
}
