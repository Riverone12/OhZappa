package biz.riverone.ohzappa.models

import android.content.ContentValues
import android.os.Parcel
import android.os.Parcelable
import biz.riverone.ohzappa.common.Database
import biz.riverone.ohzappa.common.MyCalendarUtil

/**
 * PaymentItem.kt: 支払い明細データ
 * Created by kawahara on 2017/12/31.
 */

class PaymentItem() : Parcelable {
    var id: Int = 0
    var accountId: Int = 0
    var date: Int = 0
    var amount: Int = 0
    var memo: String = ""
    var registered: Int = 0

    var erased: Int = 0

    fun clear() {
        id = 0
        accountId = 0
        date = 0
        amount = 0
        memo = ""
        registered = 0

        erased = 0
    }

    fun find(database: Database?, id: Int): Boolean {
        clear()
        val db = database?.resource ?: return false

        val selection = "id = ?"
        val selectionArgs = arrayOf(id.toString())

        val cursor = db.query(
                TABLE_NAME,
                columns,
                selection,
                selectionArgs,
                null, null, null
        )

        var result = false
        if (cursor.moveToFirst()) {
            this.id = cursor.getInt(0)
            accountId = cursor.getInt(1)
            date = cursor.getInt(2)
            amount = cursor.getInt(3)
            memo = cursor.getString(4)
            registered = cursor.getInt(5)

            result = true
        }
        cursor.close()
        return result
    }

    private fun insert(database: Database?) {
        registered = MyCalendarUtil.currentDay()
        id = getNextId(database)

        val contentValues = ContentValues()
        contentValues.put("id", id)
        contentValues.put("account", accountId)
        contentValues.put("pay_date", date)
        contentValues.put("amount", amount)
        contentValues.put("memo", memo)
        contentValues.put("registered", registered)

        val db = database?.resource
        db?.insert(TABLE_NAME, null, contentValues)
    }

    private fun update(database: Database?) {
        registered = MyCalendarUtil.currentDay()

        val whereClause = "id = ?"
        val whereArgs = arrayOf(id.toString())

        val contentValues = ContentValues()
        contentValues.put("account", accountId)
        contentValues.put("pay_date", date)
        contentValues.put("amount", amount)
        contentValues.put("memo", memo)
        contentValues.put("registered", registered)

        val db = database?.resource
        db?.update(TABLE_NAME, contentValues, whereClause, whereArgs)
    }

    fun register(database: Database?) {
        val temp = PaymentItem()
        if (id > 0 && temp.find(database, id)) {
            update(database)
        } else {
            insert(database)
        }
    }

    fun erase(database: Database?) {
        val db = database?.resource
        db?.delete(TABLE_NAME, "id=?", arrayOf(id.toString()))
    }

    constructor(parcel: Parcel) : this() {
        id = parcel.readInt()
        accountId = parcel.readInt()
        date = parcel.readInt()
        amount = parcel.readInt()
        memo = parcel.readString()
        registered = parcel.readInt()

        erased = parcel.readInt()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeInt(accountId)
        parcel.writeInt(date)
        parcel.writeInt(amount)
        parcel.writeString(memo)
        parcel.writeInt(registered)

        parcel.writeInt(erased)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object {
        const val TABLE_NAME= "payment_item"
        private val columns = arrayOf(
                "id",
                "account",
                "pay_date",
                "amount",
                "memo",
                "registered"
        )

        private fun getNextId(database: Database?): Int {
            val db = database?.resource ?: return -1
            val columns = arrayOf("COALESCE(MAX(id), 0) + 1")
            val cursor = db.query(
                    TABLE_NAME,
                    columns,
                    null, null,
                    null, null, null, null
            )
            var result = -1
            if (cursor.moveToFirst()) {
                result = cursor.getInt(0)
            }
            cursor.close()
            return result
        }

        @JvmField
        val CREATOR : Parcelable.Creator<PaymentItem>
                = object : Parcelable.Creator<PaymentItem> {
            override fun createFromParcel(parcel: Parcel): PaymentItem {
                return PaymentItem(parcel)
            }

            override fun newArray(size: Int): Array<PaymentItem?> {
                return arrayOfNulls(size)
            }
        }
    }
}