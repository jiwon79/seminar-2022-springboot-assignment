package com.wafflestudio.seminar.core.user.database
import com.wafflestudio.seminar.common.BaseTimeEntity
import com.wafflestudio.seminar.core.seminar.database.UserSeminarEntity
import com.wafflestudio.seminar.core.user.domain.User
import javax.persistence.*

@Entity
class UserEntity(
    val username: String,
    val email: String,
    val password: String,
    
    @OneToOne(cascade = [CascadeType.ALL], optional = true)
    @JoinColumn(name = "participant_id")
    var participant: ParticipantEntity? = null,
    
    @OneToOne(cascade = [CascadeType.ALL], optional = true)
    @JoinColumn(name = "instructor_id")
    var instructor: InstructorEntity? = null,

    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL])
    val userSeminars: MutableList<UserSeminarEntity> = mutableListOf()
): BaseTimeEntity() {
    fun toUser(): User {
        return User(
            id = id,
            username = username,
            email = email,
            password = password,
            participant = userSeminars
                .filter { it.role == "participant" }
                .map { it.toParticipant() }
                .firstOrNull(),
            instructor = userSeminars
                .filter { it.role == "instructor" }
                .map { it.toInstructor() }
                .firstOrNull(),
        )
    }
}