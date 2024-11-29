package io.github.ludorival.pactjvm.mockk.spring.providers.shoppingservice

import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import java.util.concurrent.ConcurrentHashMap
import java.time.LocalDate

@RestController
@RequestMapping("/shopping-service")
open class ShoppingServiceRestController {
    private val logger = org.slf4j.LoggerFactory.getLogger(ShoppingServiceRestController::class.java)

    // In-memory storage
    private val shoppingLists = ConcurrentHashMap<Long, MutableList<ShoppingList>>()
    
    private val items = ConcurrentHashMap<Long, ShoppingList.Item>()


    @PostMapping("/user/{userId}")
    open fun createShoppingList(
        @PathVariable userId: Long,
        @RequestBody request: CreateShoppingListRequest
    ): ResponseEntity<ShoppingList> {
        logger.info("Creating shopping list for user: $userId")
        if (!isValidTitle(request.title)) {
            throw IllegalArgumentException("The title contains unexpected character")
        }

        val shoppingList = ShoppingList(
            id = (shoppingLists[userId]?.size?.plus(1) ?: 1).toLong(),
            title = request.title,
            userId = userId,
            items = emptyList(),
            createdAt = LocalDate.now()
        )

        shoppingLists.computeIfAbsent(userId) { mutableListOf() }
            .add(shoppingList)

        return ResponseEntity.ok(shoppingList)
    }

    @GetMapping("/user/{userId}")
    open fun listShoppingLists(
        @PathVariable userId: Long,
        @RequestParam limit: Int?,
        @RequestHeader("Authorization") authorization: String
    ): ResponseEntity<List<ShoppingList>> {
        logger.info("Listing shopping lists for user: $userId -> ${shoppingLists[userId]}")
        if (!authorization.startsWith("Bearer ")) {
            return ResponseEntity.status(401).build()
        }

        return ResponseEntity.ok(shoppingLists[userId] ?: emptyList())
    }

    @GetMapping("/user/{userId}/list/{listId}")
    open fun getShoppingList(
        @PathVariable userId: Long,
        @PathVariable listId: Long
    ): ResponseEntity<ShoppingList> {
        logger.info("Fetching shopping list $listId for user: $userId")
        val userLists = shoppingLists[userId]
        val list = userLists?.find { it.id == listId }
        return if (list != null) {
            ResponseEntity.ok(list)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @PutMapping("/user/{userId}/list/{listId}")
    open fun updateShoppingList(
        @PathVariable userId: Long,
        @PathVariable listId: Long,
        @RequestBody request: UpdateShoppingListRequest
    ): ResponseEntity<Void> {
        logger.info("Updating shopping list $listId for user: $userId")
        val userLists = shoppingLists[userId]
        userLists?.let { lists ->
            val index = lists.indexOfFirst { it.id == listId }
            if (index != -1) {
                lists[index] = lists[index].copy(title = request.name)
            }
        }
        return ResponseEntity.ok().build()
    }

    @DeleteMapping("/user/{userId}/list/{listId}")
    open fun deleteShoppingList(
        @PathVariable userId: Long,
        @PathVariable listId: Long
    ): ResponseEntity<Void> {
        logger.info("Deleting shopping list $listId for user: $userId")
        shoppingLists[userId]?.removeIf { it.id == listId }
        return ResponseEntity.ok().build()
    }

    @PatchMapping("/user/{userId}/list/{listId}")
    open fun patchShoppingItem(
        @PathVariable userId: Long,
        @PathVariable listId: Long,
        @RequestBody item: ShoppingList.Item
    ): ResponseEntity<ShoppingList.Item> {
        logger.info("Patching item ${item.id} in list $listId for user: $userId")
        val userLists = shoppingLists[userId]
        val list = userLists?.find { it.id == listId }
        list?.let { currentList ->
            val updatedItems = currentList.items.map { 
                if (it.id == item.id) item else it 
            }
            val updatedList = currentList.copy(items = updatedItems)
            userLists[userLists.indexOf(currentList)] = updatedList
        }
        return ResponseEntity.ok(item)
    }

    fun clearAll() {
        shoppingLists.clear()
    }

    fun putShoppingList(shoppingList: ShoppingList) {
        shoppingLists.computeIfAbsent(shoppingList.userId) { mutableListOf() }
            .add(shoppingList)
    }   

    private fun isValidTitle(title: String): Boolean {
        return title.matches(Regex("^[a-zA-Z0-9\\s-_]+$"))
    }
}

data class CreateShoppingListRequest(val title: String = "")
data class UpdateShoppingListRequest(val name: String = "")

@ControllerAdvice
class GlobalExceptionHandler {
    
    @ExceptionHandler(IllegalArgumentException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    fun handleValidationException(ex: IllegalArgumentException): ResponseEntity<String> {
        ex.printStackTrace()
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .contentType(MediaType.TEXT_PLAIN)
            .body(ex.message ?: "An error occurred")
    }
}
