package phoenix.service

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import phoenix.entity.UserTable
import phoenix.models.User
import phoenix.models.UserRegistrationRequest
import java.security.MessageDigest
import java.time.LocalDateTime

class UserService {

    fun createUser(request: UserRegistrationRequest): User? {
        return transaction {
            // Проверяем, что пользователь с таким username или email не существует
            val existingUser = UserTable.select {
                (UserTable.username eq request.username) or (UserTable.email eq request.email)
            }.singleOrNull()

            if (existingUser != null) {
                return@transaction null // Пользователь уже существует
            }

            val passwordHash = hashPassword(request.password)
            val now = LocalDateTime.now()

            val userId = UserTable.insertAndGetId {
                it[UserTable.username] = request.username
                it[UserTable.email] = request.email
                it[UserTable.passwordHash] = passwordHash
                if (request.fullName != null) {
                    it[UserTable.fullName] = request.fullName
                }
                it[UserTable.createdAt] = now
                it[UserTable.updatedAt] = now
                it[UserTable.isActive] = true
            }

            User(
                id = userId.value,
                username = request.username,
                email = request.email,
                fullName = request.fullName,
                isActive = true
            )
        }
    }

    fun authenticateUser(username: String, password: String): User? {
        return transaction {
            val userRow = UserTable.select {
                (UserTable.username eq username) and (UserTable.isActive eq true)
            }.singleOrNull()

            if (userRow != null && verifyPassword(password, userRow[UserTable.passwordHash])) {
                User(
                    id = userRow[UserTable.id].value,
                    username = userRow[UserTable.username],
                    email = userRow[UserTable.email],
                    fullName = userRow[UserTable.fullName],
                    isActive = userRow[UserTable.isActive]
                )
            } else null
        }
    }

    fun getUserById(userId: Int): User? {
        return transaction {
            UserTable.select {
                (UserTable.id eq userId) and (UserTable.isActive eq true)
            }.singleOrNull()?.let { row ->
                User(
                    id = row[UserTable.id].value,
                    username = row[UserTable.username],
                    email = row[UserTable.email],
                    fullName = row[UserTable.fullName],
                    isActive = row[UserTable.isActive]
                )
            }
        }
    }

    fun getUserByUsername(username: String): User? {
        return transaction {
            UserTable.select {
                (UserTable.username eq username) and (UserTable.isActive eq true)
            }.singleOrNull()?.let { row ->
                User(
                    id = row[UserTable.id].value,
                    username = row[UserTable.username],
                    email = row[UserTable.email],
                    fullName = row[UserTable.fullName],
                    isActive = row[UserTable.isActive]
                )
            }
        }
    }

    private fun hashPassword(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(password.toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    private fun verifyPassword(password: String, hash: String): Boolean {
        return hashPassword(password) == hash
    }
}
