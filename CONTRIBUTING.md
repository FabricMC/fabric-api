# Fabric API development guidelines

This document describes the development guidelines for Fabric API. It may be amended at any time. Therefore you should refer to the development guidelines when working on any contributions.

Following these guidelines should ensure your contributions to Fabric API are quick to review, consistent with other code in Fabric API and well thought out. This document should not be seen completely as a strict ruleset but instead the thought process that a Fabric team member would consider during the design, implementation and review of contributions to Fabric API.

Old code or parts thereof might not yet be up to the standards defined by these guidelines. When working with old code, try to adhere to these guidelines, but don't bulk update legacy code to match them. The team will handle updating older code to match the newer standards when appropriate.

## Scope

In order to retain maintainability, portability and discoverability, Fabric API only targets features that are important to a broader set of mods. Whether an addition is desirable is a balance between its
- usefulness,
- complexity,
- inherent necessity for compatibility,
- performance impact,
- continued support by the authors or other interested parties.

Fabric API generally is not meant for bugfixes, performance improvements or gameplay changes, and exceptions need careful evaluation.

It is highly recommended that an issue be opened or a message be posted in the official Discord server, to discuss whether a feature is in scope, before it is designed or implemented. Design choices may also warrant prior discussion to avoid wasting time.

## Structure of the guidelines

The rest of this document is split in the following categories:

- [**General design considerations**](#general-design-considerations): Broad guidelines to keep in mind when writing APIs for Fabric.
- [**API conventions**](#api-conventions): Guidelines for common API patterns.
- [**Implementation guidelines**](#implementation-guidelines): Guidelines to keep in mind when writing implementation code.
- [**Documentation**](#documentation): Guidelines for writing documentation. Extensive documentation of the offered features is essential for usability.
- [**Structure of Fabric API**](#structure-of-fabric-api): Organization of code within Fabric API.
- [**Code formatting**](#code-formatting): Specific Java code formatting standards.
- [**Testing**](#testing): Guidelines for writing testmod code. Tests showing that the submitted feature is working should be included in the PR.
- [**Pull Request checklist**](#checklist-before-submitting-a-pull-request): Smaller things to keep in mind when submitting a Pull Request.

## General design considerations

When designing an API addition, the following goals should be kept in mind:
- Simplicity: Additions should be
    - easy to use,
    - inherently hard to misuse or protected against misuse,
    - reasonably self contained,
    - sufficiently capable, yet not excessively loaded with niche features,
    - suitable for simple implementations,
    - not overly abstract.
- Familiarity: Additions should reuse or extend existing designs/patterns in other parts of Fabric API or vanilla.
- Extensibility: Additions should be open for future expansion without needing to deprecate anything.
- Portability: Additions should not be too closely coupled with specific vanilla code. They should
    - avoid exposure of unimportant aspects,
    - project into the future and consider potential future vanilla development,
    - avoid auxiliary libraries,
    - be portable for the sake of simplicity and extensibility.
- Performance: Additions should
    - be fast to initialize and execute,
    - have low or zero allocation rate,
    - have low resident memory use,
    - keep extreme uses in mind (nuke, bulk command execution, spamming etc).

One important consideration when designing an API is spending some time thinking about the API in isolation without any influence from underlying implementation and vanilla code. Considering the API as a single entity rather than a part of a larger implementation, akin a regular user of the API may help find missing coverage, bad return values or parts of the API that could be improved.

Additionally, any feature provided by Fabric API should be fully usable by mods without mixins or reflection. Helper methods may be used to bridge these gaps.

It can be assumed that nothing other than Fabric API itself accesses non-api packages directly, regardless of their visibility. Reflection doesn't have to be accounted for anywhere.

## API conventions
This section covers Fabric API guidelines for common patterns.

### Backwards compatibility
Fabric API makes strong backwards compatibility guarantees, by which contributors must abide. **Modders should not need to update their source code when they update Fabric API**, with the following exceptions:

- APIs might be broken due to **related** changes in Minecraft updates.
    - Preserving backwards compatibility in these cases will be considered if possible.
        - Deprecated code will usually be removed if it requires non-trivial updates.
    - This is limited to code directly affected by the Minecraft version update. Minecraft making breaking changes in some areas of its API is not a reason to break unrelated parts of Fabric API.
- Experimental modules might be broken if necessary.
    - Changes that affect compilation but not the resulting bytecode are allowed in experimental modules, for example some generic changes.
    - Even if an API is marked as experimental, avoid breaking the ecosystem of mods using it.
        - Changes that will cause hard crashes in many mods when they are released should be delayed until the next majorly breaking Minecraft version update.
- There might not be a way around a breaking change.
    - If a major oversight is noticed in an API, it might be broken to address the oversight. Such cases require careful consideration, and should always be discussed with the team beforehand.
    - Remember that deprecating code is always an option.

### Targeted Minecraft versions
- Fabric API does not have strict Minecraft version support policies, but rather supports what is feasible and what the community is interested in.
    - In particular, the latest stable Minecraft version and the latest subsequent snapshot or pre-release version are always supported.
    - Which features go in older stable versions is a tradeoff between how easy it is, how many mods are still using that version, and what the community is willing to contribute.
- New features can be targeted at any supported version.
    - Maintainers will take care of cherry-picking the feature to the other branches when applicable.
    - In doubt, prefer PR'ing to newer stable versions.
- Backporting PRs for older versions will generally be accepted, depending on how many changes were required.

### Discouraged API design patterns

- Avoid the old `Api.INSTANCE` pattern in favor of static methods.
    - This pattern may be used if the backing implementation may be changed by another mod (such as the renderer api). Even then there should be static methods to access the standard api facilities.
- Avoid unnecessary use of generics.
    - Unless Vanilla Minecraft mandates the use of generics, or a good reason exists to use generics.
- Avoid optionals in return values, fields and parameters.
    - Where possible, you should prefer a `@Nullable T`.
    - If vanilla exposes optionals in return types, then returning an optional is fine.
- Avoid requiring the user to cast to a subtype if possible.
    - Adding methods to vanilla types can be done via interface injection.
- Avoid exposing java `record`s as public API.
    - Records expose more than is necessary for most APIs, which makes them difficult to evolve.
    - Prefer to expose an interface that is implemented by an impl record.

### API design patterns to consider

- Transitive Access Wideners (TAWs) should be used to expose access to private or protected members in vanilla classes.
    - Most TAWs should go in the dedicated `fabric-transitive-access-wideners-v1` module.
        - Remember to add the `transitive-` prefix, otherwise dependent mods will not see the access modifications.
        - Some TAWs such as Block subclass constructors are automatically generated. Make sure that you don't edit the `.accesswidener` file in `src` directly. Rather edit the template file (`template.accesswidener`) and run the `gradlew generateAccessWidener` task to update the generated file.
        - Large amounts of TAWs for a specific purpose can be included in another module, as is the case for the data generation API, for example.
    - Do **not** expose TAWs for functions that take a `String` identifier.
        - This makes it too easy to forget the mod ID namespace, so the identifier would often end up in the vanilla `minecraft` namespace.
    - In general, keep the API guidelines in mind when deciding whether something should be a TAW.
- Interface injection (i.e. making a minecraft class or interface extend a Fabric interface) should be considered over separate static helpers.
    - Interface injection requires both a `fabric.mod.json` custom value to make it visible in Minecraft source code, and a mixin to actually implement the interface at runtime.
    - Injected interfaces should have **no abstract methods**.
        - Methods that are guaranteed to be implemented via a mixin to a vanilla class should contain a default body that throws an error.
          Otherwise, the compiler will complain when it can't find the implementation of an interface method on a class.
          For example:
        ```java
        default void injectedMethod() {
            throw new UnsupportedOperationException("Implemented via mixin");
        }
        ```
        - Never use interface injection to add methods that modders must implement. Rather define a subclass or subinterface in Fabric API.
- Builders can be used instead of constructors or factory methods with large amounts of parameters.

### API class modifiers and member visibility

- Classes in Fabric API should be `final` classes unless the class exposed in the API is explicitly meant for extension.
- `private` constructors should be used in API classes unless the class is explicitly meant to be instantiated.
    - This only applies to modder-facing classes, i.e. classes in the `net.fabricmc.fabric.api` subpackage. See below for package structure.
    - They should be placed at the very bottom or top of the class to not hurt readability.
- Access modifiers for fields and methods should be as strict as possible.
    - If a method is intended to only be for use by mods implementing an api, a `protected` method should suffice.

### Annotations
- Nullable members, parameters or return values must be annotated with the `@Nullable` annotation.
    - Any member, parameter or return value that is not marked as nullable can be assumed to be nonnull. The `@NotNull` annotation should never be used, it is implicit.
    - Introducing custom types might be appropriate (no need to specify `List<@Nullable String>` everywhere).
- Deprecated API members should have the `@Deprecated` annotation.
    - Avoid specifying `forRemoval = true`, the functionality will be supported as long as possible.
        - Exceptions are made for experimental APIs, where deprecated for removal functionality might be removed after sufficient time has passed.
    - A javadoc `@deprecated` comment should be added to deprecated members with migration instructions.
- Experimental API classes should all have the `@ApiStatus.Experimental` annotation.
    - The annotation is not required on individual members, unless the class itself is not experimental.
- `@ApiStatus.NonExtendable` should be used for API interfaces or classes that modders must not implement or override, but can't be `final` for some reason.
    - Adding methods to such interfaces or classes is not a breaking change.
- `@ApiStatus.Internal` is automatically applied to all implementation packages to avoid IDE autocompletion suggesting them. There should be no need for internal methods or classes in the public API package.

### Naming
- All names should follow the [Yarn naming standards](https://github.com/FabricMC/yarn/blob/HEAD/CONVENTIONS.md).
- If a class only contains getter methods, the `get` prefix may be omitted for methods. The `get` prefix may also be omitted where it is not appropriate.
- Accessor mixins should be named **Target**Accessor, other mixins should be named **Target**Mixin, where `Target` is the target class name. More details in the mixin section below.

### Events

- Events should not be used if there is only one subscriber, like a handler in a registered unique namespace.
- Events should be produced and fully usable with minimal object allocation.
  - In particular, avoid data holder objects for inputs, but rather pass them as separate parameters.
- Events should use dedicated callback interfaces.
    - Callback interfaces should be `@FunctionalInterface`s.
    - Callback methods should be uniquely named such that a handler can implement multiple at once.
        - Avoid words that are already clear from the parameters. For example, prefer `onStartTick(MinecraftServer)` over `onStartServerTick(MinecraftServer)`. The interface should still be named `StartServerTick` if "server" is not already implied by the containing class.
    - Callback signatures should use the most specific type, if appropriate. E.g. `WorldChunk` over `Chunk`.
    - Callback signatures should pass context that the listener might be expected to use, without excess. For example:
        - Events involving a `MinecraftServer` directly should consider passing a `MinecraftServer` parameter.
            - If the server is easily available via the other context parameters (for example a `ServerWorld`), passing it explicitly is unnecessary.
        - Consider passing the `MinecraftClient` instance as a parameter if it makes sense.
- `Event<>` objects should be in the class declaring the event field or getter.
    - Usually, an event will be a stored in a `public static final` field.
    - Sometimes an event might be specific to a class instance. In that case, there should be static methods that return event instances. One example of this is an event specific to a registry.
    - Related callbacks and events should be grouped in classes.

Example:
```java
public final class FooEvents {
    public static final Event<Allow> ALLOW = ...;
    public static final Event<Before> BEFORE = ...;
    public static final Event<After> AFTER = ...;

    @FunctionalInterface
    public interface Allow {
        boolean allowFoo(/* relevant parameters */);
    }

    @FunctionalInterface
    public interface Before {
        void beforeFoo(/* relevant parameters */);
    }

    @FunctionalInterface
    public interface Two {
        void afterFoo(/* relevant parameters */);
    }

    // Holder class is not meant for instantiation.
    private ExampleEvents() {
    }
}
```

#### Event naming

- Callback interfaces:
    - Callback interfaces should be named using present tense. For example, `ChunkUnload` and not `ChunkUnloaded`.
    - The methods should be named in line with the action of the event, such as `entryAdded(...)`.
    - Method names for notification events should be prefixed with `on`.
      For example, a `DataLoad` event would have an `onDataLoad` method.
    - Events that may allow or block some action should start with `Allow`. For example, an event to cancel player death might be called `AllowPlayerDeath`, with method name `allowDeath`.
- `Event<>` fields and methods:
    - The field or method exposing the `Event<>` object should be named similarly to the callback interface.
- The `fabric-lifecycle-events-v1` module is a good example of event naming standards.


#### Event ordering

- Processes happening in multiple steps should use multiple events.
    - While Fabric provides an event phase system, purpose-driven events should always be preferred.
    - For example, an event that both cancels and notifies of an action will produce false notifications (notified but later canceled). It would be preferable to have an `AllowXxx` event for cancellation, and then a `BeforeXxx` event for notification.

## Implementation guidelines

### Simplicity

- Simple code that is easy to debug and reason about is generally preferable to the shortest possible implementation.
- Limited duplication can be better than indirection, unless the code is complex or used several times.
- Indirections might make the code harder to read, and should be weighed against their benefits. Examples include:
    - Lambda methods (`forEach`, streams).
    - Method splitting.
    - Recursion.
    - Complex class hierarchies.
- Functional operators (i.e. interfaces) should be designed for maximal readability, and only used when necessary.
    - Ask for concrete objects if applicable.
    - Don't include superfluous methods. For example, replace `shouldApply`+`apply` with a single `apply` that returns success.
    - Consider having the user implement a larger interface as a class.
    - Consider using a custom interface if it is beneficial for comprehension or documentation.
- The overall complexity of a module shouldn't be ignored in favor of the simplicity of each individual piece. Similarly, the overall complexity of one small piece shouldn't be ignored in favor of the simplicity of the module as a whole.

### Code quality
- Strongly validate inputs to detect misuse.
    - Exceptions should be thrown in the first method that can reasonably detect misuse.
    - Precondition assertions such as `Objects.requireNonNull` for non-null parameters are strongly encouraged.
    - This might apply to other cases, such as strong JSON validation.
- Use the weakest suitable interface or class when exposing anything without harming expected uses.
    - However, prefer `Collection` over `Iterable`.
- Non-trivial reused (directly or derived) inline constants should be moved to static final fields.
- Javac shouldn't produce any warnings in its default configuration.

### Thread safety

Since Minecraft has two primary threads, the render and server threads, APIs dealing with shared state need to consider thread safety. This will usually require consideration on a case-by-case basis.

- A registry shared between the render and the server threads may for example wish to use locking for infrequent accesses, or otherwise a copy-on-write strategy to ensure lock-free reads and thread-safe writes.
- Lazily initialized caches accessed from multiple threads require `volatile`. Use an intermediate variable to only have one volatile read:
```java
private volatile Stuff cachedStuff = null;

private Stuff stuff() {
    Stuff stuff = this.cachedStuff;

    if (stuff == null) {
        this.cachedStuff = stuff = compute();
    }

    return stuff;
}
```
- Read-only access from multiple threads does not need synchronization.

## Documentation

- Every API class should carry a Javadoc comment explaining its purpose and reference related classes. Example code in the primary class of any major feature should outline the use, including related vanilla invocations/registrations/etc as applicable to provide an idea of how to start.
    - Parameters, implementation bodies, etc... may be omitted as appropriate, since the examples are not meant as to be fully working implementations.
    - The examples should be more akin to pseudocode with a checklist for the process and pointers to everything needed.
    - These examples should be written for developers that have a knowledge of Java and basic knowledge of the Minecraft codebase.
- A brief description belongs in the 1st paragraph, with further paragraphs separated by blank lines and starting with `<p>`. Javadoc for methods may follow with another blank line before describing parameters, return values and exceptions with the appropriate tags. All accessible members should be described appropriately.
- Good documentation doesn't only explain what something is, but (as appropriate) why it exists, what are the intended use cases, what use cases something is not suitable for and any semantics that need to be kept in mind.
    - In particular, tricky implementation details should be explained by a few comments when appropriate.
- Direct references to classes or members should use the `@link` and `@linkplain` tags, unspecific further reading elsewhere may use `@see`.
- Deprecated elements should describe and reference the replacement or alternative in Javadoc `@deprecated`. The `@deprecated` tag has to be last, everything else may be removed while adding it.
- The Fabric Wiki is the proper place for additional in-depth documentation and how-tos. Examples are not necessary for simple events.

## Structure of Fabric API
Fabric API is organized in different modules. Each module is located in a specific folder matching the module id. For example under `fabric-item-api-v1/` for the Item API (v1).

### Module naming conventions
- Module names should be named after the exposed functionality.
    - Consider future developments when naming a module: they might later be expanded.
- Module names should usually be suffixed by `-api`.
    - Modules whose primary purpose is not interaction with their API do not need this suffix. For example, `fabric-transitive-access-wideners-v1` or `fabric-convention-tags-v2`.
    - Event modules should have the `-events` suffix instead.
- Module names should always be suffixed by a major version (`-v1`, `-v2`, etc).
    - The major version starts at `v1` for new functionality, unless they replace a module with equivalent functionality, in which case the version is incremented.
    - `vn` and `vn+1` module names need not match exactly. For example, `fabric-loot-tables-v1` was replaced by `fabric-loot-api-v2`.

### Deprecated modules
Modules that are entirely deprecated are in the `deprecated/` folder. All of their API classes should be deprecated, and their `fabric.mod.json` should have the deprecated lifecycle marker. (See PR checklist below).

A module may only be deprecated if all of its functionality is also provided by a non-deprecated module or vanilla Minecraft itself.

### Experimental modules
Modules whose design is hard to evaluate might go through an experimental phase, allowing for relaxed backwards compatibility requirements, as described in the Backwards compatibility section.

Writers of experimental modules need to consider the following additional requirements:
- All API classes should be marked as `@ApiStatus.Experimental`. Do not use `@Deprecated` to generate compiler and IDE warnings for experimental modules.
- All API classes should carry a javadoc comment, worded similarly to the following example:
  ```java
  /**
   * (normal javadoc here)
   * 
   * <p><b>Experimental feature</b>, may be removed or changed without further notice.
   */
  ```
- The module's `fabric.mod.json` should have the experimental lifecycle marker. (See PR checklist below).
- Note that experimental modules should be in the root folder of the Fabric API repository, as they are expected to be stabilized eventually.

### Versioning, dependencies

The initial version of a module should **always** be `1.0.0`, never 0.x for non-legacy modules.
Do not increment versions when writing a pull request. Version increments will be applied by maintainers after the pull request is merged.

Every module should in its `fabric.mod.json` declare dependencies for:
- `"fabricloader": ">=x.y.z"`, where `x.y.z` is the version used at the time the module is added.
- Other used modules, for example `"fabric-api-base": "*"` if events are used. No explicit version needs to be specified.
- Minecraft and Java version dependencies do not need to be specified.
- In general, version ranges in module dependencies should be optimistic, omitting an upper bound until it is known.


### Packages

Each module contains various sourcesets in the `src` folder:
- `src/client`: Code for client-only additions such as rendering hooks.
- `src/main`: Code for additions that are available on both the client and the server.
- `src/testmod`: Testing code.

Inside the relevant sourceset, all Fabric API code should be in a subpackage of `net.fabricmc.fabric`.
- Add `.api`, `.impl` and `.mixin` for public-facing API, implementation details, and mixins respectively.
- Add `.client` or `.server` for client-only or dedicated server-only code respectively. Common code requires no additional folder.
- Add module name subpackage. It might contain multiple parts separated by `.`.
- For API only: add the module major version with a v prefix, for example `.v1`
- Further subpackages can be added as needed, all singular.

A good example is the Lifecycle Events (v1) module.


### Mixins

These guidelines should be followed with regards to a mixin's visibility and naming:

|            | Accessors          | Mixin                                                                      |
|------------|--------------------|----------------------------------------------------------------------------|
| Naming     | **Target**Accessor | **Target**Mixin (May include `Client/Server` or `Legacy` prefix if needed) |
| Visibility | public             | package-private*                                                           |

\* A mixin may be public if a subclass extends a super mixin in a different package. Example: `abstract class ServerWorldMixin extends WorldMixin`

The organization of mixins with a package is dependent on the type of module.
- Generally if there are a small amount of mixins, then having all mixins in the same package is fine.
- Client only and dedicated server only mixins should be moved into `client/server` subpackages.
- If a module is generally complex or has multiple distinct parts, multiple mixins for each class target may exist assuming said mixins are separated into subpackages by feature.


## Code formatting

Formatting should be consistent with the remainder of Fabric API.

The general code formatting is as follows:
- Single tabs per indentation level, 2 extra tabs for continued lines.
- Blank line between block statements within the same indentation level (unless adjacent to if, else, do etc).
- No blank lines at the start or end of a block, i.e. no blank line after `{` or before `}`.
- Blank line after input (parameter) validation, if present.
- New line after `{` unless empty  (`{}` or `{ }`), never before.
- Line length usually below 120 columns, sometimes up to 160 if beneficial for readability.
- Single space after `if`, `for`, `do`, `break` unless followed by `;`.
- No space after `(` or before `)`.
- Statements should use the block form except for single-line if statements that use a short condition and nothing else.
- Ternary operator use only within a single line total.

Fabric API uses the Gradle checkstyle plugin. If you run `gradle check` any style errors will be logged in the output. The style errors can also be viewed in a generated webpage by the Gradle task.

### Style guidelines

These are less strict guidelines for the code style, but you should generally obey these:

- No redundant static access qualifiers.
  - For example if you call `ClassHere.staticThing()` inside of another method in `ClassHere`, you should avoid fully qualifying the static access to `staticThing()`.
- The use of the `this` qualifier is optional. You may need to use it in order to avoid shadowing variables but its general use elsewhere is fine.
  - Generally keep consistent with the current standard inside of the class you are editing.

## Testing
Including testing code when submitting new features is essential, both to demonstrate the feature, and to ensure that it works correctly.
Testing code should not be in the regular source sets, it belongs to so-called "test mods".

- Test mods are located in the `testmod` source set. Example: `fabric-lifecycle-events-v1/src/testmod/`
- Test mods should provide the necessary content to debug an issue with the api manually.
  - Test mods can also be helpful in order to illustrate how to use a module as a fallback for wiki pages that have yet to be written.
- If a test mod can be partially automated then it is encouraged to implement an automatically failing test if something goes wrong.
  - This allows issues with the implementation or porting issues to be detected immediately.  
  - For example, if a test mod checks if commands are registered on the server and the commands are not on the `CommandDispatcher` then the test should fail. 
  - Good places to run the automated checks include:
    - The mod initializer if applicable.
    - In a listener for `ServerLifecycleEvents.SERVER_STARTED` if a server instance is required.
    - A game test.
  - Test failures should always throw an `AssertionError` explaining what condition was not met during the test.

Fabric API pull requests should be tested in the dev environment and in production (on both a client and dedicated server).
The `gradlew build` command can be used to produce the Fabric API fatjar, located in `builds/libs/`.

#### Common mistakes
One highly likely cause of a production failure is the use of `remap=false` in a mixin. If `remap=false` is used, you need to verify the mixin works in dev and production. Most likely the mixin will not work in production.

## Checklist before submitting a pull request

### Apply license headers
There is a Gradle task that can automate this for you. Simply run `gradlew spotlessApply`.

### Run checks
- The `gradlew check` task runs all the style checks and game tests.
- Code style can be checked with the faster `gradlew checkstyleMain checkstyleTestmod`.


### Any new modules/newly deprecated modules need to specify the correct module lifecycle.
If you have created a new module you need to specify the module lifecycle. The `gradlew check` task will pick up any missing module lifecycles for you and tell you the subproject it is missing in. The module lifecycle is specified in the `fabric.mod.json`.

**Example:**

```json=
"custom": {
    "fabric-api:module-lifecycle": "<ModuleLifecycle>"
}
```

The supported values for the `ModuleLifecycle` are:
- `"stable"`
- `"experimental"`
- `"deprecated"`

### Testing
See the Testing section above. Make sure to describe in the pull request body how you tested your code, and include relevant test mod code with your pull request.
