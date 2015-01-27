package com.maicoin.android.checkout;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.maicoin.android.utils.CheckoutUtils;
import com.maicoin.android.utils.MaiCoinSingleton;
import com.maicoin.api.MaiCoin;
import com.maicoin.api.entity.Checkout;
import com.maicoin.api.entity.CheckoutParamBuilder;


public class MainActivity extends ActionBarActivity {

    interface CreateCheckoutListener {
        void onCheckoutCreated(Checkout checkout);
    }
    class CreateCheckout extends AsyncTask<Void, Void, Checkout>{
        private CreateCheckoutListener mListener;

        CreateCheckout(CreateCheckoutListener listener) {
            mListener = listener;
        }

        @Override
        protected Checkout doInBackground(Void... params) {
            Checkout result = null;
            try {
                MaiCoin maicoin = MaiCoinSingleton.getInstance();
                CheckoutParamBuilder builder = new CheckoutParamBuilder();
                builder.setCheckoutData("5", "twd", "http://my.com/return", "http://my.com/cancel",
                        "http://my.com/callback", "ref_id_001", "userid=10, desc=test", "zh-TW");
                builder.setBuyerData("YL", "apt 124", "road 456", "sf", "ca", "94305", "abc@gmail.com", "650444040", "tw");
                builder.addItem("test item1", "1234", "100", "twd", true);
                builder.addItem("test item2", "45", "300", "twd", false);
                result = maicoin.createCheckout(builder);
                Log.d(CreateCheckout.class.getSimpleName(), result.toString());
            } catch (Exception e){
                Log.e(MainActivity.class.getSimpleName(), "Exception:"+e.getMessage());
            }
            return result;
        }

        @Override
        protected void onPostExecute(Checkout checkout) {
            super.onPostExecute(checkout);
            mListener.onCheckoutCreated(checkout);
        }
    };

    private Button.OnClickListener mButtonListener = new Button.OnClickListener(){
        @Override
        public void onClick(View v) {
            if(v.getId() == R.id.button_checkout) {
                Toast.makeText(MainActivity.this, "Create checkout", Toast.LENGTH_SHORT).show();
                new CreateCheckout(mCheckoutListener).execute();
            }
        }
    };

    private CheckoutUtils.GetCheckoutStatusListener mCheckoutPaymentListener = new CheckoutUtils.GetCheckoutStatusListener() {
        @Override
        public void onGetStatus(Checkout checkout) {
            Toast.makeText(MainActivity.this, "Checkout status updated: " + checkout.getStatus(), Toast.LENGTH_SHORT).show();
        }
    };

    private CreateCheckoutListener mCheckoutListener = new CreateCheckoutListener() {
        @Override
        public void onCheckoutCreated(Checkout checkout) {
            if(checkout != null) {
                Toast.makeText(MainActivity.this, "Checkout created:"+checkout.getCheckoutUrl(), Toast.LENGTH_SHORT).show();
                CheckoutUtils.showDialog(MainActivity.this, "Payment", checkout, mCheckoutPaymentListener);
            } else {
                Toast.makeText(MainActivity.this, "Checkout not created", Toast.LENGTH_SHORT).show();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button checkoutButton = (Button) findViewById(R.id.button_checkout);
        checkoutButton.setOnClickListener(mButtonListener);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
