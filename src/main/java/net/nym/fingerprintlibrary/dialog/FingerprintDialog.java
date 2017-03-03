/*
 * Copyright (c) 2017  Ni YueMing<niyueming@163.com>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 */

package net.nym.fingerprintlibrary.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import net.nym.fingerprintlibrary.FingerPrintUtils;
import net.nym.fingerprintlibrary.R;
import net.nym.fingerprintlibrary.util.VibratorUtils;

import java.lang.ref.WeakReference;

/**
 * @author niyueming
 * @date 2017-03-02
 * @time 13:48
 */

public class FingerprintDialog extends Dialog {
    private TextView txt_title;
    private Button btn_password;
    private ImageView fingerprint;
    private OnClickListener mOnClickListener;
    private CancellationSignal mCancellationSignal;
    private String mTitle;
    private RevisionHandler mHandler;
    private OnFingerPrintCallback mOnFingerPrintCallback;


    public FingerprintDialog(Context context) {
        super(context);
    }

    public FingerprintDialog(Context context, int themeResId) {
        super(context, themeResId);
    }

    protected FingerprintDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_fingerprint);
        txt_title = (TextView) findViewById(R.id.title);
        txt_title.setText(mTitle != null ? mTitle: getContext().getText(R.string.fp_unlock_title));
        btn_password = (Button) findViewById(R.id.to_password);
        btn_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnClickListener != null){
                    mOnClickListener.onClick(FingerprintDialog.this,-1);
                }
                onBackPressed();
            }
        });
        fingerprint = (ImageView) findViewById(R.id.fingerprint);
        mTitle = txt_title.getText().toString().trim();
        setCanceledOnTouchOutside(false);

        setWindowWidth();

        mHandler = new RevisionHandler(this);
        if (!FingerPrintUtils.check(getContext())){
            exit();
            return;
        }

        mCancellationSignal = new CancellationSignal();
        FingerprintManager.AuthenticationCallback mAuthenticationCallback = new FingerprintManager.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                System.out.println("errString=" + errString + ",errorCode=" + errorCode);
                txt_title.setText(errString);
                exit();
            }

            @Override
            public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
                System.out.println("helpString=" + helpString + ",helpCode=" + helpCode);
                super.onAuthenticationHelp(helpCode, helpString);
                txt_title.setText(helpString);
                VibratorUtils.Vibrate(getContext(), 200);
                revision();
            }

            @Override
            public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                System.out.println("onAuthenticationSucceeded=" + result.toString());
                if (mOnFingerPrintCallback != null) {
                    mOnFingerPrintCallback.onAuthenticationSucceeded(result);
                }
                onBackPressed();
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                System.out.println("onAuthenticationFailed");
                txt_title.setText(R.string.fp_try_again);
                revision();
            }
        };
        FingerPrintUtils.authenticate(getContext(),null,mCancellationSignal, mAuthenticationCallback);
    }

    /**
     * 让dialog的宽为屏幕宽的80%
     */
    private void setWindowWidth() {
        Window dialogWindow = getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        DisplayMetrics d = getContext().getResources().getDisplayMetrics(); // 获取屏幕宽、高用
        lp.width = (int) (d.widthPixels * 0.8); //
        dialogWindow.setAttributes(lp);
    }

    private void exit() {
        mHandler.sendEmptyMessageDelayed(1,1000);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        AnimationDrawable animationDrawable = (AnimationDrawable) fingerprint.getDrawable();
        animationDrawable.start();
    }

    public void setOnClickListener(OnClickListener onClickListener) {
        this.mOnClickListener = onClickListener;
    }

    public void setOnFingerPrintCallback(OnFingerPrintCallback onFingerPrintCallback) {
        this.mOnFingerPrintCallback = onFingerPrintCallback;
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = String.valueOf(title);
    }

    @Override
    public void setTitle(int titleId) {
        mTitle = String.valueOf(getContext().getText(titleId));
    }

    @Override
    public void dismiss() {
        if (mCancellationSignal != null){
            mCancellationSignal.cancel();
            mCancellationSignal = null;
        }
        super.dismiss();
    }

    @Override
    public void show() {

        super.show();
    }

    private void revision(){
        Message msg = mHandler.obtainMessage();
        msg.what = 0;
        msg.obj = mTitle;
        mHandler.sendMessageDelayed(msg,1 * 1000);
    }

    private static class RevisionHandler extends Handler {
        WeakReference<FingerprintDialog> mDialog;

        RevisionHandler(FingerprintDialog dialog) {
            mDialog = new WeakReference<FingerprintDialog>(dialog);
        }

        @Override
        public void handleMessage(Message msg) {
            FingerprintDialog dialog = mDialog.get();
            if (dialog == null){
                return;
            }
            switch (msg.what){
                case 0:
                    String title = String.valueOf(msg.obj);
                    dialog.setTitle(title);
                    break;
                case 1:
                    dialog.onBackPressed();
                    break;
            }
        }
    }

    public interface OnFingerPrintCallback{
        void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result);
    }

}
