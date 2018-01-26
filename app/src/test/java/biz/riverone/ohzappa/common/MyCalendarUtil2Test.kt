package biz.riverone.ohzappa.common

import org.junit.Test

import org.junit.Assert.*

/**
 * カレンダーのテスト
 * Created by kawahara on 2018/01/04.
 */
class MyCalendarUtil2Test {
    @Test
    fun calcClosingDate() {
        // 締め日が25日の場合
        val res0 = MyCalendarUtil2.calcClosingDate(2017, 12, 25)
        assertEquals(20171225, res0)

        // 締め日が30日で、2月の場合
        val res1 = MyCalendarUtil2.calcClosingDate(2017, 2, 30)
        assertEquals(20170228, res1)

        // 締め日が29日で、2月（うるう年）の場合
        val res2 = MyCalendarUtil2.calcClosingDate(2020, 2, 29)
        assertEquals(20200229, res2)

        // 締め日が末日で、2月の場合
        val res3 = MyCalendarUtil2.calcClosingDate(2018, 2, 0)
        assertEquals(20180228, res3)

        // 締め日が末日で、2月（うるう年）の場合
        val res4 = MyCalendarUtil2.calcClosingDate(2020, 2, 0)
        assertEquals(20200229, res4)

        // 締め日が末日で、4月（30日）の場合
        val res5 = MyCalendarUtil2.calcClosingDate(2018, 4, 0)
        assertEquals(20180430, res5)

        // 締め日が末日で、1月（31日）の場合
        val res6 = MyCalendarUtil2.calcClosingDate(2018, 1, 0)
        assertEquals(20180131, res6)
    }

    @Test
    fun currentClosingDate() {
        val result = MyCalendarUtil2.currentClosingDate(0)
        assertEquals(20180131, result)
    }

    @Test
    fun lastClosingDate() {
        val result = MyCalendarUtil2.lastClosingDate(0)
        assertEquals(20171231, result)
    }

    @Test
    fun currentStartDate() {
        assertEquals(20171221, MyCalendarUtil2.currentStartDate(20))

        assertEquals(20180101, MyCalendarUtil2.currentStartDate(0))
    }

    @Test
    fun lastStartDate() {
        val result = MyCalendarUtil2.lastStartDate(20)
        assertEquals(20171121, result)

        val result2 = MyCalendarUtil2.lastStartDate(0)
        assertEquals(20171201, result2)
    }

}