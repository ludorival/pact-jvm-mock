package io.github.ludorival.pactjvm.mockk.spring.fakeapplication.infra.userservice

data class UserProfile(val id: Long, val name: String, val email: String, val userPreferences: UserPreferences)
