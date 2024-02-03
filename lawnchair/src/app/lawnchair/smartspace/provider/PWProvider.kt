package app.lawnchair.smartspace.provider

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.drawable.Icon
import android.os.BatteryManager
import androidx.core.content.getSystemService
import app.lawnchair.HeadlessWidgetsManager
import app.lawnchair.smartspace.model.SmartspaceAction
import app.lawnchair.smartspace.model.SmartspaceScores
import app.lawnchair.smartspace.model.SmartspaceTarget
import app.lawnchair.util.broadcastReceiverFlow
import app.lawnchair.util.formatShortElapsedTimeRoundingUpToMinutes
import com.android.launcher3.R
import com.android.launcher3.Utilities
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

class PWProvider(context: Context) : SmartspaceDataSource(
    context,
    R.string.smartspace_pw_status,
    { smartspacePWStatus },
) {
    override val internalTargets: Flow<List<SmartspaceTarget>>

    init {
        val target = SmartspaceTarget(
            id = "pwStatus",
            headerAction = SmartspaceAction(
                id = "pwStatusAction",
                icon = Icon.createWithResource(context, R.drawable.ic_recent),
                title = "PW walla this is a long string \n which wafeg eafug ea iuefh ",
                subtitle = "Subtitle this is b ahdhf h gsfig  euiwfg  sfg shfb shdfb shdf h jhv hjhg",
            ),
            score = 100f,
            featureType = SmartspaceTarget.FeatureType.FEATURE_PW,
        )
        internalTargets = flowOf(listOfNotNull(target))
    }
}
