package biz.riverone.ohzappa.dialogs

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import biz.riverone.ohzappa.R
import biz.riverone.ohzappa.models.AccountItem

/**
 * 科目の設定ダイアログ
 * Created by kawahara on 2018/01/02.
 */
class SettingAccountDialog : SettingDialogBase() {

    companion object {
        const val REQUEST_CODE = 10
        const val EXTRA_KEY_ACCOUNT_ITEM = "accountItem"

        fun create(accountItem: AccountItem) : SettingAccountDialog {
            val fragment = SettingAccountDialog()
            val bundle = Bundle()
            bundle.putParcelable(EXTRA_KEY_ACCOUNT_ITEM, accountItem)
            fragment.arguments = bundle
            fragment.setTargetFragment(null, REQUEST_CODE)
            return fragment
        }
    }

    override val layoutId = R.layout.dialog_setting_account
    override val dialogTitleResourceId: Int
        get() = R.string.empty

    private var accountItem: AccountItem? = null

    private var editName: EditText? = null
    private var closingDaySpinner: Spinner? = null

    override fun initializeControls(v: View) {
        if (arguments != null && arguments.containsKey(EXTRA_KEY_ACCOUNT_ITEM)) {
            accountItem = arguments.getParcelable(EXTRA_KEY_ACCOUNT_ITEM)
        }
        if (accountItem == null) {
            accountItem = AccountItem()
        }

        // タイトルを設定する
        val title = resources.getString(R.string.settingMenuAccount)
        val titleView = v.findViewById<TextView>(R.id.textViewSettingAccountTitle)
        titleView.text = title

        // 背景色を設定する
        val colors = resources.obtainTypedArray(R.array.colors)
        val colorId = accountItem?.colorId ?: 0
        v.setBackgroundColor(colors.getColor(colorId, 0))
        colors.recycle()

        // 名称の入力欄の準備をする
        editName = v.findViewById<EditText>(R.id.editTextAccountName)
        editName?.setText(accountItem?.title)

        // スピナーの準備をする
        val daysList = resources.getStringArray(R.array.strDays)
        val adapter = ArrayAdapter<String>(context, R.layout.spinner_item, daysList)
        closingDaySpinner = v.findViewById(R.id.spinnerClosingDay)
        closingDaySpinner?.adapter = adapter

        closingDaySpinner?.setSelection(accountItem?.closingDay?: 0)
    }

    override fun putResult(result: Intent): Intent {
        accountItem?.title = editName?.text.toString()
        accountItem?.closingDay = closingDaySpinner?.selectedItemPosition ?: 0

        result.putExtra(EXTRA_KEY_ACCOUNT_ITEM, accountItem)
        return result
    }
}