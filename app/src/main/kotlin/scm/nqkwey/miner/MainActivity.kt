package scm.nqkwey.miner

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.newtronlabs.easyexchange.EasyCurrency
import com.newtronlabs.easyexchange.EasyExchangeManager
import com.newtronlabs.easyexchange.ICurrencyExchange
import com.newtronlabs.easyexchange.ICurrencyExchangeCallback
import io.reactivex.Observable
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onResume() {
        super.onResume()
        exchangeObservable(EasyCurrency.BTC, EasyCurrency.RUB).subscribe({
            onUiThread(Runnable {
                val text = "1 BTC = ${it.toAmount.roundToInt()} ${it.toCurrency}"
                textView.text = text
            })
        }, {
            onUiThread(Runnable { Toast.makeText(this@MainActivity, "Finished with the error: ${it.message}", Toast.LENGTH_SHORT).show()})
        })
    }

    private fun onUiThread(runnable: Runnable) {
        runOnUiThread { runnable.run() }
    }

    private fun exchangeObservable(from: EasyCurrency, to: EasyCurrency): Observable<ICurrencyExchange> {
        return Observable.create {
            EasyExchangeManager.getInstance().performExchange(from, to, 1.0, object : ICurrencyExchangeCallback {
                override fun onExchangeComplete(value: ICurrencyExchange?) {
                    if (!it.isDisposed && value != null)
                        it.onNext(value)
                    else if (value == null)
                        it.onError(NullPointerException("Exchange value is null, bro. Sorry for this"))
                    else
                        it.onError(IllegalArgumentException("Value is disposed, you're late, man"))
                }

                override fun onExchangeFailed(value: ICurrencyExchange?, t: Throwable?) {
                    if (t != null)
                        it.onError(t)
                    if (value != null)
                        it.onNext(value)
                }

            })
        }
    }
}
