package com.maicoin.android.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.AsyncTask;
import android.util.Log;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.maicoin.android.checkout.R;
import com.maicoin.api.MaiCoin;
import com.maicoin.api.entity.Checkout;

import java.util.Date;

/**
 * Created by yutelin on 1/19/15.
 */
public class CheckoutUtils {

    public interface  GetCheckoutStatusListener {
        void onGetStatus(Checkout checkout);
    }

    private static class GetCheckoutStatus extends AsyncTask<Void, Void, Checkout>{
        private Checkout mCheckout;
        private Date mExpiredAt;
        private GetCheckoutStatusListener mListener;
        private Dialog mDialog;
        GetCheckoutStatus(Checkout checkout, Dialog dialog, GetCheckoutStatusListener listener){
            mCheckout = checkout;
            mDialog = dialog;
            mExpiredAt = new Date(checkout.getCreatedAt().getTime() + 15*60*1000); //15 minutes
            mListener = listener;
        }

        @Override
        protected Checkout doInBackground(Void... params) {
            Checkout result = null;
            MaiCoin maicoin = MaiCoinSingleton.getInstance();
            Date now = new Date();
            while(now.before(mExpiredAt)) {
                try{
                    result = maicoin.getCheckout(mCheckout.getUid());
                    Log.d(GetCheckoutStatus.class.getSimpleName(), result.toString());
                    if(!result.getStatus().equalsIgnoreCase("unpaid")) {
                        break;
                    } else {
                        Thread.sleep(5000);
                    }
                } catch (Exception e){
                    Log.e(GetCheckoutStatus.class.getSimpleName(), e.getMessage());
                    break;
                }
                now = new Date();
            }
            return result;
        }

        @Override
        protected void onPostExecute(Checkout checkout) {
            super.onPostExecute(checkout);
            mDialog.dismiss();
            mListener.onGetStatus(checkout);
        }
    }

    public static void showDialog(final Activity activity, final String title, Checkout checkout, GetCheckoutStatusListener checkoutListener) {
        final Dialog authDialog;
        //Poll checkout status
        authDialog = new Dialog(activity);
        final GetCheckoutStatus getCheckoutStatus = new GetCheckoutStatus(checkout, authDialog, checkoutListener);
        getCheckoutStatus.execute();
        DialogInterface.OnCancelListener onCancelListener = new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                getCheckoutStatus.cancel(true);
            }
        };
        DialogInterface.OnDismissListener onDismissListener = new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                getCheckoutStatus.cancel(true);
            }
        };
        authDialog.setOnCancelListener(onCancelListener);
        authDialog.setOnDismissListener(onDismissListener);
        authDialog.setContentView(R.layout.checkout_dialog);
        WebView oauthWebView = (WebView)authDialog.findViewById(R.id.web_view_checkout);
        oauthWebView.setWebViewClient(new SSLTolerentWebViewClient());
        oauthWebView.getSettings().setJavaScriptEnabled(true);
        String checkoutUrl = checkout.getCheckoutUrl().replace("127.0.0.1", "10.0.3.2");
        oauthWebView.loadUrl(checkoutUrl);
        oauthWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onLoadResource(WebView view, String url) {
                super.onLoadResource(view, url);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
            }


        });
        authDialog.show();
        authDialog.setTitle(title);
        authDialog.setCancelable(true);

    }

    private static class SSLTolerentWebViewClient extends WebViewClient {

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            handler.proceed(); // Ignore SSL certificate errors
        }

    }
}
