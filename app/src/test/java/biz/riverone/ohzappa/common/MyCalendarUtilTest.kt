package biz.riverone.ohzappa.common

import org.junit.Test

import org.junit.Assert.*
import java.util.*

/**
 * カレンダーのテスト
 * Created by kawahara on 2018/01/07.
 */
class MyCalendarUtilTest {
    @Test
    fun calendarToInt() {
        val cal = Calendar.getInstance()
        cal.clear()
        cal.set(2018, 0, 7)

        val result = MyCalendarUtil.calendarToInt(cal)
        assertEquals(20180107, result)


    }

    @Test
    fun intToCalendar() {
        val dt = 20180107
        val result = MyCalendarUtil.intToCalendar(dt)
        assertEquals(dt, MyCalendarUtil.calendarToInt(result))
    }

}