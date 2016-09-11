package com.androidprojects.kaustav.fpl;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.androidprojects.kaustav.helper.SQLiteHandler;
import com.androidprojects.kaustav.helper.SessionManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Forgot extends AppCompatActivity{

    private static final String TAG = Forgot.class.getSimpleName();
    private EditText forgot_input_email;
    private Button sendForgotEmailButton;

    private ProgressDialog pDialog;

    private SessionManager session;
    private SQLiteHandler db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot);

        forgot_input_email = (EditText) findViewById(R.id.forgot_input_email);

        sendForgotEmailButton = (Button) findViewById(R.id.sendForgotEmailButton);

        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        // SQLite database handler
        db = new SQLiteHandler(getApplicationContext());

        // Session manager
        session = new SessionManager(getApplicationContext());

        // Forgot Password send email button Click Event
        sendForgotEmailButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                String email = forgot_input_email.getText().toString().trim();

                // Check for empty data in the form
                if (!email.isEmpty()) {
                    // Check if email field is entered correctly
                    if(isValidEmail(email)){
                        // Check if phone has internet connected
                        if(isOnline()){
                            // login user
                            sendResetEmail(email);                                 // CUSTOM method
                        }
                        else{
                            // Prompt user enable internet first
                            Toast.makeText(getApplicationContext(),
                                    "Not connected to the Internet!", Toast.LENGTH_LONG)
                                    .show();
                        }
                    }
                    else{
                        // Prompt user enable internet first
                        Toast.makeText(getApplicationContext(),
                                "Not a valid email ID!", Toast.LENGTH_LONG)
                                .show();
                    }
                } else {
                    // Prompt user to enter credentials
                    Toast.makeText(getApplicationContext(),
                            "Please fill in the email field!", Toast.LENGTH_LONG)
                            .show();
                }
            }

        });

    }


    /**
     * function to send password reset email and show confirmation...then close activity and go to Login
     * */
    private void sendResetEmail(final String email) {
        // Tag used to cancel the request
        String tag_string_req = "send_reset_email";

        pDialog.setMessage("Sending email ...");
        showDialog();

        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_FORGOT, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "PasswordReset Mail response: " + response.toString());
                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");

                    // Check for error node in json
                    if (!error) {

                        // Reset mail has been sent successfully
                        String errorMsg = jObj.getString("mail");

                        if(errorMsg.equals("true")){     // true message recieved from PHP mail function code
                            Toast.makeText(getApplicationContext(),
                                    "Check mail for password reset", Toast.LENGTH_LONG).show();

                            // Launch main activity
                            Intent intent = new Intent(Forgot.this,LoginRegister.class);
                            startActivity(intent);
                            finish();
                        }
                        else{

                        }
                    } else {
                        // Error in login. Get the error message
                        String errorMsg = jObj.getString("error_msg");
                        Toast.makeText(getApplicationContext(),
                                errorMsg, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    // JSON error
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Login Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to login url
                Map<String, String> params = new HashMap<String, String>();
                params.put("email", email);

                return params;
            }

        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }


    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }

    //custom function
    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        }
        return false;
    }

    // email validator
    public final static boolean isValidEmail(CharSequence target) {
        if (TextUtils.isEmpty(target)) {
            return false;
        } else {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
        }
    }
}
