package biz.riverone.ohzappa.dialogs

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import android.widget.TextView
import biz.riverone.ohzappa.R
import biz.riverone.ohzappa.common.MyCalendarUtil
import biz.riverone.ohzappa.models.AccountItem
import biz.riverone.ohzappa.models.PaymentItem

/**
 * 取引内容を入力するダイアログ
 * Created by kawahara on 2018/01/02.
 */
class InputAmountDialog : SettingDialogBase(), DatePickerDialog.OnDateSetListener {

    companion object {
        const val REQUEST_CODE: Int = 100
        const val EXTRA_KEY_PAYMENT_ITEM = "paymentItem"
        const val EXTRA_KEY_ACCOUNT_ITEM = "accountItem"

        fun create(paymentItem: PaymentItem, accountItem: AccountItem): InputAmountDialog {
            val dialog = InputAmountDialog()
            val bundle = Bundle()
            bundle.putParcelable(EXTRA_KEY_PAYMENT_ITEM, paymentItem)
            bundle.putParcelable(EXTRA_KEY_ACCOUNT_ITEM, accountItem)
            dialog.arguments = bundle
            dialog.setTargetFragment(null, REQUEST_CODE)
            return dialog
        }
    }

    override val layoutId = R.layout.dialog_input_amount
    override val dialogTitleResourceId: Int
        get() = R.string.empty

    private var paymentItem: PaymentItem? = null
    private var textViewTradeDay: TextView? = null
    private var yenEdit: EditText? = null
    private var memoEdit: EditText? = null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)

        // ソフトウェアキーボードを表示する
        yenEdit?.requestFocus()
        yenEdit?.selectAll()
        dialog.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)

        return view
    }

    override fun initializeControls(v: View) {
        val accountItem: AccountItem = if (arguments != null && arguments.containsKey(EXTRA_KEY_ACCOUNT_ITEM)) {
            arguments.getParcelable(EXTRA_KEY_ACCOUNT_ITEM)
        } else {
            AccountItem()
        }

        if (arguments != null && arguments.containsKey(EXTRA_KEY_PAYMENT_ITEM)) {
            paymentItem = arguments.getParcelable(EXTRA_KEY_PAYMENT_ITEM)
        }
        if (paymentItem == null) {
            paymentItem = PaymentItem()
            paymentItem?.date = MyCalendarUtil.currentDay()
        }

        // 背景色を設定する
        val colors = resources.obtainTypedArray(R.array.colors)
        v.setBackgroundColor(colors.getColor(accountItem.colorId, 0))
        colors.recycle()

        // タイトルを設定する
        val titleView = v.findViewById<TextView>(R.id.textViewTitle)
        val title = accountItem.title + resources.getString(R.string.dialogPostTitle)
        titleView.text = title

        // 日付入力の準備
        textViewTradeDay = v.findViewById(R.id.textViewTradeDate)
        textViewTradeDay?.setOnClickListener {
            val currentDate = MyCalendarUtil.currentDay()
            val y = MyCalendarUtil.toYear(paymentItem?.date ?: MyCalendarUtil.toYear(currentDate))
            val m = MyCalendarUtil.toMonth(paymentItem?.date ?: MyCalendarUtil.toMonth(currentDate))
            val d = MyCalendarUtil.toDay(paymentItem?.date ?: MyCalendarUtil.toDay(currentDate))

            val datePickerDialog = DatePickerDialog(context, this, y, m - 1, d)
            datePickerDialog.show()
        }

        val currentDay = MyCalendarUtil.currentDay()
        if (paymentItem?.date ?: 0 <= 0) {
            paymentItem?.date = currentDay
        }
        displayTradeDate(paymentItem?.date ?: currentDay)

        // 金額入力の準備
        yenEdit = v.findViewById(R.id.editTextAmount)
        if (paymentItem?.amount == 0) {
            yenEdit?.setText("")
        } else {
            val str = paymentItem!!.amount.toString()
            yenEdit?.setText(str)
            yenEdit?.setSelection(0, str.length)
        }
        yenEdit?.requestFocus()

        // メモ欄の準備
        memoEdit = v.findViewById(R.id.editTextMemo)
        memoEdit?.setText(paymentItem?.memo)

        // 削除ボタンの準備
        val eraseController = v.findViewById<View>(R.id.eraseController)
        if (paymentItem?.id ?: 0 > 0) {
            val buttonErase = v.findViewById<Button>(R.id.buttonErase)
            buttonErase.setOnClickListener(buttonEraseClickListener)
        } else {
            eraseController.visibility = View.GONE
        }
    }

    // 日付選択ダイアログから戻ってきたときのリスナ
    override fun onDateSet(datePicker: DatePicker?, year: Int, month: Int, day: Int) {
        paymentItem?.date = (year * 10000) + ((month + 1) * 100) + day
        displayTradeDate(paymentItem?.date?: MyCalendarUtil.currentDay())
    }

    override fun putResult(result: Intent): Intent {
        // 日付は、onDateSet() にて設定済みのはず

        // 金額を取得する
        try {
            paymentItem?.amount = Integer.parseInt(yenEdit?.text.toString())
        }
        catch (ex: NumberFormatException) {
            paymentItem?.amount = 0
        }
        // メモを取得する
        paymentItem?.memo = memoEdit?.text.toString()

        result.putExtra(EXTRA_KEY_PAYMENT_ITEM, paymentItem)
        return result
    }

    private fun displayTradeDate(value: Int) {
        val caption = DateFormat.format("yyyy/MM/dd", MyCalendarUtil.intToCalendar(value))
        textViewTradeDay?.text = caption
    }

    // 削除ボタンクリック時の動作
    private val buttonEraseClickListener = View.OnClickListener {
        AlertDialog.Builder(activity)
                .setTitle(R.string.confirmEraseTitle)
                .setMessage(R.string.confirmErase)
                .setPositiveButton(R.string.captionOk, eraseOkButtonClickListener)
                .setNegativeButton(R.string.captionCancel, null)
                .show()
    }

    private val eraseOkButtonClickListener = DialogInterface.OnClickListener {
        _, _ ->

        // 削除フラグを立てる
        paymentItem?.erased = 1

        val result = Intent()
        putResult(result)

        if (targetFragment != null) {
            targetFragment.onActivityResult(targetRequestCode, Activity.RESULT_OK, result)
        } else {
            val pendingIntent = activity.createPendingResult(targetRequestCode, result, PendingIntent.FLAG_ONE_SHOT)
            try {
                pendingIntent.send(Activity.RESULT_OK)
            } catch (ex: PendingIntent.CanceledException) {
                ex.printStackTrace()
            }
        }
        dismiss()
    }
}