package biz.riverone.ohzappa.models

import android.os.Parcel
import android.os.Parcelable
import biz.riverone.ohzappa.common.Database

/**
 * AccountItem.kt: 勘定科目
 * Created by kawahara on 2017/12/31.
 */

class AccountItem() : Parcelable {
    var id: Int = 0
    var title: String = ""
    var closingDay: Int = 0
    var settlementDay: Int = 0
    var colorId: Int = 0

    constructor(parcel: Parcel) : this() {
        id = parcel.readInt()
        title = parcel.readString()
        closingDay = parcel.readInt()
        settlementDay = parcel.readInt()
        colorId = parcel.readInt()
    }

    fun from(c: AccountItem) {
        id = c.id
        title = c.title
        closingDay = c.closingDay
        settlementDay = c.settlementDay
        colorId = c.colorId
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(title)
        parcel.writeInt(closingDay)
        parcel.writeInt(settlementDay)
        parcel.writeInt(colorId)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object {

        fun minimamPayDate(database: Database?, id: Int) : Int {
            val db = database?.resource ?: return 0

            val columns = arrayOf("MIN(pay_date)")
            val selection = "account = ?"
            val selectionArgs = arrayOf(id.toString())

            val cursor = db.query(
                    "payment_item",
                    columns,
                    selection,
                    selectionArgs,
                    null, null, null
            )

            var ymd = 0
            if (cursor.moveToFirst()) {
               ymd = cursor.getInt(0)
            }
            cursor.close()
            return ymd
        }

        @JvmField
        val CREATOR : Parcelable.Creator<AccountItem>
                = object: Parcelable.Creator<AccountItem> {
            override fun createFromParcel(parcel: Parcel): AccountItem {
                return AccountItem(parcel)
            }

            override fun newArray(size: Int): Array<AccountItem?> {
                return arrayOfNulls(size)
            }
        }
    }
}