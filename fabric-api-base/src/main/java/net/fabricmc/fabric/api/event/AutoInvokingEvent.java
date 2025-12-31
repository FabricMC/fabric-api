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

package net.fabricmc.fabric.api.event;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/// Indicates that this [Event] is auto-invoking:
/// it calls the event callback implemented by a context parameter type automatically and without registration.
///
/// This means that this event can be listened to in two ways:
///
///   - If the consumer is the context parameter and it implements the callback, it will be automatically invoked, don't register manually.
///   - Otherwise, there is no invocation and the listener needs manual registration as usual.
///
///
/// Do note that there may be more than one context parameter.
///
/// A typical use case is feature augmentation, for example to expose raw clicks to slots.
/// The event callback has a slot parameter - the context parameter - and the event itself is carrying this annotation.
/// All the slot needs to receive slot clicks is to implement `SlotClickCallback` on itself.
/// It shouldn't do any explicit event registration like `SLOT_CLICK_EVENT.register(this::onSlotClick)`,
/// otherwise it will see extraneous callback invocations.
///
/// In general, an auto-invoking event bridges the gap between the flexibility of an event with global reach,
/// and the convenience of implementing an interface that gets detected automatically.
///
/// This is a documentation-only annotation, the event factory has to implement the functionality explicitly by checking the parameter type and invoking it.
/// On top of adding this annotation, the event field or method should document which parameters are context parameters,
/// and under which circumstances they are invoked.
// TODO: explore enforcing that auto-invoked listeners don't register themselves.
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.METHOD })
public @interface AutoInvokingEvent {
}
