package the.lyn.kotlinexposedmockingexample.service

import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Service
import the.lyn.kotlinexposedmockingexample.entity.User
import the.lyn.kotlinexposedmockingexample.entity.Users

@Service
class UserExposedRepository {
    fun initData() {
        SchemaUtils.create(Users)

        User.new {
            userName = "Lyn"
            email = "unknown@email.com"
            age = 18
        }
    }

    fun getFirstUser(): User {
        return Users.selectAll()
            .first()
            .let {
                User.wrapRow(it)
            }
    }
}

@Service
class UserService(
    private val repository: UserExposedRepository
) {
    fun initData() {
        transaction {
            repository.initData()
        }
    }

    fun readId(): Long {
        return transaction {
            repository.getFirstUser().id.value
        }
    }

    fun readName(): String {
        return transaction {
            repository.getFirstUser().userName
        }
    }

    fun incAndGetAge(): Int {
        return transaction {
            val user = repository.getFirstUser()
            val newAge = user.age + 1
            user.age = newAge
            newAge
        }
    }
}