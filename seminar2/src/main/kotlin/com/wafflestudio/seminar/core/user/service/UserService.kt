package com.wafflestudio.seminar.core.user.service

import com.wafflestudio.seminar.common.Seminar400
import com.wafflestudio.seminar.common.Seminar404
import com.wafflestudio.seminar.core.user.api.request.CreateInstructorDTO
import com.wafflestudio.seminar.core.user.api.request.CreateParticipantDTO
import com.wafflestudio.seminar.core.user.api.request.SignInRequest
import com.wafflestudio.seminar.core.user.api.request.SignUpRequest
import com.wafflestudio.seminar.core.user.database.UserEntity
import com.wafflestudio.seminar.core.user.database.UserRepository
import com.wafflestudio.seminar.core.user.domain.User
import org.springframework.stereotype.Service

interface UserService {
    fun getUser(id: Long): User
    fun createUser(user: SignUpRequest): AuthToken
    fun loginUser(user: SignInRequest): AuthToken
}

@Service
class UserServiceImpl(
    private val userRepository: UserRepository,
    private val participantService: ParticipantService,
    private val instructorService: InstructorService,
    private val authTokenService: AuthTokenService,
): UserService {
    override fun getUser(id: Long): User {
        val entity = userRepository.findById(id)
        if (entity.isEmpty) throw Seminar404("해당 id로 유저를 찾을 수 없습니다.")
        
        return User(entity.get())
    }

    override fun createUser(user: SignUpRequest): AuthToken {
        val entityByEmail = userRepository.findByEmail(user.email)
        if (entityByEmail.isPresent) throw Seminar404("이미 존재하는 이메일입니다.")
        var newUser = UserEntity(
            username = user.username,
            email = user.email,
            password = user.password,
        )

        if (user.role == "participant") {
            if (user.participant == null) throw Seminar400("수강생 프로필 정보를 입력해주세요.")
            
            val createParticipantDTO = CreateParticipantDTO(
                university = user.participant.university,
                is_registered = user.participant.is_registered,
            )
            val newParticipant = participantService.createParticipant(createParticipantDTO)
            newUser.participant = newParticipant
        } else if (user.role == "instructor") {
            if (user.instructor?.year != null && user.instructor.year <= 0) {
                throw Seminar400("회사의 경력은 자연수가 되어야 합니다.")
            }
            
            val createInstructorDTO = CreateInstructorDTO(
                company = user.instructor?.company ?: "",
                year = user.instructor?.year,
            )
            val newInstructor = instructorService.createInstructor(createInstructorDTO)
            newUser.instructor = newInstructor
        } else {
            throw Seminar400("잘못된 role 입니다.")
        }
        
        val entity = userRepository.save(newUser)
        return authTokenService.generateTokenByUsername(entity.id)
    }
    
    override fun loginUser(user: SignInRequest): AuthToken {
        val entity = userRepository.findByEmail(user.email)
        if (entity.isEmpty) throw Seminar400("해당 email의 유저가 없습니다.")
        if (entity.get().password != user.password) throw Seminar400("비밀번호가 틀렸습니다.")
        
        return authTokenService.generateTokenByUsername(entity.get().id)
    }
    
    private fun User(entity: UserEntity) = entity.run {
        User(id, username, email, password, participant, instructor)
    }
}