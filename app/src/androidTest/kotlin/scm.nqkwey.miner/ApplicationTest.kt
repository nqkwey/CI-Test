package scm.nqkwey.miner

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.assertion.ViewAssertions
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.espresso.matcher.ViewMatchers.withText
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import com.newtronlabs.easyexchange.EasyCurrency
import org.hamcrest.core.StringContains
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ApplicationTest {

    @Rule
    @JvmField
    val activity = ActivityTestRule<MainActivity>(MainActivity::class.java)

   /* @Test
    fun testAdd() {
        onView(withId(R.id.textView))
                .check(ViewAssertions.matches(ViewMatchers.withText("Hello World!")))
    }*/

    @Test
    fun testCurrency() {
        Thread.sleep(5000)
        onView(withId(R.id.textView))
                .check(ViewAssertions.matches(withText(StringContains.containsString(EasyCurrency.RUB.key))))
    }

   /* @Test
    fun testUSDCurrency() {
        Thread.sleep(10000)
        onView(withId(R.id.textView))
                .check(ViewAssertions.matches(withText(StringContains.containsString(EasyCurrency.USD.key))))
    }*/
}
