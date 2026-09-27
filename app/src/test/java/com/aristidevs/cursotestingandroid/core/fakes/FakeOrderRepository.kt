package com.aristidevs.cursotestingandroid.core.fakes

import com.aristidevs.cursotestingandroid.checkout.domain.model.OrderConfirmation
import com.aristidevs.cursotestingandroid.checkout.domain.repository.OrderRepository
import java.io.IOException

class FakeOrderRepository: OrderRepository {

    private var orderConfirmation: OrderConfirmation = OrderConfirmation(
        "OId-1",
        14,
        35.3
    )

    var throwExceptionWhenPlaceOrder: Boolean = false

    fun setOrderConfirmation(orderConfirmation: OrderConfirmation){
        this.orderConfirmation = orderConfirmation
    }

    override suspend fun placeOrder(): OrderConfirmation =
        if(throwExceptionWhenPlaceOrder) throw IOException() else orderConfirmation

}
