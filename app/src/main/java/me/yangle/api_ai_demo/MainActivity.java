package me.yangle.api_ai_demo;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import ai.api.AIDataService;
import ai.api.AIServiceException;
import ai.api.android.AIConfiguration;
import ai.api.android.GsonFactory;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;
import ai.api.model.Metadata;
import ai.api.model.Result;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechUtility;

import java.util.HashMap;
import java.util.Map;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getName();
    private Gson gson = GsonFactory.getGson();
    private Button listenButton;
    private TextView resultTextView;
    private AIDataService aiDataService;
    private AIRequest aiRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SpeechUtility.createUtility(this, SpeechConstant.APPID + getString(R.string.speechAppId));

        final AIConfiguration config = new AIConfiguration(getString(R.string.aiAccessToken),
                AIConfiguration.SupportedLanguages.ChineseChina,
                AIConfiguration.RecognitionEngine.System);

        TTS.init(this);

        aiDataService = new AIDataService(config);
        aiRequest = new AIRequest();

        listenButton = (Button) findViewById(R.id.listenButton);
        resultTextView = (TextView) findViewById(R.id.resultTextView);
    }

    public void listenButtonOnClick(final View view) {
        SpeechRec.doSpeechRec(this, new SpeechRec.SpeechRecListener() {
            private String resultString = "";

            @Override
            public void onSpeechResult(String result, boolean isLast) {
                if (isLast) {
                    Log.d("SpeechRec", resultString);
                    aiRequest.setQuery(resultString);
                    new AsyncTask<AIRequest, Void, AIResponse>() {
                        @Override
                        protected AIResponse doInBackground(AIRequest... requests) {
                            final AIRequest request = requests[0];
                            try {
                                final AIResponse response = aiDataService.request(aiRequest);
                                return response;
                            } catch (AIServiceException e) {
                                e.printStackTrace();
                            }
                            return null;
                        }

                        @Override
                        protected void onPostExecute(final AIResponse response) {
                            if (response != null) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Log.d(TAG, "onResult");

                                        resultTextView.setText(gson.toJson(response));

                                        Log.i(TAG, "Received success response");

                                        // this is example how to get different parts of result object
                                        //final Status status = response.getStatus();
                                        //Log.i(TAG, "Status code: " + status.getCode());
                                        //Log.i(TAG, "Status type: " + status.getErrorType());

                                        final Result result = response.getResult();
                                        Log.i(TAG, "Resolved query: " + result.getResolvedQuery());

                                        Log.i(TAG, "Action: " + result.getAction());
                                        final String speech = result.getFulfillment().getSpeech();
                                        Log.i(TAG, "Speech: " + speech);
                                        TTS.speak(speech);

                                        final Metadata metadata = result.getMetadata();
                                        if (metadata != null) {
                                            Log.i(TAG, "Intent id: " + metadata.getIntentId());
                                            Log.i(TAG, "Intent name: " + metadata.getIntentName());
                                        }

                                        final HashMap<String, JsonElement> params = result.getParameters();
                                        if (params != null && !params.isEmpty()) {
                                            Log.i(TAG, "Parameters: ");
                                            for (final Map.Entry<String, JsonElement> entry : params.entrySet()) {
                                                Log.i(TAG, String.format("%s: %s", entry.getKey(), entry.getValue().toString()));
                                            }
                                        }
                                    }

                                });
                            }
                        }
                    }.execute(aiRequest);
                } else {
                    resultString += result;
                }
            }

            @Override
            public void onSpeechError(String error) {
                Log.e("SpeechRec", error);
            }
        });
    }
}
