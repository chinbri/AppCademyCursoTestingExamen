package com.aristidevs.cursotestingandroid.checkout.presentation

import app.cash.turbine.test
import com.aristidevs.cursotestingandroid.cart.domain.repository.CartItemRepository
import com.aristidevs.cursotestingandroid.cart.domain.usecase.GetCartSummaryUseCase
import com.aristidevs.cursotestingandroid.checkout.domain.repository.OrderRepository
import com.aristidevs.cursotestingandroid.checkout.domain.usecase.PlaceOrderUseCase
import com.aristidevs.cursotestingandroid.core.MainDispatcherRule
import com.aristidevs.cursotestingandroid.core.builders.cartItem
import com.aristidevs.cursotestingandroid.core.builders.product
import com.aristidevs.cursotestingandroid.core.fakes.FakeCartItemRepository
import com.aristidevs.cursotestingandroid.core.fakes.FakeOrderRepository
import com.aristidevs.cursotestingandroid.core.fakes.FakeProductRepository
import com.aristidevs.cursotestingandroid.core.fakes.FakePromotionRepository
import com.aristidevs.cursotestingandroid.core.fakes.FakeSystemClock
import com.aristidevs.cursotestingandroid.productlist.domain.repository.ProductRepository
import com.aristidevs.cursotestingandroid.productlist.domain.repository.PromotionRepository
import com.aristidevs.cursotestingandroid.productlist.domain.usecase.GetPromotionForProduct
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * EXAMEN — Tests UNITARIOS del ViewModel de checkout.
 *
 * Completa cada test siguiendo Given-When-Then. No modifiques producción.
 * SUT: [CheckoutViewModel] — estados [CheckoutUiState], `canSubmit`, `onConfirm`, eventos.
 * Pistas: usa Turbine sobre `uiState`/`event`, `runTest(mainDispatcherRule.scheduler)`,
 * fakes (FakeCartItemRepository, FakeProductRepository, FakePromotionRepository, FakeSystemClock)
 * y un fake de OrderRepository que tendrás que crear.
 */
class CheckoutViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val validName = "VALID NAME"
    private val validAddress = "VALID ADDRESS"
    private val validEmail = "my_valid_email@test.com"
    private val inValidEmail = "WRONG EMAIL!!"

    @Test
    fun `given empty cart when initialized then canSubmit is false`() = runTest(mainDispatcherRule.scheduler) {
        // GIVEN
        val cartItemRepository = FakeCartItemRepository().apply {
            setCartItems(emptyList())
        }

        // WHEN
        val viewModel = createViewModel(
            cartItemRepository = cartItemRepository,
        )

        // THEN
        viewModel.uiState.test {
            val submission = awaitItem()
            assertTrue(submission is CheckoutUiState.Idle)
            assertTrue((submission as CheckoutUiState.Idle).canSubmit.not())

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `given valid form and non empty cart when form completed then canSubmit is true`() =
        runTest(mainDispatcherRule.scheduler) {
            // GIVEN
            val productId = "1"
            val cartItem =
                cartItem {
                    withProductId(productId)
                    withQuantity(6)
                }
            val cartItemRepository = FakeCartItemRepository().apply {
                setCartItems(listOf(cartItem))
            }
            val productRepository = FakeProductRepository().apply {
                setProducts(
                    listOf(
                        product {
                            withId(productId)
                            withName("Pan")
                            withPrice(2.0)
                        },
                    ),
                )
            }

            // WHEN
            val viewModel = createViewModel(
                cartItemRepository = cartItemRepository,
                productRepository = productRepository,
            )
            viewModel.onNameChange(validName)
            viewModel.onEmailChange(validEmail)
            viewModel.onAddressChange(validAddress)

            // THEN
            viewModel.uiState.test {
                val uiState = awaitItem()
                assertTrue(uiState is CheckoutUiState.Idle)
                assertTrue((uiState as CheckoutUiState.Idle).canSubmit)

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `given malformed email when email changed then emailError is INVALID_EMAIL and canSubmit is false`() =
        runTest(mainDispatcherRule.scheduler) {
            // GIVEN

            // WHEN
            val viewModel = createViewModel()
            viewModel.onNameChange(validName)
            viewModel.onAddressChange(validAddress)
            viewModel.onEmailChange(inValidEmail)

            // THEN
            viewModel.uiState.test {
                val uiState = awaitItem()
                assertTrue(uiState is CheckoutUiState.Idle)
                assertTrue((uiState as CheckoutUiState.Idle).errors.emailError != null)
                assertTrue(uiState.canSubmit.not())

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `given valid form when onConfirm succeeds then emits Success state`() = runTest(mainDispatcherRule.scheduler) {
        // GIVEN

        // WHEN
        val viewModel = createViewModel()
        viewModel.onNameChange(validName)
        viewModel.onEmailChange(validEmail)
        viewModel.onAddressChange(validAddress)
        viewModel.onConfirm()

        // THEN
        viewModel.uiState.test {
            val uiState = awaitItem()
            assertTrue(uiState is CheckoutUiState.Success)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `given place order fails when onConfirm then emits Error state and ShowMessage event`() =
        runTest(mainDispatcherRule.scheduler) {
            // GIVEN
            val orderRepository = FakeOrderRepository().apply {
                throwExceptionWhenPlaceOrder = true
            }

            // WHEN
            val viewModel = createViewModel(
                orderRepository = orderRepository,
            )
            viewModel.onNameChange(validName)
            viewModel.onEmailChange(validEmail)
            viewModel.onAddressChange(validAddress)

            // THEN
            viewModel.event.test {
                viewModel.onConfirm()

                val event = awaitItem()
                assertTrue(event is CheckoutEvent.ShowMessage)

                cancelAndIgnoreRemainingEvents()
            }
            viewModel.uiState.test {
                val uiState = awaitItem()
                assertTrue(uiState is CheckoutUiState.Error)

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `given invalid form when onConfirm then does not place order`() = runTest(mainDispatcherRule.scheduler) {
        // GIVEN

        // WHEN
        val viewModel = createViewModel()
        viewModel.onNameChange("")
        viewModel.onConfirm()

        // THEN
        viewModel.uiState.test {
            val uiState = awaitItem()
            assertTrue(uiState is CheckoutUiState.Idle)
            assertTrue((uiState as CheckoutUiState.Idle).canSubmit.not())
            assertTrue(uiState.errors.isValid.not())

            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun createViewModel(
        orderRepository: OrderRepository = FakeOrderRepository(),
        cartItemRepository: CartItemRepository = FakeCartItemRepository(),
        productRepository: ProductRepository = FakeProductRepository(),
        promotionRepository: PromotionRepository = FakePromotionRepository(),
    ): CheckoutViewModel {
        val fakeClock = FakeSystemClock()

        val placeOrderUseCase = PlaceOrderUseCase(
            orderRepository = orderRepository,
            cartItemRepository = cartItemRepository,
        )

        val getCartSummaryUseCase = GetCartSummaryUseCase(
            cartItemRepository = cartItemRepository,
            productRepository = productRepository,
            promotionRepository = promotionRepository,
            getPromotionForProduct = GetPromotionForProduct(),
            clock = fakeClock,
        )

        return CheckoutViewModel(
            placeOrderUseCase = placeOrderUseCase,
            getCartSummaryUseCase = getCartSummaryUseCase,
        )
    }
}
