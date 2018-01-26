package biz.riverone.ohzappa.models

import biz.riverone.ohzappa.common.Database

/**
 * 毎月の支出額集計
 * Created by kawahara on 2018/01/07.
 */
object MonthlyPayment {

    private const val TABLE_NAME = "payment_item"

    fun calc(database: Database?, accountId: Int, startDate: Int, endDate: Int): Int {
        val db = database?.resource ?: return 0

        val columns = arrayOf("SUM(amount)")
        val selection = "account = ? AND pay_date >= ? AND pay_date <= ?"
        val selectionArgs = arrayOf(
                accountId.toString(),
                startDate.toString(),
                endDate.toString()
        )
        val cursor = db.query(
                TABLE_NAME,
                columns,
                selection,
                selectionArgs,
                null, null, null
        )
        var result = 0
        if (cursor.moveToFirst()) {
            result = cursor.getInt(0)
        }
        cursor.close()
        return result
    }
}