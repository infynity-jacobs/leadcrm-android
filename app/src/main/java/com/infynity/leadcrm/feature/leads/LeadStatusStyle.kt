package com.infynity.leadcrm.feature.leads

import androidx.compose.ui.graphics.Color
import com.infynity.leadcrm.core.theme.LeadContacted
import com.infynity.leadcrm.core.theme.LeadConverted
import com.infynity.leadcrm.core.theme.LeadFollowUp
import com.infynity.leadcrm.core.theme.LeadInterested
import com.infynity.leadcrm.core.theme.LeadLost
import com.infynity.leadcrm.core.theme.LeadNew

data class LeadStatusStyle(
    val background: Color,
    val foreground: Color
)

fun leadStatusStyle(
    status: String
): LeadStatusStyle {

    return when (
        status.lowercase()
            .replace("_", "")
    ) {

        "new" -> LeadStatusStyle(
            LeadNew.copy(alpha = 0.15f),
            LeadNew
        )

        "contacted" -> LeadStatusStyle(
            LeadContacted.copy(alpha = 0.15f),
            LeadContacted
        )

        "followup" -> LeadStatusStyle(
            LeadFollowUp.copy(alpha = 0.15f),
            LeadFollowUp
        )

        "interested" -> LeadStatusStyle(
            LeadInterested.copy(alpha = 0.15f),
            LeadInterested
        )

        "converted" -> LeadStatusStyle(
            LeadConverted.copy(alpha = 0.15f),
            LeadConverted
        )

        "lost" -> LeadStatusStyle(
            LeadLost.copy(alpha = 0.15f),
            LeadLost
        )

        else -> LeadStatusStyle(
            LeadLost.copy(alpha = 0.15f),
            LeadLost
        )
    }
}
