package app.lawnchair.smartspace

import android.content.Context
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import app.lawnchair.launcher
import app.lawnchair.ui.preferences.PreferenceActivity
import app.lawnchair.ui.preferences.Routes
import com.android.launcher3.CheckLongPressHelper
import com.android.launcher3.R
import com.android.launcher3.logging.StatsLogManager
import com.android.launcher3.views.OptionsPopupView

class SmartspaceViewContainer @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    private val previewMode: Boolean = false,
) : FrameLayout(context, attrs) {

    private val longPressHelper = CheckLongPressHelper(this) { performLongClick() }
    private val smartspaceView: View

    init {
        val inflater = LayoutInflater.from(context)
        smartspaceView = inflater.inflate(R.layout.smartspace_enhanced, this, false) as BcSmartspaceView
        smartspaceView.previewMode = previewMode
        setOnLongClickListener {
            openOptions()
            true
        }
        addView(smartspaceView)
        Log.d(
            "Manjul",
            "init called with:  padding = $paddingTop",
        )
    }

    override fun setPadding(left: Int, top: Int, right: Int, bottom: Int) {
        super.setPadding(left, 0, right, bottom)
        Log.d(
            "Manjul",
            "setPadding() called with: left = $left, top = $top, right = $right, bottom = $bottom",
        )
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        Log.d("Manjul", "onAttachedToWindow() called , paddingTop = $paddingTop")
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        Log.d(
            "Manjul",
            "onMeasure() called with: widthMeasureSpec = $widthMeasureSpec, heightMeasureSpec = $heightMeasureSpec, paddingTop = $paddingTop",
        )
    }

    private fun openOptions() {
        if (previewMode) return

        val launcher = context.launcher
        val pos = Rect()
        launcher.dragLayer.getDescendantRectRelativeToSelf(smartspaceView, pos)
        OptionsPopupView.show(launcher, RectF(pos), listOf(getCustomizeOption()), true)
    }

    private fun getCustomizeOption() = OptionsPopupView.OptionItem(
        context,
        R.string.customize_button_text,
        R.drawable.ic_setting,
        StatsLogManager.LauncherEvent.IGNORE,
    ) {
        context.startActivity(PreferenceActivity.createIntent(context, "/${Routes.SMARTSPACE}/"))
        true
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        longPressHelper.onTouchEvent(ev)
        return longPressHelper.hasPerformedLongPress()
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        longPressHelper.onTouchEvent(ev)
        return true
    }

    override fun cancelLongPress() {
        super.cancelLongPress()
        longPressHelper.cancelLongPress()
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        Log.d(
            "Manjul",
            "onLayout() called with: changed = $changed, padding = $paddingTop",
        )
    }
}
