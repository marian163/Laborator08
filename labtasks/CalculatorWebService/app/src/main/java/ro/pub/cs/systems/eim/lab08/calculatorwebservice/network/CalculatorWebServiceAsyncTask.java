package ro.pub.cs.systems.eim.lab08.calculatorwebservice.network;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import java.io.IOException;

import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import ro.pub.cs.systems.eim.lab08.calculatorwebservice.general.Constants;

public class CalculatorWebServiceAsyncTask extends AsyncTask<String, Void, String> {

    private TextView resultTextView;

    public CalculatorWebServiceAsyncTask(TextView resultTextView) {
        this.resultTextView = resultTextView;
    }

    @Override
    protected String doInBackground(String... params) {
        String operator1 = params[0];
        String operator2 = params[1];
        String operation = params[2];
        int method = Integer.parseInt(params[3]);

        // validare: niciun operand nu poate lipsi
        if (operator1 == null || operator1.trim().isEmpty()
                || operator2 == null || operator2.trim().isEmpty()) {
            return Constants.ERROR_MESSAGE_EMPTY;
        }

        OkHttpClient client = new OkHttpClient();
        Request request;

        if (method == Constants.GET_OPERATION) {
            // GET: parametrii merg in URL -> ...expr_get.php?operation=plus&t1=3&t2=4
            HttpUrl url = HttpUrl.parse(Constants.GET_WEB_SERVICE_ADDRESS).newBuilder()
                    .addQueryParameter(Constants.OPERATION_ATTRIBUTE, operation)
                    .addQueryParameter(Constants.OPERATOR1_ATTRIBUTE, operator1)
                    .addQueryParameter(Constants.OPERATOR2_ATTRIBUTE, operator2)
                    .build();
            request = new Request.Builder().url(url).get().build();
        } else {
            // POST: parametrii merg in corpul cererii (form-urlencoded)
            RequestBody body = new FormBody.Builder()
                    .add(Constants.OPERATION_ATTRIBUTE, operation)
                    .add(Constants.OPERATOR1_ATTRIBUTE, operator1)
                    .add(Constants.OPERATOR2_ATTRIBUTE, operator2)
                    .build();
            request = new Request.Builder()
                    .url(Constants.POST_WEB_SERVICE_ADDRESS)
                    .post(body)
                    .build();
        }

        // execute() e blocant -> OK aici, suntem pe thread-ul de fundal
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                return "HTTP error: " + response.code();
            }
            return response.body().string();
        } catch (IOException e) {
            Log.e(Constants.TAG, "Request failed: " + e.getMessage());
            if (Constants.DEBUG) {
                e.printStackTrace();
            }
            return "Network error: " + e.getMessage();
        }
    }

    @Override
    protected void onPostExecute(String result) {
        // ruleaza pe thread-ul UI -> aici avem voie sa modificam TextView-ul
        resultTextView.setText(result);
    }

}
