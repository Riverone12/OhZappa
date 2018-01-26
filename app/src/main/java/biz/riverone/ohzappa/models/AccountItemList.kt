package biz.riverone.ohzappa.models

import android.content.ContentValues
import biz.riverone.ohzappa.common.ApplicationControl.Companion.context
import biz.riverone.ohzappa.common.Database

/**
 * 勘定科目の配列
 * Created by kawahara on 2017/12/31.
 */

class AccountItemList : ArrayList<AccountItem>() {

    companion object {
        private val DB_TABLE = "account_item"

        fun reset(database: Database?) {
            val db = database?.resource ?: return
            db.delete(DB_TABLE, null, null)

            database.DBHelper(context).onCreate(database.resource)
        }
    }

    fun find(id: Int): AccountItem? {
        return this.firstOrNull { it.id == id }
    }

    // 全アイテムを読み込む
    fun load(database: Database?) {
        clear()

        val dbResource = database?.resource
        if (dbResource != null) {
            val columns = arrayOf(
                    "id",
                    "title",
                    "closing",
                    "settlement",
                    "color"
            )
            val cursor = dbResource.query(
                    DB_TABLE, columns, null, null, null, null, "id"
            )
            if (cursor?.moveToFirst() == true) {
                do {
                    val item = AccountItem()
                    item.id = cursor.getInt(0)
                    item.title = cursor.getString(1)
                    item.closingDay = cursor.getInt(2)
                    item.settlementDay = cursor.getInt(3)
                    item.colorId = cursor.getInt(4)

                    this.add(item)
                } while (cursor.moveToNext())
            }
            cursor?.close()
        }
    }

    // データ更新
    fun update(database: Database?, accountItem: AccountItem) : Boolean {
        val dbResource = database?.resource
        var result = false
        if (dbResource != null) {
            val values = ContentValues()
            values.put("title", accountItem.title)
            values.put("closing", accountItem.closingDay)
            values.put("settlement", accountItem.settlementDay)
            values.put("color", accountItem.colorId)

            val updatedCount: Int = if (accountItem.id <= 0) {
                dbResource.insert(DB_TABLE, null, values).toInt()
            } else {
                val where = "id = " + accountItem.id
                dbResource.update(DB_TABLE, values, where, null)
            }
            result = (updatedCount > 0)
            if (result) {
                find(accountItem.id)?.from(accountItem)
            }
        }
        return result
    }
}