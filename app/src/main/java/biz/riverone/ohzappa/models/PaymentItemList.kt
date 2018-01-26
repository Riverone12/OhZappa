package biz.riverone.ohzappa.models

import android.util.Log
import biz.riverone.ohzappa.common.Database
import biz.riverone.ohzappa.common.MyCalendarUtil
import biz.riverone.ohzappa.common.MyCalendarUtil2
import java.util.*

/**
 * PaymentItemList.kt: 支払い履歴の配列
 * Created by kawahara on 2018/01/08.
 */
class PaymentItemList : ArrayList<PaymentItem>() {

    companion object {
        private val TABLE_NAME = "payment_item"

        fun eraseOld(database: Database?) {
            // 15ヶ月以上前のデータを削除する
            val db = database?.resource ?: return
            val intDate = MyCalendarUtil.calendarToInt(Calendar.getInstance())
            val day = MyCalendarUtil.toDay(intDate)
            if (day % 10 == 0) {
                // 10日、20日、30日に実行する
                var year = MyCalendarUtil.toYear(intDate)
                var month = MyCalendarUtil.toMonth(intDate)
                month -= 15
                while (month < 0) {
                    month += 12
                    year -= 1
                }
                val ymd = MyCalendarUtil.ymdToInt(year, month, day)
                Log.d("PaymentItemList", "ymd= $ymd =============================")
                val where = "pay_date < ?"
                val whereArg = arrayOf(ymd.toString())
                db.delete(TABLE_NAME, where, whereArg)
            }
        }

        fun eraseAll(database: Database?) {
            val db = database?.resource ?: return
            db.delete(TABLE_NAME, null, null)
        }
    }

    val totalAmount: Int
        get() {
            return sumBy { it.amount }
        }

    fun amountOfDay(ymd: Int) : Int {
        return this.indices
                .filter { this[it].date == ymd }
                .sumBy { this[it].amount }
    }

    fun load(database: Database?, accountItem: AccountItem, year: Int, month: Int) {
        clear()

        val db = database?.resource ?: return

        val startDate = MyCalendarUtil2.calcStartDate(year, month, accountItem.closingDay)
        val endDate = MyCalendarUtil2.calcClosingDate(year, month, accountItem.closingDay)

        val columns = arrayOf(
                "id",
                "account",
                "pay_date",
                "amount",
                "memo",
                "registered"
        )

        val selection = "account = ? AND pay_date >= ? AND pay_date <= ?"
        val selectionArgs = arrayOf(
                accountItem.id.toString(),
                startDate.toString(),
                endDate.toString()
        )

        val cursor = db.query(
                TABLE_NAME,
                columns,
                selection,
                selectionArgs,
                null, null,
                "pay_date, id"
        )

        if (cursor?.moveToFirst() == true) {
            do {
                val item = PaymentItem()
                item.id = cursor.getInt(0)
                item.accountId = cursor.getInt(1)
                item.date = cursor.getInt(2)
                item.amount = cursor.getInt(3)
                item.memo = cursor.getString(4)
                item.registered = cursor.getInt(5)

                this.add(item)
            } while (cursor.moveToNext())
        }
        cursor?.close()
    }
}