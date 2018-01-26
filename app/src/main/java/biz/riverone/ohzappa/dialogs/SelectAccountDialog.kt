package biz.riverone.ohzappa.dialogs

import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import android.view.View
import android.widget.TextView
import biz.riverone.ohzappa.R
import biz.riverone.ohzappa.common.Database
import biz.riverone.ohzappa.models.AccountItemList

/**
 * 科目の選択ダイアログ
 * Created by kawahara on 2018/01/08.
 */
class SelectAccountDialog : SettingDialogBase() {

    companion object {
        const val REQUEST_CODE = 2
        const val EXTRA_KEY_ACCOUNT_ID = "accountId"

        fun create() : SelectAccountDialog {
            val dialog = SelectAccountDialog()
            dialog.setTargetFragment(null, REQUEST_CODE)
            dialog.showOkButton = false
            return dialog
        }
    }

    override val layoutId = R.layout.dialog_select_account
    override val dialogTitleResourceId: Int
        get() = R.string.selectAccountDialogTitle

    private val accountItemList = AccountItemList()
    private val controllerList = ArrayList<View>()

    override fun initializeControls(v: View) {
        // データベースから科目データを取得する
        val database = Database(context)
        database.openReadable()
        accountItemList.load(database)
        database.close()

        // 選択肢コントロールの準備
        controllerList.clear()
        val itemCount = accountItemList.size
        val packageName = activity.packageName
        (0..itemCount)
                .map { resources.getIdentifier("account"+ it, "id", packageName) }
                .mapNotNullTo(controllerList) { v.findViewById(it)}

        // 科目名と締め日、背景色を設定する
        val colors = resources.obtainTypedArray(R.array.colors)
        val strDays = resources.getStringArray(R.array.strDays)

        for (i in accountItemList.indices) {
            val accountItem = accountItemList[i]
            val view = controllerList[i]

            // 科目名を表示する
            val textViewAccountName = view.findViewById<TextView>(R.id.textViewAccountName)
            textViewAccountName.text = accountItem.title

            // 締め日を表示する
            val textViewClosingDay = view.findViewById<TextView>(R.id.textViewClosingDay)
            textViewClosingDay.text = strDays[accountItem.closingDay]

            // 背景色を設定する
            val defaultColorId = (accountItem.id - 1) % colors.length()
            view.setBackgroundColor(colors.getColor(accountItem.colorId, defaultColorId))

            view.tag = accountItem.id

            view.setOnClickListener(onClickListener)
        }
        colors.recycle()
    }

    private val onClickListener = View.OnClickListener {
        sender ->

        val result = Intent()
        val accountId = sender?.tag as Int
        result.putExtra(EXTRA_KEY_ACCOUNT_ID, accountId)
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