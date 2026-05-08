package com.example.basketballhelp.domain.usecase

import com.example.basketballhelp.ui.screen.log.SessionFormState

object SessionValidation {
    fun validate(form: SessionFormState): String? {
        if (form.sessionDate.isBlank()) return "Session date is required."
        if (form.freeThrowsMade > form.freeThrowsAttempted) return "Free throws made cannot exceed attempts."
        if (form.spotShootingMade > form.spotShootingAttempted) return "Spot shooting made cannot exceed attempts."
        if (form.closeRangeMade > form.closeRangeAttempted) return "Close range made cannot exceed attempts."
        return null
    }
}
