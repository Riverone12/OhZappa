package biz.riverone.ohzappa.views

import android.content.Context
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import biz.riverone.ohzappa.R
import biz.riverone.ohzappa.common.MyCalendarUtil
import biz.riverone.ohzappa.models.PaymentItem
import biz.riverone.ohzappa.models.PaymentItemList

/**
 * HistoryListViewAdapter.kt:
 * Created by kawahara on 2018/01/09.
 */
class HistoryListViewAdapter(context: Context, resource: Int, objects: PaymentItemList)
    : ArrayAdapter<PaymentItem>(context, resource, objects) {

    private var layoutInflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    private val paymentItemList = objects
    private val resourceId: Int = resource

    override fun getCount(): Int {
        return paymentItemList.size
    }

    override fun getItemId(position: Int): Long {
        return (paymentItemList[position].id).toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: layoutInflater.inflate(resourceId, null)
        val context = view.context

        val paymentItem = paymentItemList[position]

        val textViewDate = view.findViewById<TextView>(R.id.textViewDate)
        val m = MyCalendarUtil.toMonth(paymentItem.date)
        val d = MyCalendarUtil.toDay(paymentItem.date)
        val strDate = "$m/$d"
        textViewDate.text = strDate

        val textViewAmount = view.findViewById<TextView>(R.id.textViewAmount)
        val strAmount = "%,d".format(paymentItem.amount)
        textViewAmount.text = strAmount

        val textViewMemo = view.findViewById<TextView>(R.id.textViewMemo)
        textViewMemo.text = paymentItem.memo

        // 背景色を変更する
        var dayIdx = 0
        var currentDate = 0
        for (i in paymentItemList.indices) {
            if (paymentItemList[i].date > paymentItem.date) {
                break
            }
            if (currentDate != paymentItemList[i].date) {
                dayIdx += 1
                currentDate = paymentItemList[i].date
            }
        }
        val bkColor = if (dayIdx % 2 == 0) {
            ContextCompat.getColor(context, R.color.list_even)
        } else {
            ContextCompat.getColor(context, R.color.list_odd)
        }
        view.setBackgroundColor(bkColor)

        return view
    }
}