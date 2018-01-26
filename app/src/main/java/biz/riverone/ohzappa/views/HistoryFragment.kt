package biz.riverone.ohzappa.views


import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ListView
import android.widget.TextView
import biz.riverone.ohzappa.R
import biz.riverone.ohzappa.common.ApplicationControl
import biz.riverone.ohzappa.common.MyCalendarUtil
import biz.riverone.ohzappa.common.MyCalendarUtil2
import biz.riverone.ohzappa.dialogs.InputAmountDialog
import biz.riverone.ohzappa.models.AccountItem
import biz.riverone.ohzappa.models.PaymentItem
import biz.riverone.ohzappa.models.PaymentItemList

/**
 * 科目別の支出履歴表示用フラグメント
 * Copyright (C) 2018 J.Kawahara
 * 2018.1.8 J.Kawahara 新規作成
 */

class HistoryFragment : Fragment() {

    companion object {

        private const val ARG_KEY_ACCOUNT_ITEM = "accountItem"
        private const val ARG_KEY_TARGET_YEAR = "targetYear"
        private const val ARG_KEY_TARGET_MONTH = "targetMonth"

        fun create(accountItem: AccountItem, year: Int, month: Int) : HistoryFragment {
            val arguments = Bundle()
            arguments.putParcelable(ARG_KEY_ACCOUNT_ITEM, accountItem)
            arguments.putInt(ARG_KEY_TARGET_YEAR, year)
            arguments.putInt(ARG_KEY_TARGET_MONTH, month)

            val fragment = HistoryFragment()
            fragment.arguments = arguments
            return fragment
        }
    }

    private var accountItem: AccountItem? = null
    private var targetYear: Int = 0
    private var targetMonth: Int = 0

    var textViewTotal: TextView? = null
    var listView: ListView? = null
    var textViewNoData: TextView? = null

    private var paymentItemList = PaymentItemList()
    private var listAdapter: HistoryListViewAdapter? = null

    private fun getParameters() {
        targetYear =
                if (arguments.containsKey(ARG_KEY_TARGET_YEAR)) {
                    arguments.getInt(ARG_KEY_TARGET_YEAR)
                } else {
                    0
                }
        targetMonth =
                if (arguments.containsKey(ARG_KEY_TARGET_MONTH)) {
                    arguments.getInt(ARG_KEY_TARGET_MONTH)
                } else {
                    0
                }

        accountItem =
                if (arguments.containsKey(ARG_KEY_ACCOUNT_ITEM)) {
                    arguments.getParcelable(ARG_KEY_ACCOUNT_ITEM)
                } else {
                    AccountItem()
                }
    }

    val title: String get() {
        getParameters()
        return targetYear.toString() + "年" + targetMonth.toString() + "月度"
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val v = inflater!!.inflate(R.layout.fragment_history, container, false)

        getParameters()
        initializeControls(v)

        return v
    }

    private fun initializeControls(v: View) {
        // タイトル
        val textViewMonthlyTitle = v.findViewById<TextView>(R.id.textViewMonthlyTitle)
        textViewMonthlyTitle.text = title

        // 処理対象の開始日、終了日を取得する
        val closingDay = accountItem?.closingDay ?: 0
        val startDate = MyCalendarUtil2.calcStartDate(targetYear, targetMonth, closingDay)
        val closingDate = MyCalendarUtil2.calcClosingDate(targetYear, targetMonth, closingDay)

        val sm = MyCalendarUtil.toMonth(startDate)
        val sd = MyCalendarUtil.toDay(startDate)
        val em = MyCalendarUtil.toMonth(closingDate)
        val ed = MyCalendarUtil.toDay(closingDate)

        val strPeriod = "($sm/$sd ～ $em/$ed)"

        val textViewPeriod = v.findViewById<TextView>(R.id.textViewPeriod)
        textViewPeriod.text = strPeriod

        listView = v.findViewById(R.id.listView)
        listView?.onItemClickListener = listViewItemClickListener

        textViewTotal = v.findViewById(R.id.textViewTotal)
        textViewNoData = v.findViewById(R.id.textViewNoData)

        reload()
    }

    private fun reload() {
        // 対象期間の支払いデータを取得する
        paymentItemList.load(ApplicationControl.database, accountItem!!, targetYear, targetMonth)

        // 対象月度の合計額
        val strYen = "%,d円".format(paymentItemList.totalAmount)
        textViewTotal?.text = strYen

        // リストビューの準備
        listAdapter = HistoryListViewAdapter(context, R.layout.history_list_row, paymentItemList)
        listView?.adapter = listAdapter

        if (paymentItemList.size > 0) {
            textViewNoData?.visibility = View.GONE
            listView?.visibility = View.VISIBLE
        } else {
            textViewNoData?.visibility = View.VISIBLE
            listView?.visibility = View.GONE
        }
    }

    private val listViewItemClickListener = AdapterView.OnItemClickListener {
        _, _, _, l ->

        val id = l.toInt()
        val paymentItem = PaymentItem()
        if (paymentItem.find(ApplicationControl.database, id)) {
            // 編集ダイアログを表示する
            val inputDialog = InputAmountDialog.create(paymentItem, accountItem!!)
            inputDialog.setTargetFragment(this, InputAmountDialog.REQUEST_CODE)
            inputDialog.show(activity.supportFragmentManager, "dialog")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == InputAmountDialog.REQUEST_CODE
                && resultCode == RESULT_OK) {

            if (data != null && data.hasExtra(InputAmountDialog.EXTRA_KEY_PAYMENT_ITEM)) {
                val paymentItem = data.getParcelableExtra<PaymentItem>(InputAmountDialog.EXTRA_KEY_PAYMENT_ITEM)
                if (paymentItem.erased > 0) {
                    // 削除する
                    Log.d("HistoryFragment", "pyamentItem.id = ${paymentItem.id}, erased = ${paymentItem.erased}")
                    paymentItem.erase(ApplicationControl.database)
                } else {
                    // 更新する
                    paymentItem.register(ApplicationControl.database)
                    Log.d("HistoryFragment", "pyamentItem.id = ${paymentItem.id}, erased = ${paymentItem.erased}")
                }

                // 再表示
                reload()
            }
        }
    }
}
