package com.aristidevs.cursotestingandroid.checkout.data.repository

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.aristidevs.cursotestingandroid.checkout.domain.repository.OrderRepository
import com.aristidevs.cursotestingandroid.core.domain.model.AppError
import com.aristidevs.cursotestingandroid.core.mockwebserver.rules.MockWebServerRule
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.SocketPolicy
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject
import kotlin.test.assertEquals

/**
 * EXAMEN — Tests de INTEGRACIÓN del repositorio de pedidos.
 *
 * Completa cada test siguiendo Given-When-Then. No modifiques producción.
 * SUT: [OrderRepositoryImpl] sobre RemoteDataSource real + MockWebServer
 * (200 -> OrderConfirmation mapeada; 404 -> AppError.NotFoundError; red caída -> AppError.NetworkError).
 * Pistas: encola respuestas en `mockWebServer.server`, inyecta con Hilt (`hilt.inject()`).
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class OrderRepositoryImplTest {
    @get:Rule(order = 0)
    val mockWebServer = MockWebServerRule()

    @get:Rule(order = 1)
    val hilt = HiltAndroidRule(this)

    @Inject
    lateinit var orderRepository: OrderRepository

    @Before
    fun setUp() =
        runTest {
            hilt.inject()
        }

    @Test
    fun givenSuccessfulResponse_whenPlaceOrder_thenReturnsOrderConfirmation() = runTest {
        val orderId = "ORD-1001"
        val etaMinutes = 130
        val total = 0.0

        // GIVEN
        mockWebServer.server.enqueue(
            MockResponse().setBody("""{"orderId":$orderId,"etaMinutes":$etaMinutes,"total":$total}""").setResponseCode(200),
        )

        // WHEN
        val result = orderRepository.placeOrder()

        // THEN
        assertEquals(result.orderId, orderId)
        assertEquals(result.etaMinutes, etaMinutes)
        assertEquals(result.total, total)
    }

    @Test(expected = AppError.NotFoundError::class)
    fun given404Response_whenPlaceOrder_thenThrowsNotFoundError() = runTest {
        // GIVEN
        mockWebServer.server.enqueue(
            MockResponse().setResponseCode(404),
        )

        // WHEN
        orderRepository.placeOrder()

        // THEN
        // AppError.NotFoundError is thrown
    }

    @Test(expected = AppError.NetworkError::class)
    fun givenNetworkFailure_whenPlaceOrder_thenThrowsNetworkError() = runTest {
        // GIVEN
        mockWebServer.server.enqueue(
            MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE),
        )

        // WHEN
        orderRepository.placeOrder()

        // THEN
        // AppError.NetworkError is thrown
    }
}
