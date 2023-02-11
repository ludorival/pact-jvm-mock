package io.github.ludorival.pactjvm.mockk.fakeapplication.infra

import io.github.ludorival.pactjvm.mockk.fakeapplication.infra.shoppingservice.ShoppingList
import io.github.ludorival.pactjvm.mockk.fakeapplication.infra.shoppingservice.ShoppingServiceClient
import io.github.ludorival.pactjvm.mockk.fakeapplication.infra.userservice.UserServiceClient

class Scenarios(private val userServiceClient: UserServiceClient, private val shoppingServiceClient: ShoppingServiceClient) {


    fun createAShoppingList(userid: Long): ShoppingList {

        val shoppingList = shoppingServiceClient.createShoppingList(userid, "My weekly shopping list")
        return shoppingList

    }

    fun getCurrentShoppingList(userid: Long): ShoppingList {
        val userProfile = userServiceClient.getUserProfile(userid)
        return shoppingServiceClient.getShoppingList(userid, userProfile.userPreferences.preferredShoppingListId)
    }

}
