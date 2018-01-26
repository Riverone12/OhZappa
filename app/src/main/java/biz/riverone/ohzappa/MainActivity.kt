package biz.riverone.ohzappa

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import biz.riverone.ohzappa.common.ApplicationControl
import biz.riverone.ohzappa.common.MyCalendarUtil
import biz.riverone.ohzappa.common.MyCalendarUtil2
import biz.riverone.ohzappa.dialogs.InputAmountDialog
import biz.riverone.ohzappa.dialogs.ResetDialogFragment
import biz.riverone.ohzappa.dialogs.SelectAccountDialog
import biz.riverone.ohzappa.dialogs.SettingAccountDialog
import biz.riverone.ohzappa.models.*
import biz.riverone.ohzappa.views.HistoryActivity
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import java.util.*

/**
 * OhZappa : おおざっぱ支出管理
 * Copyright (C) 2017-2018 J.Kawahara
 * 2017.12.31 J.Kawahara 新規作成
 * 2017.1.12 ver.1.00 J.Kawahara
 *           ver.1.01 J.Kawahara Firebase アナリティクス 対応
 * 2018.1.26 ver.1.02 J.Kawahara 履歴アクティビティの戻るボタンを削除
 */
class MainActivity : AppCompatActivity() {

    private val accountItemList = AccountItemList()
    private val controllerList = ArrayList<View>()
    private lateinit var mAdView : AdView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 画面をポートレートに固定する
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        // 古いデータを削除する
        PaymentItemList.eraseOld(ApplicationControl.database)

        // AdMob
        MobileAds.initialize(this, "ca-app-pub-1882812461462801~4668241085")
        mAdView = findViewById(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)
    }

    override fun onStart() {
        super.onStart()

        // データベースから科目データを取得する
        accountItemList.load(ApplicationControl.database)

        initializeControls()
    }

    private fun initializeControls() {

        controllerList.clear()
        val itemCount = accountItemList.size

        // UI の配列を生成する
        (0..itemCount)
                .map { resources.getIdentifier("account" + it, "id", packageName) }
                .mapNotNullTo(controllerList) { findViewById(it) }

        // 科目名と背景色を設定する
        for (i in accountItemList.indices) {
            // 画面表示
            val accountItem = accountItemList[i]
            val view = controllerList[i]
            display(view, accountItem)

            // 入力ボタンの準備をする
            val buttonInput = view.findViewById<Button>(R.id.buttonInput)
            buttonInput.setOnClickListener(onButtonInputClickListener)

            // 履歴表示画面へのリンクの準備をする
            val calcViewer = view.findViewById<View>(R.id.calcViewer)
            calcViewer.setOnClickListener(onAccountClickListener)

            buttonInput.tag = accountItem.id
            calcViewer.tag = accountItem.id
        }
    }

    private fun strYen(yen: Int): String {
        val strYen = "%,d".format(yen)
        if (yen >= 1000000) {
            return strYen
        }
        return strYen + resources.getString(R.string.labelYen)
    }

    private fun display(view: View, accountItem: AccountItem) {
        // 科目名を設定する
        val textViewAccountName = view.findViewById<TextView>(R.id.textViewAccountName)
        textViewAccountName.text = accountItem.title

        // 背景色を設定する
        val colors = resources.obtainTypedArray(R.array.colors)
        val defaultColorId = (accountItem.id - 1) % colors.length()
        view.setBackgroundColor(colors.getColor(accountItem.colorId, defaultColorId))
        colors.recycle()

        // 先月度の締め日を表示する
        val lastClosingDay = MyCalendarUtil2.lastClosingDate(accountItem.closingDay)
        val month = MyCalendarUtil.toMonth(lastClosingDay)
        val day = MyCalendarUtil.toDay(lastClosingDay)
        val md = String.format("(～%d/%d)", month, day)
        val labelLastMonth = resources.getString(R.string.labelLastMonth) + md
        val textViewLastMonth = view.findViewById<TextView>(R.id.textViewLastMonth)
        textViewLastMonth.text = labelLastMonth

        // 先月度の集計額を表示する
        val lastMonthStart = MyCalendarUtil2.lastStartDate(accountItem.closingDay)
        val lastTotal = MonthlyPayment.calc(ApplicationControl.database, accountItem.id, lastMonthStart, lastClosingDay)
        val textViewLastAmount = view.findViewById<TextView>(R.id.textViewLastAmount)
        textViewLastAmount.text = strYen(lastTotal)

        // 今月度の集計額を表示する
        val currentMonthStart = MyCalendarUtil2.currentStartDate(accountItem.closingDay)
        val currentDay = MyCalendarUtil.currentDay()
        val currentTotal = MonthlyPayment.calc(ApplicationControl.database, accountItem.id, currentMonthStart, currentDay)
        val textViewAmount = view.findViewById<TextView>(R.id.textViewAmount)
        textViewAmount.text = strYen(currentTotal)
    }

    // 入力ボタンクリック時のリスナ
    private val onButtonInputClickListener = View.OnClickListener {
        sender ->

        if (sender != null && sender.tag != null) {
            val accountId = sender.tag as Int
            val accountItem = accountItemList.find(accountId) ?: AccountItem()
            if (accountItem.id > 0) {
                val paymentItem = PaymentItem()
                paymentItem.accountId = accountId
                paymentItem.date = MyCalendarUtil.calendarToInt(Calendar.getInstance())

                val inputDialog = InputAmountDialog.create(paymentItem, accountItem)
                inputDialog.show(supportFragmentManager, "dialog")
            }
        }
    }

    // 科目表示部クリック時のリスナ
    private val onAccountClickListener = View.OnClickListener {
        sender ->

        if (sender != null && sender.tag != null) {
            val accountId = sender.tag as Int
            val accountItem = accountItemList.find(accountId) ?: AccountItem()
            if (accountItem.id > 0) {
                val sendIntent = Intent(this, HistoryActivity::class.java)
                sendIntent.putExtra(HistoryActivity.EXTRA_KEY_ACCOUNT_ITEM, accountItem)
                startActivity(sendIntent)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    private fun showSettingAccountDialog(accountId: Int) {
        val accountItem = accountItemList.find(accountId) ?: return
        val settingDialog = SettingAccountDialog.create(accountItem)
        settingDialog.show(supportFragmentManager, "dialog")
    }

    override fun onOptionsItemSelected(item: MenuItem?) : Boolean {
        when (item?.itemId) {
            R.id.menu_setting_account -> {
                // 科目の設定
                val selectAccountDialog = SelectAccountDialog.create()
                selectAccountDialog.show(supportFragmentManager, "dialog")
                return true
            }

            R.id.menu_reset -> {
                // リセット
                ResetDialogFragment.show(supportFragmentManager)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    // ダイアログから戻ってきたときの処理
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
         if (requestCode == InputAmountDialog.REQUEST_CODE
                && resultCode == RESULT_OK) {
            // 支出額の入力ダイアログから戻ってきた
            if (data != null && data.hasExtra(InputAmountDialog.EXTRA_KEY_PAYMENT_ITEM)) {
                // 編集内容を登録する
                val paymentItem = data.getParcelableExtra<PaymentItem>(InputAmountDialog.EXTRA_KEY_PAYMENT_ITEM)
                paymentItem.register(ApplicationControl.database)

                val viewIndex = paymentItem.accountId - 1
                display(controllerList[viewIndex], accountItemList[viewIndex])
            }
        } else if (requestCode == SelectAccountDialog.REQUEST_CODE
                && resultCode == RESULT_OK) {
            // 設定を変更する科目の選択ダイアログから戻ってきた
            if (data != null && data.hasExtra(SelectAccountDialog.EXTRA_KEY_ACCOUNT_ID)) {
                val accountId = data.getIntExtra(SelectAccountDialog.EXTRA_KEY_ACCOUNT_ID, 0)
                if (accountId > 0) {
                    // 科目の設定ダイアログを表示する
                    showSettingAccountDialog(accountId)
                }
            }
        } else if (requestCode == SettingAccountDialog.REQUEST_CODE
                && resultCode == RESULT_OK) {
            // 科目の設定ダイアログから戻ってきた
            if (data != null && data.hasExtra(SettingAccountDialog.EXTRA_KEY_ACCOUNT_ITEM)) {
                val accountItem = data.getParcelableExtra<AccountItem>(SettingAccountDialog.EXTRA_KEY_ACCOUNT_ITEM)
                accountItemList.update(ApplicationControl.database, accountItem)

                val viewIndex = accountItem.id - 1
                display(controllerList[viewIndex], accountItem)
            }
        } else if (requestCode == ResetDialogFragment.REQUEST_CODE
                 && resultCode == RESULT_OK) {
             // リセット確認ダイアログから戻ってきた

             // 明細データを全て削除する
             PaymentItemList.eraseAll(ApplicationControl.database)

             // 科目データを削除後、初期データを投入する
             AccountItemList.reset(ApplicationControl.database)

             onStart()
             Toast.makeText(this, R.string.reset_message, Toast.LENGTH_SHORT).show()
         }
    }
}
