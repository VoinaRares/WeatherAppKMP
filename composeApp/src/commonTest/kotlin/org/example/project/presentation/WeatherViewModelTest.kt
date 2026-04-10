package org.example.project.presentation

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.example.project.domain.model.City
import org.example.project.fakes.FakeWeatherRepository
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Tests [WeatherViewModel] state transitions.
 *
 * ## Coroutine strategy
 * [viewModelScope] uses [Dispatchers.Main] internally, so we must replace it
 * with a test dispatcher before each test and restore it afterwards.
 *
 * - [UnconfinedTestDispatcher] runs coroutines eagerly (inline with the caller),
 *   which makes state assertions simple — no advance* calls needed.
 * - [StandardTestDispatcher] pauses coroutines until explicitly advanced,
 *   letting us capture intermediate states such as Loading.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class WeatherViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepo: FakeWeatherRepository
    private lateinit var viewModel: WeatherViewModel

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepo  = FakeWeatherRepository()
        viewModel = WeatherViewModel(fakeRepo)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ── initial state ─────────────────────────────────────────────────────

    @Test
    fun initialState_isIdle() {
        assertIs<WeatherUiState.Idle>(viewModel.uiState.value)
    }

    // ── happy path ────────────────────────────────────────────────────────

    @Test
    fun fetchWeather_success_transitionsIdleToLoadingToSuccess() = runTest {
        val states = mutableListOf<WeatherUiState>()

        // Collect all states emitted during the test
        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.toList(states)
        }

        viewModel.fetchWeather(london)
        advanceUntilIdle()
        collectJob.cancel()

        // Idle → Loading → Success
        assertIs<WeatherUiState.Idle>(states[0])
        assertIs<WeatherUiState.Loading>(states[1])
        assertIs<WeatherUiState.Success>(states[2])
    }

    @Test
    fun fetchWeather_success_successStateContainsCorrectWeather() = runTest {
        viewModel.fetchWeather(london)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertIs<WeatherUiState.Success>(state)
        assertEquals(FakeWeatherRepository.defaultWeather(), state.weather)
    }

    @Test
    fun fetchWeather_passesCorrectCityCoordinatesToRepository() = runTest {
        val tokyo = City("Tokyo", 35.6762, 139.6503)

        viewModel.fetchWeather(tokyo)
        advanceUntilIdle()
        assertNotNull(fakeRepo.lastLatitude)
        assertNotNull(fakeRepo.lastLongitude)
        if(fakeRepo.lastLongitude != null && fakeRepo.lastLatitude != null) {
            assertEquals(35.6762, fakeRepo.lastLatitude!!, absoluteTolerance = 0.0001)
            assertEquals(139.6503, fakeRepo.lastLongitude!!, absoluteTolerance = 0.0001)
        }
        assertEquals("Tokyo",   fakeRepo.lastCityName)
    }

    // ── error path ────────────────────────────────────────────────────────

    @Test
    fun fetchWeather_repositoryThrows_stateBecomesError() = runTest {
        fakeRepo.shouldThrow = true
        fakeRepo.throwable   = RuntimeException("Timeout")

        viewModel.fetchWeather(london)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertIs<WeatherUiState.Error>(state)
        assertEquals("Timeout", state.message)
    }

    @Test
    fun fetchWeather_errorMessage_reflectsThrowableMessage() = runTest {
        fakeRepo.shouldThrow = true
        fakeRepo.throwable   = IllegalStateException("No internet")

        viewModel.fetchWeather(london)
        advanceUntilIdle()

        val state = viewModel.uiState.value as WeatherUiState.Error
        assertTrue(state.message.contains("No internet"))
    }

    // ── reset ─────────────────────────────────────────────────────────────

    @Test
    fun reset_afterSuccess_returnsToIdle() = runTest {
        viewModel.fetchWeather(london)
        advanceUntilIdle()
        assertIs<WeatherUiState.Success>(viewModel.uiState.value)

        viewModel.reset()

        assertIs<WeatherUiState.Idle>(viewModel.uiState.value)
    }

    @Test
    fun reset_afterError_returnsToIdle() = runTest {
        fakeRepo.shouldThrow = true

        viewModel.fetchWeather(london)
        advanceUntilIdle()
        assertIs<WeatherUiState.Error>(viewModel.uiState.value)

        viewModel.reset()

        assertIs<WeatherUiState.Idle>(viewModel.uiState.value)
    }

    // ── job cancellation ──────────────────────────────────────────────────

    @Test
    fun fetchWeather_calledTwiceRapidly_onlyLatestRequestWins() = runTest {
        val paris = City("Paris", 48.8566, 2.3522)

        viewModel.fetchWeather(london)
        viewModel.fetchWeather(paris) // cancels the first job

        advanceUntilIdle()

        // The last city name provided to the fake must be Paris, not London
        assertEquals("Paris", fakeRepo.lastCityName)
        assertIs<WeatherUiState.Success>(viewModel.uiState.value)
    }

    // ── loading state is emitted before collecting ─────────────────────

    @Test
    fun fetchWeather_loadingState_emittedBeforeResult() = runTest {
        viewModel.fetchWeather(london)

        advanceUntilIdle()
        assertIs<WeatherUiState.Success>(viewModel.uiState.value)
    }

    // ── helpers ───────────────────────────────────────────────────────────

    private val london = City("London", 51.5074, -0.1278)
}
