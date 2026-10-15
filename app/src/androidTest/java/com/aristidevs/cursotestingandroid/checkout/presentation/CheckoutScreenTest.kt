package com.aristidevs.cursotestingandroid.checkout.presentation

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.aristidevs.cursotestingandroid.checkout.domain.model.OrderConfirmation
import com.aristidevs.cursotestingandroid.core.builders.cartSummary
import com.aristidevs.cursotestingandroid.core.presentation.testing.UiTestTag.CHECKOUT_CONFIRM_BUTTON
import com.aristidevs.cursotestingandroid.core.presentation.testing.UiTestTag.CHECKOUT_LOADING
import com.aristidevs.cursotestingandroid.core.presentation.testing.UiTestTag.CHECKOUT_RETRY_BUTTON
import com.aristidevs.cursotestingandroid.core.presentation.testing.UiTestTag.checkoutConfirmationOrder
import com.aristidevs.cursotestingandroid.core.presentation.testing.UiTestTag.checkoutConfirmationTime
import com.aristidevs.cursotestingandroid.core.presentation.testing.UiTestTag.checkoutConfirmationTotal
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertTrue

/**
 * EXAMEN — Tests de UI (Compose) de la pantalla de checkout.
 *
 * Completa cada test siguiendo Given-When-Then. No modifiques producción.
 * SUT: composables de [CheckoutScreen] / CheckoutContent renderizando cada [CheckoutUiState].
 * Pistas: usa `composeRule.setContent { ... }` pasando el estado deseado y callbacks de prueba;
 * localiza nodos por texto (la pantalla aún no expone testTags) y verifica habilitación del botón.
 */
class CheckoutScreenTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    fun createCheckoutScreen(
        uiState: CheckoutUiState,
        onBack: () -> Unit = {},
        onRetry: () -> Unit = {},
        onNameChange: (String) -> Unit = {},
        onEmailChange: (String) -> Unit = {},
        onAddressChange: (String) -> Unit = {},
        onConfirm: () -> Unit = {},
    ) {
        composeRule.setContent {
            CheckoutContent(
                uiState = uiState,
                onBack = onBack,
                onRetry = onRetry,
                onNameChange = onNameChange,
                onEmailChange = onEmailChange,
                onAddressChange = onAddressChange,
                onConfirm = onConfirm,
            )
        }
    }

    @Test
    fun givenLoadingState_whenRendered_thenShowsProgress() {
        // GIVEN
        createCheckoutScreen(uiState = CheckoutUiState.Loading)

        // THEN
        composeRule.onNodeWithTag(CHECKOUT_LOADING).assertIsDisplayed()
    }

    @Test
    fun givenIdleStateWithEmptyCart_whenRendered_thenConfirmButtonDisabled() {
        // GIVEN
        createCheckoutScreen(
            uiState = CheckoutUiState.Idle(
                summary = cartSummary {},
                form = CheckoutForm(),
                errors = CheckoutFormErrors(),
                isCartEmpty = true,
                isSubmitting = false,
                canSubmit = false,
            ),
        )

        // THEN
        composeRule.onNodeWithTag(CHECKOUT_CONFIRM_BUTTON).assertIsDisplayed()
        composeRule.onNodeWithTag(CHECKOUT_CONFIRM_BUTTON).assertIsNotEnabled()
    }

    @Test
    fun givenIdleStateWithValidForm_whenRendered_thenConfirmButtonEnabled() {
        // GIVEN
        createCheckoutScreen(
            uiState = CheckoutUiState.Idle(
                summary = cartSummary {},
                form = CheckoutForm(),
                errors = CheckoutFormErrors(),
                isCartEmpty = true,
                isSubmitting = false,
                canSubmit = true,
            ),
        )

        // THEN
        composeRule.onNodeWithTag(CHECKOUT_CONFIRM_BUTTON).assertIsDisplayed()
        composeRule.onNodeWithTag(CHECKOUT_CONFIRM_BUTTON).assertIsEnabled()
    }

    @Test
    fun givenIdleState_whenTypingInvalidEmail_thenConfirmButtonDisabled() {
        /*
           no acabo de entender este caso de test,
           ya que el botón confirm solo depende del valor de canSubmit me parece que el caso sería
           el mismo que el segundo test.
           La actualización de si se puede o no hacer el submit depende del viewmodel
           y se debe testear ahí.
           ¿NO SERÍA ESTO UN CASO TRAMPA? :D
         */
    }

    @Test
    fun givenSuccessState_whenRendered_thenShowsOrderConfirmation() {
        // GIVEN
        createCheckoutScreen(
            uiState = CheckoutUiState.Success(
                confirmation = OrderConfirmation(
                    orderId = "order-id-10",
                    etaMinutes = 12,
                    total = 54.3,
                ),
            ),
        )

        // THEN
        composeRule.onNodeWithTag(checkoutConfirmationOrder("order-id-10")).assertIsDisplayed()
        composeRule.onNodeWithTag(checkoutConfirmationTime(12)).assertIsDisplayed()
        composeRule.onNodeWithTag(checkoutConfirmationTotal(54.3)).assertIsDisplayed()
    }

    @Test
    fun givenErrorState_whenRetryClicked_thenInvokesRetryCallback() {
        // GIVEN
        var retryClicked = false
        createCheckoutScreen(
            uiState = CheckoutUiState.Error(message = "catastrophic error"),
            onRetry = { retryClicked = true },
        )
        // WHEN
        composeRule.onNodeWithTag(CHECKOUT_RETRY_BUTTON).performClick()

        // THEN
        assertTrue(retryClicked)
    }
}
