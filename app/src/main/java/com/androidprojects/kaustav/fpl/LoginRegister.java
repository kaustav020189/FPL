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
import com.androidprojects.kaustav.fpl.AppConfig;
import com.androidprojects.kaustav.fpl.AppController;
import com.androidprojects.kaustav.helper.SQLiteHandler;
import com.androidprojects.kaustav.helper.SessionManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LoginRegister extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = LoginRegister.class.getSimpleName();
    private TextView textView1, textView2, textView3;
    private RelativeLayout relativeLayoutLogin, relativeLayoutRegister;
    private EditText loginEmail, loginPassword, registerEmail, registerPassword, registerName, registerConfirmPassword;
    private Button btnLogin,btnRegister;

    private ProgressDialog pDialog;

    private SessionManager session;
    private SQLiteHandler db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_register);

        textView1 = (TextView) findViewById(R.id.NotRegisteredLink);
        textView2 = (TextView) findViewById(R.id.AlreadyRegisteredLink);
        textView3 = (TextView) findViewById(R.id.ForgotPassword);
        relativeLayoutLogin = (RelativeLayout) findViewById(R.id.LoginForm);
        relativeLayoutRegister = (RelativeLayout) findViewById(R.id.RegisterForm);

        textView1.setOnClickListener(this);
        textView2.setOnClickListener(this);
        textView3.setOnClickListener(this);

        loginEmail = (EditText) findViewById(R.id.login_input_email);
        loginPassword = (EditText) findViewById(R.id.login_input_password);
        registerEmail = (EditText) findViewById(R.id.input_email);
        registerPassword = (EditText) findViewById(R.id.input_password);
        registerName = (EditText) findViewById(R.id.input_name);
        registerConfirmPassword = (EditText) findViewById(R.id.input_confirm_password);

        btnLogin = (Button) findViewById(R.id.loginbutton);
        btnRegister = (Button) findViewById(R.id.registerbutton);

        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        // SQLite database handler
        db = new SQLiteHandler(getApplicationContext());

        // Session manager
        session = new SessionManager(getApplicationContext());

        // Check if user is already logged in or not
        if (session.isLoggedIn()) {
            // User is already logged in. Take him to main activity
            Intent intent = new Intent(LoginRegister.this, Home.class);
            startActivity(intent);
            finish();
        }

        // Login button Click Event
        btnLogin.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                String email = loginEmail.getText().toString().trim();
                String password = loginPassword.getText().toString().trim();

                // Check for empty data in the form
                if (!email.isEmpty() && !password.isEmpty()) {
                    // Check if email field is entered correctly
                    if(isValidEmail(email)){
                        // Check if phone has internet connected
                        if(isOnline()){
                            // login user
                            checkLogin(email, password);                                 // CUSTOM method
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
                }
                else {
                    Toast.makeText(getApplicationContext(),
                            "Oops! Some fields are empty...", Toast.LENGTH_LONG)
                            .show();
                }
            }

        });

        // Register button click event
        btnRegister.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                String name = registerName.getText().toString().trim();
                String email = registerEmail.getText().toString().trim();
                String password = registerPassword.getText().toString().trim();
                String confirmPassword = registerConfirmPassword.getText().toString().trim();

                if (!name.isEmpty() && !email.isEmpty() && !password.isEmpty() && !confirmPassword.isEmpty()) {
                    // Check password validation
                    if(password.equals(confirmPassword)){
                        // Check email valid or not
                        if(isValidEmail(email)){
                            // Check if phone has internet connected
                            if(isOnline()){
                                // register user
                                registerUser(name, email, password);                        // CUSTOM method
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
                    }
                    else{
                        Toast.makeText(getApplicationContext(),
                                "The two password fields do not match!", Toast.LENGTH_LONG)
                                .show();
                    }
                }
                else {
                    Toast.makeText(getApplicationContext(),
                            "Oops! Some fields are empty...", Toast.LENGTH_LONG)
                            .show();
                }
            }
        });

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.NotRegisteredLink:
                // NotRegisteredLink clicked
                relativeLayoutLogin.setVisibility(View.GONE);
                relativeLayoutRegister.setVisibility(View.VISIBLE);
                overridePendingTransition(R.anim.animate_left_to_right, R.anim.animate_right_to_left);
                break;
            case R.id.AlreadyRegisteredLink:
                // AlreadyRegisteredLink clicked
                relativeLayoutLogin.setVisibility(View.VISIBLE);
                relativeLayoutRegister.setVisibility(View.GONE);
                overridePendingTransition(R.anim.animate_left_to_right, R.anim.animate_right_to_left);
                break;
            case R.id.ForgotPassword:
                Intent intent = new Intent(getApplicationContext(),Forgot.class);
                startActivity(intent);
                finish();
        }
    }


    /**
     * function to verify login details in mysql db
     * */
    private void checkLogin(final String email, final String password) {
        // Tag used to cancel the request
        String tag_string_req = "req_login";

        pDialog.setMessage("Logging in ...");
        showDialog();

        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_LOGIN, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Login Response: " + response.toString());
                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");

                    // Check for error node in json
                    if (!error) {

                        // Now store the user in SQLite
                        //String uid = jObj.getString("uid");

                        JSONObject user = jObj.getJSONObject("user");
                        Integer id = user.getInt("userid");
                        String name = user.getString("username");
                        String email = user.getString("useremail");
                        Integer approvedstatus = user.getInt("approvedstatus");

                        if(approvedstatus == 1) {
                            // user successfully logged in
                            // Create login session
                            session.setLogin(true);

                            // Inserting row in users table
                            db.addUser(id, name, email);

                            // Launch main activity
                            Intent intent = new Intent(LoginRegister.this, Home.class);
                            startActivity(intent);
                            finish();
                        }
                        else{
                            String notApprovedYet = "Your account not yet approved";
                            // USer not yet approved by admin
                            Toast.makeText(getApplicationContext(),
                                    notApprovedYet, Toast.LENGTH_LONG).show();
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
                params.put("password", password);

                return params;
            }

        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    /**
     * Function to store user in MySQL database will post params(tag, name,
     * email, password) to register url
     * */
    private void registerUser(final String name, final String email,
                              final String password) {
        // Tag used to cancel the request
        String tag_string_req = "req_register";

        pDialog.setMessage("Registering ...");
        showDialog();

        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_REGISTER, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Register Response: " + response.toString());
                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    if (!error) {

                        JSONObject user = jObj.getJSONObject("user");
                        Integer id = user.getInt("userid");
                        String name = user.getString("username");
                        String email = user.getString("useremail");

                        Toast.makeText(getApplicationContext(), "Success! Try logging in", Toast.LENGTH_LONG).show();

                        // Launch login pane
                        relativeLayoutLogin.setVisibility(View.VISIBLE);
                        relativeLayoutRegister.setVisibility(View.GONE);
                        overridePendingTransition(R.anim.animate_left_to_right, R.anim.animate_right_to_left);
                    } else {

                        // Error occurred in registration. Get the error
                        // message
                        String errorMsg = jObj.getString("error_msg");
                        Toast.makeText(getApplicationContext(),
                                errorMsg, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Registration Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting params to register url
                Map<String, String> params = new HashMap<String, String>();
                params.put("name", name);
                params.put("email", email);
                params.put("password", password);

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
