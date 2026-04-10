package org.example.project

// This file is intentionally kept as the test source-set entry point.
// All tests live in sub-packages:
//
//  domain/WeatherInfoTest.kt           — pure unit tests, no coroutines
//  data/WeatherDtoSerializationTest.kt — JSON deserialization
//  data/WeatherRepositoryTest.kt       — repository via Ktor MockEngine
//  presentation/WeatherViewModelTest.kt — ViewModel state + coroutines
