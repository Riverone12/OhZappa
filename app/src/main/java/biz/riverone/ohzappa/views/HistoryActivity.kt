package biz.riverone.ohzappa.views

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Button
import biz.riverone.ohzappa.R
import biz.riverone.ohzappa.common.ApplicationControl
import biz.riverone.ohzappa.models.AccountItem
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds

/**
 * HistoryActivity.kt: 支出の履歴を表示
 * Copyright (C) 2018 J.Kawahara
 * 2018.1.7 J.Kawahara 新規作成
 * 2018.1.26 J.Kawahara 戻るボタンを削除
 */

class HistoryActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_KEY_ACCOUNT_ITEM = "accountItem"
        private const val MAX_MONTHS = 13
    }

    private val pager: ViewPager by lazy { findViewById<ViewPager>(R.id.pager) }
    private lateinit var pagerAdapter: MyFragmentPagerAdapter
    private lateinit var mAdView : AdView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        // 画面をポートレートに固定する
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        val accountItem = if (intent.hasExtra(EXTRA_KEY_ACCOUNT_ITEM)) {
            intent.getParcelableExtra(EXTRA_KEY_ACCOUNT_ITEM)
        } else {
            AccountItem()
        }

        // タイトルを設定する
        title = accountItem.title

        // データがある月からのみ表示する
        val minYmd = AccountItem.minimamPayDate(ApplicationControl.database, accountItem.id)

        // ページャーの準備
        pagerAdapter = MyFragmentPagerAdapter(this.supportFragmentManager)
        pager.adapter = pagerAdapter
        pagerAdapter.initialize(accountItem, MAX_MONTHS, minYmd)

        // 背景色を設定する
        val outerLayout = findViewById<View>(R.id.historyOuterLayout)
        val colors = resources.obtainTypedArray(R.array.colors)
        val defaultColorId = (accountItem.id - 1) % colors.length()
        outerLayout.setBackgroundColor(colors.getColor(accountItem.colorId, defaultColorId))
        colors.recycle()

        // 最後のページを表示する
        val pageCount = pagerAdapter.count
        if (pageCount > 0) {
            pager.currentItem = pageCount -1
        }

        // AdMob
        MobileAds.initialize(this, "ca-app-pub-1882812461462801~4668241085")
        mAdView = findViewById(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)
    }
}
