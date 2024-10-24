package the.lyn.kotlinexposedmockingexample.service

import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.*
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.boot.test.context.SpringBootTest
import the.lyn.kotlinexposedmockingexample.entity.User
import javax.sql.DataSource

@SpringBootTest
class UserServiceRealDBTest(
    val dataSource: DataSource,
    val userExposedRepository: UserExposedRepository,
) : StringSpec({
    val userService = UserService(
        userExposedRepository
    )

    "Read DB Test" {
        userService.initData()

        userService.readId() shouldBe 1L
        userService.readName() shouldBe "Lyn"
        userService.incAndGetAge() shouldBe 19
    }
})


class UserCRUDServiceTest : StringSpec({
    "transaction() Required DB Connection" {
        val userExposedRepository = mockk<UserExposedRepository>()
        val userService = UserService(userExposedRepository)

        shouldThrowExactly<IllegalStateException> {
            userService.readId() shouldBe 1L
            userService.readName() shouldBe "Lyn"
            userService.incAndGetAge() shouldBe 19
        }
    }

    "MockTest, transaction() Mocking. But Entity Require DB Connection" {
        mockTransaction()

        val userExposedRepository = mockk<UserExposedRepository>()
        val userService = UserService(userExposedRepository)

        shouldThrowExactly<IllegalStateException> {
            every {
                userExposedRepository.getFirstUser()
            } returns User.new {
                userName = "Lyn"
                email = "unknown"
                age = 18
            }

            userService.readId() shouldBe 1L
            userService.readName() shouldBe "Lyn"
            userService.incAndGetAge() shouldBe 19
        }

        unMockTransaction()
    }

    "MockTest, Transaction Mocking. Entity Mocking" {
        mockTransaction()

        val mockUserId = mockk<EntityID<Long>>()
        every { mockUserId.value } returns 1L

        val mockUser = mockk<User>()
        every { mockUser.id } returns mockUserId
        every { mockUser.userName } returns "Lyn"
        every { mockUser.age } returns 18
        every { mockUser.age = any() } just runs

        val userExposedRepository = mockk<UserExposedRepository>()
        val userService = UserService(userExposedRepository)

        every { userExposedRepository.getFirstUser() } returns mockUser

        userService.readId() shouldBe 1L
        userService.readName() shouldBe "Lyn"
        userService.incAndGetAge() shouldBe 19
    }
})

private val TRANSACTION_CLASS_NAME = "org.jetbrains.exposed.sql.transactions.ThreadLocalTransactionManagerKt"

fun mockTransaction() {
    mockkStatic(TRANSACTION_CLASS_NAME)

    val transactionSlot = slot<Transaction.() -> Any>()
    every { transaction(any(), any(), any(), capture(transactionSlot)) } answers {
        transactionSlot.captured.invoke(mockk())
    }
}

fun unMockTransaction() {
    unmockkStatic(TRANSACTION_CLASS_NAME)
}