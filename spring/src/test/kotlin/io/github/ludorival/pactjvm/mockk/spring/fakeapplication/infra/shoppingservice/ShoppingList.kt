package io.github.ludorival.pactjvm.mockk.spring.fakeapplication.infra.shoppingservice

data class ShoppingList(val id: Long, val title: String, val userId: Long, val items: List<Item>) {

    data class Item(val id: Long, val name: String, val quantity: Int)
}
