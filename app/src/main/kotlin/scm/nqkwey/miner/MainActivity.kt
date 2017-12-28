package scm.nqkwey.miner

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.crashlytics.android.Crashlytics
import com.kwabenaberko.openweathermaplib.Units
import com.kwabenaberko.openweathermaplib.implementation.OpenWeatherMapHelper
import com.kwabenaberko.openweathermaplib.models.currentweather.CurrentWeather
import com.newtronlabs.easyexchange.EasyCurrency
import com.newtronlabs.easyexchange.EasyExchangeManager
import com.newtronlabs.easyexchange.ICurrencyExchange
import com.newtronlabs.easyexchange.ICurrencyExchangeCallback
import io.fabric.sdk.android.Fabric
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.math.roundToInt


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Fabric.with(this, Crashlytics())
        setContentView(R.layout.activity_main)
        textView.setOnClickListener { requestNewExchanges() }
        val safeSubscriber = object : Observer<CurrentWeather> {
            override fun onSubscribe(d: Disposable) {
                Log.d(this@MainActivity.javaClass.simpleName, "onSubscribe")
            }

            override fun onNext(weather: CurrentWeather) {
                onWeatherReceived(weather)
            }

            override fun onError(e: Throwable) {
                onWeatherError(e)
            }

            override fun onComplete() {
                Log.d(this@MainActivity.javaClass.simpleName, "Weather onComplete")
            }

        }
        initWeatherHelper().safeSubscribe(safeSubscriber)
    }

    override fun onResume() {
        super.onResume()
        requestNewExchanges()
    }

    private fun onWeatherReceived(weather: CurrentWeather) {
        val weatherText = (getString(R.string.current_weather_in_omsk)
                + "from: ${weather.main.tempMin}℃ to ${weather.main.tempMax}℃\n"
                + "description: ${weather.weatherArray[0].description} \n"
                + "wind speed: ${weather.wind.speed}")
        weatherTextView.text = weatherText
    }

    private fun onWeatherError(t: Throwable) {
        t.printStackTrace()
    }

    private fun initWeatherHelper(): Observable<CurrentWeather> {
        val weatherHelper = OpenWeatherMapHelper()
        with(weatherHelper) {
            setApiKey(getString(R.string.api_key))
            setUnits(Units.METRIC)
        }
        return Observable.create {
            weatherHelper.getCurrentWeatherByCityName("Omsk", object : OpenWeatherMapHelper.CurrentWeatherCallback {
                override fun onSuccess(currentWeather: CurrentWeather) {
                    if (!it.isDisposed)
                        it.onNext(currentWeather)
                    else
                        it.onError(IllegalStateException("The weather subscriber is disposed! AAAAAAAAA!!111"))
                }

                override fun onFailure(t: Throwable?) {
                    if (t != null)
                        it.onError(t)
                }

            })
        }
    }

    private fun requestNewExchanges() {
        exchangeObservable(EasyCurrency.BTC, EasyCurrency.RUB)
                .subscribe({
                    val roublesAmount = it.toAmount.roundToInt()
                    val text = "1 BTC = $roublesAmount ${it.toCurrency}"
                    exchangeObservable(EasyCurrency.RUB, EasyCurrency.USD)
                            .subscribe({
                                val newText: String = text + "\nAND ${(roublesAmount * it.toAmount).roundToInt()} ${it.toCurrency}"
                                if (!text.contains(EasyCurrency.USD.key, false)) {
                                    onUiThread(Runnable {
                                        textView.text = newText
                                    })
                                }
                            }, {
                                onUiThread(Runnable { textView.text = text })
                                handleError(it)
                            })
                    //onUiThread(Runnable { textView.text = text })
                }, { onUiThread(Runnable { handleError(it) }) })
    }

    private fun handleError(t: Throwable) {
        Toast.makeText(this@MainActivity, "Finished with the error: ${t.message}", Toast.LENGTH_SHORT).show()
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
