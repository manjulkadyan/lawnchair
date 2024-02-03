package app.lawnchair.smartspace

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.View.MeasureSpec.EXACTLY
import android.view.View.MeasureSpec.makeMeasureSpec
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.viewpager.widget.ViewPager
import app.lawnchair.smartspace.model.SmartspaceTarget
import app.lawnchair.smartspace.provider.SmartspaceProvider
import app.lawnchair.util.repeatOnAttached
import com.android.launcher3.R
import kotlin.math.roundToInt
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class BcSmartspaceView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    var previewMode: Boolean = false,
) : FrameLayout(context, attrs) {

    private val provider = SmartspaceProvider.INSTANCE.get(context)

    private lateinit var viewPager: ViewPager
    private lateinit var indicator: PageIndicator
    private val adapter = CardPagerAdapter(context)
    private var scrollState = ViewPager.SCROLL_STATE_IDLE
    private var pendingTargets: List<SmartspaceTarget>? = null
    private var runningAnimation: Animator? = null
    var parentPaddingTop = 0


    override fun onFinishInflate() {
        super.onFinishInflate()
        viewPager = findViewById(R.id.smartspace_card_pager)
        viewPager.isSaveEnabled = false
        indicator = findViewById(R.id.smartspace_page_indicator)

        viewPager.addOnPageChangeListener(
            object : ViewPager.OnPageChangeListener {
                override fun onPageScrolled(
                    position: Int,
                    positionOffset: Float,
                    positionOffsetPixels: Int,
                ) {
                    indicator.setPageOffset(position, positionOffset)
                }

                override fun onPageSelected(position: Int) {
                }

                override fun onPageScrollStateChanged(state: Int) {
                    scrollState = state
                    if (state == 0) {
                        pendingTargets?.let {
                            pendingTargets = null
                            onSmartspaceTargetsUpdate(it)
                        }
                    }
                }
            },
        )

        val targets = provider.previewTargets
        repeatOnAttached {
            viewPager.adapter = adapter
            targets
                .onEach(::onSmartspaceTargetsUpdate)
                .launchIn(this)
        }
    }


    override fun setOnLongClickListener(l: OnLongClickListener?) {
        viewPager.setOnLongClickListener(l)
    }

    private fun onSmartspaceTargetsUpdate(targets: List<SmartspaceTarget>) {
        if (adapter.count > 1 && scrollState != ViewPager.SCROLL_STATE_IDLE) {
            pendingTargets = targets
            return
        }

        val sortedTargets = targets.sortedByDescending { it.score }.toMutableList()
        val isRtl = layoutDirection == LAYOUT_DIRECTION_RTL
        val currentItem = viewPager.currentItem
        val index = if (isRtl) adapter.count - currentItem else currentItem
        if (isRtl) {
            sortedTargets.reverse()
        }

        val oldCard = adapter.getCardAtPosition(currentItem)
        adapter.setTargets(sortedTargets)
        val count = adapter.count
        if (isRtl) {
            viewPager.setCurrentItem((count - index).coerceIn(0 until count), false)
        }
        indicator.setNumPages(targets.size)
        oldCard?.let { animateSmartspaceUpdate(it) }
        adapter.notifyDataSetChanged()
    }

    private fun animateSmartspaceUpdate(oldCard: BcSmartspaceCard) {
        if (runningAnimation != null || oldCard.parent != null) return

        val animParent = viewPager.parent as ViewGroup
        oldCard.measure(makeMeasureSpec(viewPager.width, EXACTLY), makeMeasureSpec(viewPager.height, EXACTLY))
        oldCard.layout(viewPager.left, viewPager.top, viewPager.right, viewPager.bottom)
        val shift = resources.getDimension(R.dimen.enhanced_smartspace_dismiss_margin)
        val animator = AnimatorSet()
        animator.play(
            ObjectAnimator.ofFloat(
                oldCard,
                View.TRANSLATION_Y,
                0f,
                (-height).toFloat() - shift,
            ),
        )
        animator.play(ObjectAnimator.ofFloat(oldCard, View.ALPHA, 1f, 0f))
        animator.play(
            ObjectAnimator.ofFloat(
                viewPager,
                View.TRANSLATION_Y,
                height.toFloat() + shift,
                0f,
            ),
        )
        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator) {
                animParent.overlay.add(oldCard)
            }

            override fun onAnimationEnd(animation: Animator) {
                animParent.overlay.remove(oldCard)
                runningAnimation = null
            }
        })
        runningAnimation = animator
        animator.start()
    }
}
