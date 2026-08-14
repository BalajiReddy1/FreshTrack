package com.example.freshtrack.data.repository

import com.example.freshtrack.data.local.dao.ProductDao
import com.example.freshtrack.data.local.entities.ProductEntity
import com.example.freshtrack.data.session.UserSession
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Covers partial consume/discard: using part of a multi-unit item must produce a
 * resolved row so it shows in History and counts in Impact, rather than silently
 * decrementing the quantity.
 */
class ProductRepositoryImplTest {

    private val dao: ProductDao = mockk(relaxed = true)
    private val session: UserSession = mockk()
    private lateinit var repo: ProductRepositoryImpl

    private val pantryId = "personal-alice"

    private fun entity(quantity: Int) = ProductEntity(
        id = "milk",
        pantryId = pantryId,
        userId = "alice",
        name = "Milk",
        category = "Dairy",
        expiryDate = 999,
        quantity = quantity,
        originalQuantity = quantity
    )

    @Before
    fun setUp() {
        every { session.activePantryId() } returns pantryId
        every { session.currentUserId() } returns "alice"
        repo = ProductRepositoryImpl(dao, session)
    }

    @Test
    fun `consuming part splits off a resolved row and reduces the original`() = runTest {
        coEvery { dao.getProductByIdOnce(pantryId, "milk") } returns entity(quantity = 2)

        val inserted = slot<ProductEntity>()
        val updated = slot<ProductEntity>()
        coEvery { dao.insertProduct(capture(inserted)) } returns Unit
        coEvery { dao.updateProduct(capture(updated)) } returns Unit

        repo.consumeUnits("milk", amount = 1)

        // The split-off row is a distinct consumed record of 1 unit.
        assertTrue(inserted.captured.isConsumed)
        assertFalse(inserted.captured.isDiscarded)
        assertEquals(1, inserted.captured.quantity)
        assertTrue("must be a new row, not the original", inserted.captured.id != "milk")
        assertTrue(inserted.captured.resolvedDate != null)

        // The original keeps the remaining unit and stays active.
        assertEquals("milk", updated.captured.id)
        assertEquals(1, updated.captured.quantity)
        assertFalse(updated.captured.isConsumed)

        // Not a whole-item resolution.
        coVerify(exactly = 0) { dao.markAsConsumed(any(), any(), any()) }
    }

    @Test
    fun `consuming the whole item marks it consumed without splitting`() = runTest {
        coEvery { dao.getProductByIdOnce(pantryId, "milk") } returns entity(quantity = 2)

        repo.consumeUnits("milk", amount = 2)

        coVerify { dao.markAsConsumed(pantryId, "milk", any()) }
        coVerify(exactly = 0) { dao.insertProduct(any()) }
    }

    @Test
    fun `discarding part splits off a discarded row`() = runTest {
        coEvery { dao.getProductByIdOnce(pantryId, "milk") } returns entity(quantity = 3)

        val inserted = slot<ProductEntity>()
        coEvery { dao.insertProduct(capture(inserted)) } returns Unit

        repo.discardUnits("milk", amount = 1)

        assertTrue(inserted.captured.isDiscarded)
        assertFalse(inserted.captured.isConsumed)
        assertEquals(1, inserted.captured.quantity)
    }

    @Test
    fun `resolving more than present marks the whole item, never negative`() = runTest {
        coEvery { dao.getProductByIdOnce(pantryId, "milk") } returns entity(quantity = 1)

        repo.consumeUnits("milk", amount = 5)

        coVerify { dao.markAsConsumed(pantryId, "milk", any()) }
        coVerify(exactly = 0) { dao.insertProduct(any()) }
    }
}
