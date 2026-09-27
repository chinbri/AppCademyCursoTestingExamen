package com.aristidevs.cursotestingandroid.checkout.presentation

import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test

/**
 * EXAMEN — Tests UNITARIOS de la validación del formulario de checkout.
 *
 * Completa cada test siguiendo Given-When-Then. No modifiques producción.
 * SUT: [CheckoutForm.validate], [CheckoutFormErrors.isValid], [FieldError].
 */
class CheckoutFormTest {

    val validCheckoutForm = CheckoutForm(
        name = "John Smith",
        address = "C/ Mayor 3",
        email = "my.valid.email@myemail.org"
    )

    @Test
    fun `given blank name when validate then nameError is REQUIRED`() {
        // GIVEN
        val checkoutForm = validCheckoutForm.copy(name = "")

        // WHEN
        val result = checkoutForm.validate()

        // THEN
        assertFalse(result.isValid)
        assertTrue(result.addressError == null)
        assertTrue(result.emailError == null)
        assertTrue(result.nameError == FieldError.REQUIRED)
    }

    @Test
    fun `given blank address when validate then addressError is REQUIRED`() {
        // GIVEN
        val checkoutForm = validCheckoutForm.copy(address = "")

        // WHEN
        val result = checkoutForm.validate()

        // THEN
        assertFalse(result.isValid)
        assertTrue(result.nameError == null)
        assertTrue(result.emailError == null)
        assertTrue(result.addressError == FieldError.REQUIRED)
    }

    @Test
    fun `given blank email when validate then emailError is REQUIRED`() {
        // GIVEN
        val checkoutForm = validCheckoutForm.copy(email = "")

        // WHEN
        val result = checkoutForm.validate()

        // THEN
        assertFalse(result.isValid)
        assertTrue(result.addressError == null)
        assertTrue(result.nameError == null)
        assertTrue(result.emailError == FieldError.REQUIRED)
    }

    @Test
    fun `given malformed email when validate then emailError is INVALID_EMAIL`() {
        // GIVEN
        val checkoutForm = validCheckoutForm.copy(email = "bad_email")

        // WHEN
        val result = checkoutForm.validate()

        // THEN
        assertFalse(result.isValid)
        assertTrue(result.addressError == null)
        assertTrue(result.nameError == null)
        assertTrue(result.emailError == FieldError.INVALID_EMAIL)
    }

    @Test
    fun `given all fields valid when validate then errors isValid is true`() {
        // GIVEN
        val checkoutForm = validCheckoutForm

        // WHEN
        val result = checkoutForm.validate()

        // THEN
        assertTrue(result.isValid)
        assertTrue(result.addressError == null)
        assertTrue(result.nameError == null)
        assertTrue(result.emailError == null)
    }
}
