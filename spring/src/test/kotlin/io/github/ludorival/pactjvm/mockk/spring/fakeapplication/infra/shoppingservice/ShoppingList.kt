package io.github.ludorival.pactjvm.mockk.spring.fakeapplication.infra.shoppingservice

import java.time.LocalDate

data class ShoppingList(
    val id: Long,
    val title: String,
    val userId: Long,
    val items: List<Item>,
    val createdAt: LocalDate = LocalDate.now()
) {

    data class Item(val id: Long, val name: String, val quantity: Int)
}
