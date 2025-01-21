package io.github.ludorival.pactjvm.mock.test.orderservice

import io.github.ludorival.pactjvm.mock.test.shoppingservice.ShoppingList

data class OrderMessage(
    val shoppingListId: String,
    val userId: Long,
    val items: List<OrderItem>
) {
    data class OrderItem(
        val name: String,
        val quantity: Int
    )

    companion object {
        fun fromShoppingList(shoppingList: ShoppingList): OrderMessage {
            return OrderMessage(
                shoppingListId = shoppingList.id.toString(),
                userId = shoppingList.userId,
                items = shoppingList.items.map { item ->
                    OrderItem(
                        name = item.name,
                        quantity = item.quantity
                    )
                }
            )
        }
    }
} 