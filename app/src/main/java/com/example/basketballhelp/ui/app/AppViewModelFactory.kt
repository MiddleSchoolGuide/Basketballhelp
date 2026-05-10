package com.example.basketballhelp.ui.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.basketballhelp.HoopDevApplication
import com.example.basketballhelp.ui.screen.dashboard.DashboardViewModel
import com.example.basketballhelp.ui.screen.drills.DrillsViewModel
import com.example.basketballhelp.ui.screen.history.EditSessionViewModel
import com.example.basketballhelp.ui.screen.history.HistoryViewModel
import com.example.basketballhelp.ui.screen.log.LogSessionViewModel
import com.example.basketballhelp.ui.screen.profile.ProfileViewModel

class AppViewModelFactory(
    private val app: HoopDevApplication,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(DashboardViewModel::class.java) ->
                DashboardViewModel(app.playerRepository, app.sessionRepository, app.goalRepository, app.aiCoachingRepository) as T
            modelClass.isAssignableFrom(LogSessionViewModel::class.java) ->
                LogSessionViewModel(app.playerRepository, app.sessionRepository) as T
            modelClass.isAssignableFrom(HistoryViewModel::class.java) ->
                HistoryViewModel(app.playerRepository, app.sessionRepository) as T
            modelClass.isAssignableFrom(EditSessionViewModel::class.java) ->
                EditSessionViewModel(app.sessionRepository) as T
            modelClass.isAssignableFrom(DrillsViewModel::class.java) ->
                DrillsViewModel(app.drillRepository) as T
            modelClass.isAssignableFrom(ProfileViewModel::class.java) ->
                ProfileViewModel(app.playerRepository, app.sessionRepository, app.goalRepository) as T
            else -> error("Unknown ViewModel: ${modelClass.name}")
        }
    }
}
