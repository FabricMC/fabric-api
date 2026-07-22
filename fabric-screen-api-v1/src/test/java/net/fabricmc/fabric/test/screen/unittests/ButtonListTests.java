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

package net.fabricmc.fabric.test.screen.unittests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;

import net.fabricmc.fabric.api.client.screen.v1.Screens;

public class ButtonListTests {
	@Test
	public void testSize() {
		List<AbstractWidget> widgets = Screens.getWidgets(screen());
		assertEquals(7, widgets.size());
	}

	@Test
	public void testAdd() {
		List<AbstractWidget> widgets = Screens.getWidgets(screen());
		int size = widgets.size();
		Button button = button();
		widgets.add(button);
		assertEquals(size, widgets.indexOf(button));
		assertEquals(size + 1, widgets.size());
	}

	@Test
	public void testAddBeforeRenderable() {
		List<AbstractWidget> widgets = Screens.getWidgets(screen());
		int size = widgets.size();
		Button button = button();
		widgets.add(0, button);
		assertEquals(0, widgets.indexOf(button));
		assertEquals(size + 1, widgets.size());
	}

	@Test
	public void testAddAtRenderable() {
		List<AbstractWidget> widgets = Screens.getWidgets(screen());
		int size = widgets.size();
		Button button = button();
		widgets.add(1, button);
		assertEquals(1, widgets.indexOf(button));
		assertEquals(size + 1, widgets.size());
	}

	@Test
	public void testAddAfterRenderable() {
		List<AbstractWidget> widgets = Screens.getWidgets(screen());
		int size = widgets.size();
		Button button = button();
		widgets.add(2, button);
		assertEquals(2, widgets.indexOf(button));
		assertEquals(size + 1, widgets.size());
	}

	@Test
	public void testRemove() {
		List<AbstractWidget> widgets = Screens.getWidgets(screen());
		int size = widgets.size();
		Button button = button();
		widgets.add(button);
		assertEquals(size, widgets.indexOf(button));
		assertEquals(size + 1, widgets.size());
		widgets.remove(button);
		assertEquals(size, widgets.size());
	}

	@Test
	public void testRemoveBeforeRenderable() {
		List<AbstractWidget> widgets = Screens.getWidgets(screen());
		int size = widgets.size();
		Button button = button();
		widgets.add(0, button);
		assertEquals(0, widgets.indexOf(button));
		assertEquals(size + 1, widgets.size());
		widgets.remove(button);
		assertEquals(-1, widgets.indexOf(button));
		assertEquals(size, widgets.size());
	}

	@Test
	public void testRemoveAtRenderable() {
		List<AbstractWidget> widgets = Screens.getWidgets(screen());
		int size = widgets.size();
		Button button = button();
		widgets.add(1, button);
		assertEquals(1, widgets.indexOf(button));
		assertEquals(size + 1, widgets.size());
		widgets.remove(button);
		assertEquals(-1, widgets.indexOf(button));
		assertEquals(size, widgets.size());
	}

	@Test
	public void testRemoveAfterRenderable() {
		List<AbstractWidget> widgets = Screens.getWidgets(screen());
		int size = widgets.size();
		Button button = button();
		widgets.add(2, button);
		assertEquals(2, widgets.indexOf(button));
		assertEquals(size + 1, widgets.size());
		widgets.remove(button);
		assertEquals(-1, widgets.indexOf(button));
		assertEquals(size, widgets.size());
	}

	@Test
	public void testRemoveIndex() {
		List<AbstractWidget> widgets = Screens.getWidgets(screen());
		int size = widgets.size();
		Button button = button();
		widgets.add(size, button);
		assertEquals(size, widgets.indexOf(button));
		assertEquals(size + 1, widgets.size());
		widgets.remove(size);
		assertEquals(size, widgets.size());
	}

	@Test
	public void testRemoveIndexBeforeRenderable() {
		List<AbstractWidget> widgets = Screens.getWidgets(screen());
		int size = widgets.size();
		Button button = button();
		widgets.add(0, button);
		assertEquals(0, widgets.indexOf(button));
		assertEquals(size + 1, widgets.size());
		widgets.remove(0);
		assertEquals(-1, widgets.indexOf(button));
		assertEquals(size, widgets.size());
	}

	@Test
	public void testRemoveIndexAtRenderable() {
		List<AbstractWidget> widgets = Screens.getWidgets(screen());
		int size = widgets.size();
		Button button = button();
		widgets.add(1, button);
		assertEquals(1, widgets.indexOf(button));
		assertEquals(size + 1, widgets.size());
		widgets.remove(1);
		assertEquals(-1, widgets.indexOf(button));
		assertEquals(size, widgets.size());
	}

	@Test
	public void testRemoveIndexAfterRenderable() {
		List<AbstractWidget> widgets = Screens.getWidgets(screen());
		int size = widgets.size();
		Button button = button();
		widgets.add(2, button);
		assertEquals(2, widgets.indexOf(button));
		assertEquals(size + 1, widgets.size());
		widgets.remove(2);
		assertEquals(-1, widgets.indexOf(button));
		assertEquals(size, widgets.size());
	}

	private static Screen screen() {
		// There must be more Button instances added via Screen::addRenderableOnly than via Screen::addWidget to properly test reliance on the backing Screen#renderables list.
		return new Screen(null, null, CommonComponents.EMPTY) {
			{
				// Present in renderables: true, present in children: true, present in ButtonList: true
				this.addRenderableWidget(button());
				// Present in renderables: true, present in children: false, present in ButtonList: true
				this.addRenderableOnly(button());
				// Present in renderables: true, present in children: false, present in ButtonList: false (not an AbstractWidget)
				this.addRenderableOnly((graphics, mouseX, mouseY, a) -> {
					// NO-OP
				});
				// Present in renderables: false, present in children: true, present in ButtonList: false
				this.addWidget(button());
				// Present in renderables: false, present in children: true, present in ButtonList: false
				this.addWidget(button());
				// Present in renderables: true, present in children: true, present in ButtonList: true
				this.addRenderableWidget(button());
				// Present in renderables: true, present in children: false, present in ButtonList: true
				this.addRenderableOnly(button());
				// Present in renderables: true, present in children: true, present in ButtonList: true
				this.addRenderableWidget(button());
				// Present in renderables: true, present in children: true, present in ButtonList: true
				this.addRenderableWidget(button());
				// Present in renderables: true, present in children: false, present in ButtonList: true
				this.addRenderableOnly(button());
			}
		};
	}

	private static Button button() {
		return Button.builder(CommonComponents.EMPTY, _ -> {
		}).build();
	}
}
