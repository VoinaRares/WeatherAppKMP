package org.example.project.network

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.android.Android

actual fun platformHttpClientEngine(): HttpClientEngine = Android.create()
