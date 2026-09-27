package com.aristidevs.cursotestingandroid.checkout.domain.usecase

import com.aristidevs.cursotestingandroid.cart.domain.repository.CartItemRepository
import com.aristidevs.cursotestingandroid.checkout.domain.repository.OrderRepository
import com.aristidevs.cursotestingandroid.core.builders.cartItem
import com.aristidevs.cursotestingandroid.core.fakes.FakeCartItemRepository
import com.aristidevs.cursotestingandroid.core.fakes.FakeOrderRepository
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test

/**
 * EXAMEN — Tests UNITARIOS del caso de uso de realizar pedido.
 *
 * Completa cada test siguiendo Given-When-Then. No modifiques producción.
 * SUT: [PlaceOrderUseCase] (éxito vacía el carrito; fallo NO lo vacía).
 * Pista: necesitarás un fake de OrderRepository y FakeCartItemRepository.
 */
class PlaceOrderUseCaseTest {
    private fun useCase(
        orderRepository: OrderRepository = FakeOrderRepository(),
        cartItemRepository: CartItemRepository = FakeCartItemRepository()
    ) = PlaceOrderUseCase(
        orderRepository = orderRepository,
        cartItemRepository = cartItemRepository
    )

    @Test
    fun `given successful order when invoke then returns success and clears cart`() = runTest {
        // GIVEN
        val cartItem = cartItem {
            withProductId("p1")
            withQuantity(23)
        }
        val cartItemRepository = FakeCartItemRepository().apply {
            setCartItems(listOf(cartItem))
        }

        // WHEN
        val result = useCase(
            cartItemRepository = cartItemRepository
        ).invoke()

        // THEN
        assertTrue(result.isSuccess)
        assertTrue(cartItemRepository.getCartItems().first().isEmpty())
    }

    @Test
    fun `given repository throws when invoke then returns failure`() = runTest {
        // GIVEN
        val orderRepository = FakeOrderRepository().apply {
            throwExceptionWhenPlaceOrder = true
        }

        // WHEN
        val result = useCase(
            orderRepository = orderRepository
        ).invoke()

        // THEN
        assertTrue(result.isFailure)
    }

    @Test
    fun `given repository throws when invoke then does not clear cart`() = runTest {
        // GIVEN
        val cartItem = cartItem {
            withProductId("p1")
            withQuantity(23)
        }
        val cartItemsStart = listOf(cartItem)
        val cartItemRepository = FakeCartItemRepository().apply {
            setCartItems(cartItemsStart)
        }
        val orderRepository = FakeOrderRepository().apply {
            throwExceptionWhenPlaceOrder = true
        }

        // WHEN
        val result = useCase(
            cartItemRepository = cartItemRepository,
            orderRepository = orderRepository
        ).invoke()

        // THEN
        val cartItemsFinal = cartItemRepository.getCartItems().first()
        assertTrue(result.isFailure)
        assertTrue(cartItemsFinal.size == cartItemsStart.size)
        assertTrue(cartItemsFinal[0].productId == cartItemsStart[0].productId)
    }
}
