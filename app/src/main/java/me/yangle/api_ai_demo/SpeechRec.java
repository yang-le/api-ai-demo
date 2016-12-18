package me.yangle.api_ai_demo;

import android.content.Context;

import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;

public class SpeechRec {
    public interface SpeechRecListener
    {
        public void onSpeechResult(String result, boolean isLast);
        public void onSpeechError(String error);
    }

    public static void doSpeechRec(Context context, final SpeechRecListener listener)
    {
        //1.创建SpeechRecognizer对象，第二个参数：本地听写时传InitListener
        RecognizerDialog iatDialog = new RecognizerDialog(context, null);
        //2.设置听写参数，同上节
        iatDialog.setParameter(SpeechConstant.DOMAIN, "iat");
        iatDialog.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
        iatDialog.setParameter(SpeechConstant.ACCENT, "mandarin");
        //3.设置回调接口
        iatDialog.setListener(new RecognizerDialogListener() {
            @Override
            public void onResult(RecognizerResult recognizerResult, boolean b) {
                String json = recognizerResult.getResultString();
                String str = SpeechJsonParser.parseIatResult(json);
                listener.onSpeechResult(str, b);
            }

            @Override
            public void onError(SpeechError speechError) {
                listener.onSpeechError(speechError.getPlainDescription(true));
            }
        });
        //4.开始听写
        iatDialog.show();
    }
}
