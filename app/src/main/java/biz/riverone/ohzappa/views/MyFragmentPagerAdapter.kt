package biz.riverone.ohzappa.views

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.view.View
import android.view.ViewGroup
import biz.riverone.ohzappa.common.MyCalendarUtil
import biz.riverone.ohzappa.common.MyCalendarUtil2
import biz.riverone.ohzappa.common.MyFragmentPagerAdapterBase
import biz.riverone.ohzappa.models.AccountItem
import java.util.*

/**
 * MyFragmentPagerAdapter.kt: スワイプでページを切り替える仕組み
 * Created by kawahara on 2018/01/08.
 */
class MyFragmentPagerAdapter(fragmentManager: FragmentManager)
    : MyFragmentPagerAdapterBase<String>(fragmentManager) {

    private val fragmentList = ArrayList<Fragment>()

    fun initialize(accountItem: AccountItem, monthsCount: Int, minYmd: Int) {

        val closingDay = accountItem.closingDay
        val currentClosingDate = MyCalendarUtil2.currentClosingDate(closingDay)

        var year = MyCalendarUtil.toYear(currentClosingDate)
        var month = MyCalendarUtil.toMonth(currentClosingDate)

        month -= (monthsCount - 1)
        while (month <= 0) {
            month += 12
            year -= 1
        }

        var my = 0
        var mm = 0

        if (minYmd > 0) {
            // もっとも古い取引日の月度を取得する(my年mm月度)
            my = MyCalendarUtil.toYear(minYmd)
            mm = MyCalendarUtil.toMonth(minYmd)
            val md = MyCalendarUtil.toDay(minYmd)
            val closingDate = MyCalendarUtil2.calcClosingDate(my, mm, accountItem.closingDay)
            val cd = MyCalendarUtil.toDay(closingDate)
            if (md > cd) {
                mm += 1
                if (mm > 12) {
                    my += 1
                    mm = 1
                }
            }
        }

        for (i in 1..monthsCount) {
            if (i < monthsCount - 1
                    && (minYmd == 0 || year < my || (year == my && month < mm))) {
                // 今月度とその1月前は必ず表示する
                // 2か月以上前はデータが存在する場合に限り表示する
            } else {
                val fragment = HistoryFragment.create(accountItem, year, month)
                fragmentList.add(fragment)
                add(fragment.title)
            }
            month += 1
            if (month > 12) {
                year += 1
                month = 1
            }
        }
    }

    private fun getFragmentByPosition(position: Int): Fragment {
        if (position < fragmentList.size) {
            return fragmentList[position]
        }
        return fragmentList[0]
    }

    override fun getFragment(item: String?, position: Int): Fragment {
        return getFragmentByPosition(position)
    }

    override fun getPageTitle(position: Int): CharSequence {
        return getItem(position)
    }

}
