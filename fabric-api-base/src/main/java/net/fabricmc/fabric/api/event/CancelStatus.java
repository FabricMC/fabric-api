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

import org.jetbrains.annotations.ApiStatus;

/**
 * Wrapper class used solely for storing the status of an {@link Event}'s cancellation.
 */
public final class CancelStatus {
	private boolean cancelled = false;

	/**
	 * Sets {@link #cancelled} to {@code true} and marks this instance as cancelled.
	 */
	public void cancel() {
		this.cancelled = true;
	}

	/**
	 * Sets {@link #cancelled} to {@code false} and "resets" this instance back to its original state.
	 *
	 * @implNote When creating an event, make sure to <b>always</b> call this method at the head/end/before return of the invoker factory.
	 * This prevents the event being falsely marked as cancelled when the functional method is called again.
	 * Example:
	 *
	 * <blockquote><pre>
	 * &#64;FunctionalInterface
	 * interface Test {
	 *     Event&lt;Test&gt; EVENT = EventFactory.createCancellable(Test.class,
	 *     	cancelStatus -> listeners -> () -> {
	 *     	    cancelStatus.reset() //&lt;--HERE
	 *
	 *     	    for (Test test : listeners) {
	 *     	        if (cancelStatus.isCancelled()) {
	 *     	            return;
	 *     	        }
	 *
	 *     	        test.onTest();
	 *     	    }
	 *
	 *     	    //cancelStatus.reset() &lt;-- OR HERE
	 *     	}
	 *     )
	 *
	 *     void onTest();
	 * }
	 * </pre></blockquote>
	 */
	@ApiStatus.Internal
	public void reset() {
		this.cancelled = false;
	}

	/**
	 * Allows you to retrieve the current status of an {@link Event}'s cancellation.
	 * @return {@code true} when the event is cancelled, {@code false} otherwise
	 */
	public boolean isCancelled() {
		return this.cancelled;
	}
}
