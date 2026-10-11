package com.aristidevs.cursotestingandroid.checkout.presentation

import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import com.aristidevs.cursotestingandroid.cart.domain.repository.CartItemRepository
import com.aristidevs.cursotestingandroid.cart.domain.usecase.GetCartSummaryUseCase
import com.aristidevs.cursotestingandroid.checkout.domain.usecase.PlaceOrderUseCase
import com.aristidevs.cursotestingandroid.core.MainDispatcherRule
import com.aristidevs.cursotestingandroid.core.mockwebserver.MiniMarketApiDispatcher
import com.aristidevs.cursotestingandroid.core.mockwebserver.MockWebServerUrlHolder
import com.aristidevs.cursotestingandroid.core.mockwebserver.ProductErrorDispatcher
import com.aristidevs.cursotestingandroid.core.mockwebserver.rules.MockWebServerRule
import com.aristidevs.cursotestingandroid.core.utils.asAsset
import com.aristidevs.cursotestingandroid.productlist.domain.repository.ProductRepository
import com.aristidevs.cursotestingandroid.productlist.domain.repository.PromotionRepository
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject
import kotlin.reflect.KClass
import kotlin.test.assertTrue

/**
 * EXAMEN — Tests de INTEGRACIÓN del ViewModel de checkout (extremo a extremo).
 *
 * Completa cada test siguiendo Given-When-Then. No modifiques producción.
 * SUT: [CheckoutViewModel] con casos de uso reales + Room + MockWebServer.
 * Pistas: inyecta dependencias con Hilt, prepara el carrito real, observa `uiState` con Turbine,
 * y verifica que tras un pedido OK el estado es Success y el carrito queda vacío.
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class CheckoutViewModelIntegrationTest {
    private companion object {
        const val PRODUCT_ID = "p1"
        const val PRODUCT_QUANTITY = 4
        const val USER_NAME = "NAME"
        const val USER_ADDRESS = "ADDRESS 1234"
        const val USER_EMAIL = "useremail@aaasfasdf.es"
    }

    @get:Rule(order = 0)
    val mockWebServer = MockWebServerRule()

    @get:Rule(order = 1)
    val hilt = HiltAndroidRule(this)

    @get:Rule(order = 2)
    val mainDispatcherRule = MainDispatcherRule()

    @Inject
    lateinit var placeOrderUseCase: PlaceOrderUseCase

    @Inject
    lateinit var getCartSummaryUseCase: GetCartSummaryUseCase

    @Inject
    lateinit var cartItemRepository: CartItemRepository

    @Inject
    lateinit var productRepository: ProductRepository

    @Inject
    lateinit var promotionRepository: PromotionRepository

    @Before
    fun setUp() =
        runTest {
            mockWebServer.server.dispatcher =
                MiniMarketApiDispatcher(
                    productJson = "product_list_default.json".asAsset(),
                    orderConfirmationJson = "order_confirmation_default.json".asAsset(),
                )
            hilt.inject()
            cartItemRepository.clearCart()
            productRepository.refreshProduct()
            promotionRepository.refreshPromotions()
        }

    @After
    fun tearDown() {
        MockWebServerUrlHolder.baseUrl = "http://localhost:8080/"
    }

    @Test
    fun givenItemsInCart_whenViewModelInitialized_thenIdleStateWithSummary() = runTest {
        // GIVEN
        cartItemRepository.addToCart(PRODUCT_ID, PRODUCT_QUANTITY)

        // WHEN
        val viewModel = createViewModel()

        // THEN
        viewModel.uiState.test {
            val idle = awaitSpecificUiState(CheckoutUiState.Idle::class)

            assertTrue(idle is CheckoutUiState.Idle)
            assertTrue(idle.summary.subtotal > 0.0)
            assertTrue(idle.summary.finalTotal > 0.0)
        }
    }

    @Test
    fun givenValidFormAndSuccessfulOrder_whenOnConfirm_thenSuccessStateAndCartCleared() = runTest {
        // GIVEN
        cartItemRepository.addToCart(PRODUCT_ID, PRODUCT_QUANTITY)
        val viewModel = createViewModel()
        setupValidFormFields(viewModel)

        // WHEN
        viewModel.onConfirm()

        // THEN
        viewModel.uiState.test {
            val success = awaitSpecificUiState(CheckoutUiState.Success::class)

            assertTrue(success is CheckoutUiState.Success)
            assertTrue(success.confirmation.total > 0)
            assertTrue(cartItemRepository.getCartItems().first().isEmpty())
        }
    }

    @Test
    fun givenOrderEndpointFails_whenOnConfirm_thenErrorState() = runTest {
        // GIVEN
        cartItemRepository.addToCart(PRODUCT_ID, PRODUCT_QUANTITY)
        mockWebServer.server.dispatcher = ProductErrorDispatcher()

        val viewModel = createViewModel()
        setupValidFormFields(viewModel)

        // WHEN
        viewModel.onConfirm()

        // THEN
        viewModel.uiState.test {
            val state = awaitSpecificUiState(CheckoutUiState.Error::class)

            assertTrue(state is CheckoutUiState.Error)
        }
    }

    private fun createViewModel(): CheckoutViewModel =
        CheckoutViewModel(
            placeOrderUseCase = placeOrderUseCase,
            getCartSummaryUseCase = getCartSummaryUseCase,
        )

    private suspend fun ReceiveTurbine<CheckoutUiState>.awaitSpecificUiState(uiStateClass: KClass<out CheckoutUiState>): CheckoutUiState {
        while (true) {
            val item = awaitItem()
            if (item::class == uiStateClass) {
                return item
            }
        }
    }

    private fun setupValidFormFields(viewModel: CheckoutViewModel) {
        viewModel.onNameChange(USER_NAME)
        viewModel.onEmailChange(USER_EMAIL)
        viewModel.onAddressChange(USER_ADDRESS)
    }
}
