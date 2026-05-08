package com.example.basketballhelp

import android.app.Application
import com.example.basketballhelp.data.network.service.NetworkModule
import com.example.basketballhelp.data.repository.DrillRepositoryImpl
import com.example.basketballhelp.data.repository.GoalRepositoryImpl
import com.example.basketballhelp.data.repository.PlayerRepositoryImpl
import com.example.basketballhelp.data.repository.SessionRepositoryImpl
import com.example.basketballhelp.domain.repository.DrillRepository
import com.example.basketballhelp.domain.repository.GoalRepository
import com.example.basketballhelp.domain.repository.PlayerRepository
import com.example.basketballhelp.domain.repository.SessionRepository

class HoopDevApplication : Application() {
    private val api by lazy { NetworkModule.createApiService() }

    val playerRepository: PlayerRepository by lazy {
        PlayerRepositoryImpl(api)
    }

    val sessionRepository: SessionRepository by lazy {
        SessionRepositoryImpl(api)
    }

    val goalRepository: GoalRepository by lazy {
        GoalRepositoryImpl(api)
    }

    val drillRepository: DrillRepository by lazy {
        DrillRepositoryImpl(api)
    }
}
