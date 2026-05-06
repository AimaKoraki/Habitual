# Android/Kotlin/Compose Coding Conventions

## 1. Architectural Patterns (MVVM / MVI)
- Enforce a strict unidirectional data flow (UDF).
- UI layers must never handle business logic. Route all actions through ViewModels.
- State must be exposed as a single source of truth using `StateFlow` or `SharedFlow` from the ViewModel. Never use `LiveData` for new code.
- Keep the domain layer pure Kotlin (no Android framework dependencies).

## 2. Kotlin Idioms & Best Practices
- Prefer `val` over `var` for immutability by default.
- Utilize Kotlin scope functions (`let`, `apply`, `run`, `with`, `also`) to write concise, chainable code.
- Exhaustive `when` statements: Always use sealed classes or sealed interfaces to represent UI states (e.g., `Loading`, `Success`, `Error`) to enforce exhaustive compilation checks.
- Coroutines: Never block the main thread. Inject `CoroutineDispatcher` (IO, Default, Main) rather than hardcoding `Dispatchers.IO` to allow for testability.
- Null Safety: Prefer safe calls (`?.`) and Elvis operators (`?:`). Never use the non-null assertion operator (`!!`).

## 3. Jetpack Compose Rules
- **Stateless Composables:** Default to stateless Composables. Hoist state up to the caller (usually the Screen/Route level) by passing data down and lambdas up.
- **Modifier Discipline:** - Every Composable MUST accept an optional `modifier: Modifier = Modifier` as its first optional parameter.
  - Apply the passed modifier to the root layout element of the Composable.
  - Order modifiers logically: Size/Layout -> Background/Decorations -> Padding/Spacing -> Interactions (clickable).
- **Recomposition:** Avoid passing unstable objects (like standard Lists) into Composables if they cause unnecessary recomposition. Prefer `kotlinx.collections.immutable` or wrap them in `@Stable`/`@Immutable` data classes.
- **Preview:** Always provide `@Preview` functions for custom UI components with realistic mock data to facilitate rapid UI iteration.

## 4. UI/UX & Resource Management
- Do not hardcode strings, dimensions, or colors. Extract them to `strings.xml`, `dimens.xml`, and the Compose `MaterialTheme` respectively.
- Ensure all interactive elements have a minimum touch target size of 48.dp.
- Handle different screen states gracefully (empty states, loading spinners, network error fallbacks).

## 5. Dependency Injection
- Use Hilt (or your designated DI framework) for all dependency resolution.
- ViewModels should receive dependencies via constructor injection (`@HiltViewModel`, `@Inject`).

## 6. Testing Strategy
- ViewModel logic must have comprehensive JUnit tests covering state emissions and coroutine execution.
- UI layer (Compose) should have semantic UI tests. Ensure Composables have `testTag` modifiers where necessary for robust UI automation.

## 7. Agentic Refactoring Guidelines
- When analyzing a file, look for "God ViewModels" or massive Composable functions and suggest breaking them down into smaller, single-responsibility components.
- If suggesting a UI change, wrap your architectural reasoning in `<|think|>` tags to ensure it aligns with Material Design 3 guidelines before writing the Compose code.