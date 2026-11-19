package ro.pub.cs.systems.eim.lab08.calculatorwebservice.network

import android.os.AsyncTask
import android.util.Log
import android.widget.TextView
import java.io.IOException
import okhttp3.FormBody
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import ro.pub.cs.systems.eim.lab08.calculatorwebservice.general.Constants

class CalculatorWebServiceAsyncTask(private val resultTextView: TextView) : AsyncTask<String, Void, String>() {

    override fun doInBackground(vararg params: String?): String? {
        val operator1 = params[0]
        val operator2 = params[1]
        val operation = params[2] ?: return null
        val method = params[3]?.toInt() ?: 0

        if (operator1.isNullOrBlank() || operator2.isNullOrBlank()) {
            return Constants.ERROR_MESSAGE_EMPTY
        }

        val client = OkHttpClient()

        val request = if (method == Constants.GET_OPERATION) {
            //GET
            val url = Constants.GET_WEB_SERVICE_ADDRESS.toHttpUrl().newBuilder()
                .addQueryParameter(Constants.OPERATION_ATTRIBUTE, operation)
                .addQueryParameter(Constants.OPERATOR1_ATTRIBUTE, operator1)
                .addQueryParameter(Constants.OPERATOR2_ATTRIBUTE, operator2)
                .build()
            Request.Builder().url(url).get().build()
        } else {
            //POST
            val body = FormBody.Builder()
                .add(Constants.OPERATION_ATTRIBUTE, operation)
                .add(Constants.OPERATOR1_ATTRIBUTE, operator1)
                .add(Constants.OPERATOR2_ATTRIBUTE, operator2)
                .build()
            Request.Builder().url(Constants.POST_WEB_SERVICE_ADDRESS).post(body).build()
        }

        return try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) "HTTP error: ${response.code}"
                else response.body?.string()
            }
        } catch (e: IOException) {
            Log.e(Constants.TAG, "Request failed: ${e.message}")
            "Network error: ${e.message}"
        }
    }

    override fun onPostExecute(result: String?) {
        resultTextView.text = result
    }
}
