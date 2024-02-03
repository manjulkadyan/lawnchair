package app.lawnchair.smartspace

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import app.lawnchair.smartspace.model.SmartspaceAction
import app.lawnchair.util.broadcastReceiverFlow
import app.lawnchair.util.repeatOnAttached
import com.android.launcher3.R
import java.util.Calendar
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class PWView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : LinearLayout(context, attrs) {

    private var dateView2: IcuDateTextView? = null
    private val quotes = arrayOf(
        "The greatest glory in living lies not in never falling, but in rising every time we fall.",
        "The way to get started is to quit talking and begin doing. - Walt Disney",
        "Life is what happens when you're busy making other plans. - John Lennon",
        "Quote4",
        "Quote5",
        "Quote6",
        "Quote7",
        // Add more quotes as needed
    )
    private var quotesView: TextView? = null
    private val updateHandler = Handler(Looper.getMainLooper())

    init {
        repeatOnAttached {
            inflate(context, R.layout.smartspace_pw_layout, this@PWView)
            dateView2 = findViewById(R.id.date_pw)
            quotesView = findViewById<TextView>(R.id.txtQuote)
            updateUI()
            setupBroadcastReceiver()
            startQuoteUpdates()
        }
    }

    private fun setupBroadcastReceiver() {
        repeatOnAttached {
            val intentFilter = IntentFilter().apply {
                addAction(Intent.ACTION_TIME_CHANGED)
                addAction(Intent.ACTION_TIMEZONE_CHANGED)
            }
            broadcastReceiverFlow(context, intentFilter)
                .onEach { updateUI() }
                .launchIn(CoroutineScope(Dispatchers.Main))
        }
    }

    private fun updateUI() {
        updateQuote()
        populateDaysLayout()
    }

    private fun startQuoteUpdates() {
        updateQuotesRunnable.run()
    }

    private fun populateDaysLayout() {
        val days = arrayOf("M", "T", "W", "Th", "F", "Sa", "Su")
        val daysLayout = findViewById<LinearLayout>(R.id.week_layout)
        val calendar = Calendar.getInstance()
        val currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val mapDaysToCalendar =
            mapOf(2 to "M", 3 to "T", 4 to "W", 5 to "Th", 6 to "F", 7 to "Sa", 1 to "Su")

        daysLayout.removeAllViews()

        days.forEachIndexed { index, day ->
            val dayView =
                LayoutInflater.from(context).inflate(R.layout.item_day_of_week, daysLayout, false)
            val dayTextView = dayView.findViewById<TextView>(R.id.day)
            val dotImage =
                dayView.findViewById<ImageView>(R.id.dot) // Assuming you have this TextView for the dot

            dayTextView.text = day
            // Check if the day is today, and mark it with a dot
            if (day == mapDaysToCalendar[currentDayOfWeek]) {
                dotImage.visibility = View.VISIBLE
            } else {
                dotImage.visibility = View.INVISIBLE
            }

            // For days before today, change background to green filled circle
            if (index < days.indexOf(mapDaysToCalendar[currentDayOfWeek])) {
                dayTextView.setBackgroundResource(R.drawable.highlight_circle_bg) // Make sure you have this drawable
            }

            daysLayout.addView(dayView)
        }
    }

    private val updateQuotesRunnable = object : Runnable {
        override fun run() {
            updateQuote()
            // Schedule this runnable to run again after an hour
            updateHandler.postDelayed(this, TimeUnit.HOURS.toMillis(1))
        }
    }

    private fun updateQuote() {
        val randomIndex = (quotes.indices).random()
        quotesView?.text = quotes[randomIndex]
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        updateHandler.removeCallbacks(updateQuotesRunnable)
    }

    fun setData(headerAction: String?, baseAction: String?) {
        val calendarAction = SmartspaceAction(
            id = headerAction ?: baseAction ?: UUID.randomUUID().toString(),
            title = "unusedTitle",
            intent = BcSmartSpaceUtil.getOpenCalendarIntent(),
        )
        BcSmartSpaceUtil.setOnClickListener(this, calendarAction, null, "BcSmartspaceCard")
    }
}
