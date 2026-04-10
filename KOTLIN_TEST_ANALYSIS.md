# kotlin.test — Analysis & Findings

> Tested against Kotlin 2.3.20 / Compose Multiplatform 1.10.3  
> Project targets: Android, iOS (arm64 + simulatorArm64)

---

## What is kotlin.test?

`kotlin.test` is the official Kotlin multiplatform testing library. It provides a thin, annotation-driven API that compiles to the native test runner on each platform:

| Platform       | Backed by              |
|----------------|------------------------|
| JVM / Android  | JUnit 4 (default) or JUnit 5 |
| iOS / Native   | XCTest (via Kotlin/Native test infrastructure) |
| JS             | Mocha / Jest (via `kotlin.test-js`) |

Everything in `kotlin.test` is in `commonMain` — you write one test once and it runs everywhere.

---

## Core API

### Annotations

| Annotation      | Purpose                                              |
|-----------------|------------------------------------------------------|
| `@Test`         | Marks a function as a test case                      |
| `@BeforeTest`   | Runs before each test in the class                   |
| `@AfterTest`    | Runs after each test in the class                    |
| `@Ignore`       | Skips a test (with optional message)                 |
| `@TestFactory`  | Dynamic test generation (JVM only — see limitations) |

### Assertion functions

```kotlin
assertEquals(expected, actual)                       // structural equality
assertEquals(1.5, actual, absoluteTolerance = 0.01)  // floating-point with tolerance
assertNotEquals(unexpected, actual)
assertTrue(condition) / assertFalse(condition)
assertNull(value) / assertNotNull(value)
assertIs<T>(value)                                    // type check + smart cast
assertIsNot<T>(value)
assertContentEquals(expected, actual)                 // collection / array deep equality
assertFailsWith<ExceptionType> { block }              // exception assertion with type check
assertFails { block }                                 // any exception
fail("message")                                       // unconditional failure
```

### Expect DSL (alternative syntax)

```kotlin
expect(actual).toBe(expected)         // deprecated since Kotlin 1.5 — prefer assertEquals
```

---

## Extensions used in this project

### kotlinx-coroutines-test

The standard companion library for testing suspending code. It integrates with `kotlin.test` via `runTest { }`.

```kotlin
// In build.gradle.kts (commonTest):
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
```

Key APIs used:

| API | Purpose |
|-----|---------|
| `runTest { }` | Suspending test body; virtual time replaces real delays |
| `StandardTestDispatcher` | Pauses coroutines until `advanceUntilIdle()` is called |
| `UnconfinedTestDispatcher` | Runs coroutines eagerly/inline — simpler but less control |
| `Dispatchers.setMain(dispatcher)` | Replaces `Dispatchers.Main` so `viewModelScope` works in tests |
| `Dispatchers.resetMain()` | Restores `Main` — must be called in `@AfterTest` |
| `advanceUntilIdle()` | Drains all pending coroutines in the test scope |
| `advanceTimeBy(millis)` | Simulate time passing without real delay |

### ktor-client-mock

Ktor's official test engine — lets you intercept HTTP calls with a `MockEngine`.

```kotlin
val engine = MockEngine { request ->
    respond(
        content = ByteReadChannel(jsonString),
        status  = HttpStatusCode.OK,
        headers = headersOf(HttpHeaders.ContentType, "application/json"),
    )
}
val client = createHttpClient(engine) // inject into real Ktor HttpClient
```

This gave us full `WeatherRepositoryImpl` coverage without a network connection.

---

## What was tested and how

| Layer | Test class | Technique |
|-------|-----------|-----------|
| Domain model (`WeatherInfo`) | `WeatherInfoTest` | Pure `kotlin.test` assertions — no async |
| Serialization (`WeatherResponseDto`) | `WeatherDtoSerializationTest` | `kotlinx.serialization` + assertions |
| Repository (`WeatherRepositoryImpl`) | `WeatherRepositoryTest` | `MockEngine` + `runTest` + `Flow.single()` |
| ViewModel (`WeatherViewModel`) | `WeatherViewModelTest` | `FakeWeatherRepository` + `setMain` + `advanceUntilIdle` |

---

## Strengths

### 1. True multiplatform — write once, run on all targets
A single test class in `commonTest` runs as JUnit on Android and as XCTest on iOS. No test duplication, no platform-specific test setup.

### 2. Small, focused API
The assertion set covers every common case. The lack of ceremony makes tests readable without learning a DSL.

### 3. `assertIs<T>` with smart cast
```kotlin
val state = viewModel.uiState.value
assertIs<WeatherUiState.Success>(state)
// state is now smart-cast to Success — no manual cast needed
assertEquals("London", state.weather.cityName)
```
Unique to Kotlin — exhaustive sealed-interface testing feels natural.

### 4. First-class coroutine support via `runTest`
`runTest` replaces all delays with virtual time, making async tests instant. Combined with `StandardTestDispatcher`, you can assert intermediate states (e.g. the `Loading` state between `Idle` and `Success`).

### 5. Floating-point tolerance built-in
```kotlin
assertEquals(12.3, result.temperature, absoluteTolerance = 0.001)
```
No third-party matcher library needed for numeric comparison.

### 6. No annotation processor / no reflection
Works correctly on Kotlin/Native (iOS) where reflection is limited.

---

## Limitations

### 1. No built-in mocking
`kotlin.test` provides zero mocking. You must write **fakes/stubs by hand** (as done here with `FakeWeatherRepository`) or use a third-party library like MockK. MockK supports KMP but its native support is experimental.

### 2. No parameterized tests in commonMain
`@TestFactory` (dynamic/parameterized tests) exists on JVM but is **not available in commonMain**. The workaround used here is manual `listOf(1,2,3).forEach { code -> ... }` inside a single `@Test`.

### 3. No built-in assertion for Flow sequences
`kotlin.test` has no concept of `Flow`. Collecting states into a `List` and asserting on that list works, but is verbose. The third-party library **Turbine** (`app.cash.turbine`) solves this elegantly — but that is a Kotest/external story.

### 4. No test ordering or grouping
There is no equivalent of JUnit 5's `@Nested`, `@Order`, or `@Tag`. All `@Test` functions in a class are peers with no guaranteed order.

### 5. `Dispatchers.Main` on iOS native requires care
`Dispatchers.setMain()` from `coroutines-test` works on native, but `viewModelScope` depends on `Dispatchers.Main.immediate` being properly available at test time. On iOS, if the test binary does not have a run loop, `Main` may behave unexpectedly. The workaround is to always pair `setMain(UnconfinedTestDispatcher())` in `@BeforeTest` with `resetMain()` in `@AfterTest`.

### 6. `@Ignore` has limited XCTest integration
On iOS, `@Ignore` marks the test as skipped in Kotlin's own runner, but XCTest may still report it differently than expected.

### 7. No assertion for exceptions with a message matcher (only type)
```kotlin
// This checks the type but NOT the message content:
assertFailsWith<RuntimeException> { ... }

// To also check the message you need:
val ex = assertFailsWith<RuntimeException> { ... }
assertEquals("expected msg", ex.message)
```

---

## Does it work on both Android and iOS?

**Yes — with caveats.**

| Concern | Android | iOS |
|---------|---------|-----|
| `@Test`, `@BeforeTest`, `@AfterTest` | JUnit 4 | XCTest |
| `assertEquals`, `assertIs`, etc. | Full support | Full support |
| `runTest { }` from coroutines-test | Full support | Full support |
| `Dispatchers.setMain` / `resetMain` | Full support | Works, needs run-loop aware setup |
| `ktor-client-mock` | Full support | Full support |
| Reflection-based libraries (some MockK features) | Full support | Limited / not recommended |
| `@TestFactory` | JUnit 4 bridge available | Not available |

The architecture in this project was specifically designed for this:
- `WeatherRepository` is an **interface** → hand-write a `FakeWeatherRepository`, no reflection needed
- `createHttpClient(engine)` accepts an optional engine → inject `MockEngine` without platform engines
- Domain models are pure `data class` → `assertEquals` works without any extras

---

## Recommended additions for the next phase (Kotest)

When you add Kotest, the comparison points will be:

| Feature | kotlin.test | Kotest |
|---------|-------------|--------|
| Assertion style | `assertEquals(a, b)` | `a shouldBe b` (fluent DSL) |
| Parameterized tests | Manual loop | `withData { ... }` |
| Flow testing | Manual collect | `flowOf(...).test { ... }` or Turbine integration |
| Nested tests | Not available | `describe/it` or `given/when/then` specs |
| Soft assertions | Not available | `assertSoftly { ... }` |
| Property testing | Not available | `forAll { a, b -> ... }` |
| iOS support | Full | Partial (Kotest native runner is experimental) |
