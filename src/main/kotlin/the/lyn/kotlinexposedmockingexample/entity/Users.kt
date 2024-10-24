package the.lyn.kotlinexposedmockingexample.entity

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable

object Users : LongIdTable(name = "users") {
    val userName = varchar("userName", 50)
    val email = varchar("email", 50)
    val age = integer("age")
}

class User(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<User>(Users)

    var userName by Users.userName
    var email by Users.email
    var age by Users.age
}