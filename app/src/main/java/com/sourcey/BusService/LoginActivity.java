package com.sourcey.BusService;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.support.v7.app.AppCompatActivity;

import android.content.Intent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.Bind;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private static final int REQUEST_SIGNUP = 0;
    private String email = null;
    private String password = null;
    ProgressDialog progressDialog;

    @Bind(R.id.input_email)
    EditText _emailText;
    @Bind(R.id.input_password)
    EditText _passwordText;
    @Bind(R.id.btn_login)
    Button _loginButton;
    @Bind(R.id.link_signup)
    TextView _signupLink;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        _loginButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                login();
            }
        });

        _signupLink.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Start the Signup activity
                Uri uri = Uri.parse("http://www.google.com");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });
    }

    public void login() {
        Log.d(TAG, "Login");

        if (!validate()) {
            onLoginFailed();
            return;
        }

        _loginButton.setEnabled(false);

        progressDialog = new ProgressDialog(LoginActivity.this,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Giriş yapılıyor...");
        progressDialog.show();


        email = _emailText.getText().toString();
        password = _passwordText.getText().toString();

        // TODO: Implement  authentication logic here.
        String URL = "http://192.168.0.10:8080/DemoService/login/";

        loginWithService(URL);
        //onLoginSuccess();

    }

    @Override
    public void onPause() {
        super.onPause();

        if ((progressDialog != null) && progressDialog.isShowing())
            progressDialog.dismiss();
        progressDialog = null;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SIGNUP) {
            if (resultCode == RESULT_OK) {

                // TODO: Implement successful signup logic here
                // By default we just finish the Activity and log them in automatically

                this.finish();
            }
        }
    }

    @Override
    public void onBackPressed() {
        // Disable going back to the MainActivity
        moveTaskToBack(true);
    }

    public void onLoginSuccess() {
        _loginButton.setEnabled(true);

        Intent intent = new Intent(LoginActivity.this, Maplayout.class);
        progressDialog.dismiss();
        startActivity(intent);
        finish();


    }

    public void onLoginFailed() {
        Toast.makeText(getBaseContext(), "Login failed", Toast.LENGTH_LONG).show();

        _loginButton.setEnabled(true);

        progressDialog.dismiss();


    }

    public boolean validate() {
        boolean valid = true;

        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _emailText.setError("enter a valid email address");
            valid = false;
        } else {
            _emailText.setError(null);
        }

        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
            _passwordText.setError("between 4 and 10 alphanumeric characters");
            valid = false;
        } else {
            _passwordText.setError(null);
        }

        return valid;
    }

    public void loginWithService(String url) {

        LoginTask loginTask = new LoginTask();
        loginTask.execute(url);
    }

    public JSONObject getLoginJSON() {
        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.accumulate("email", email);
            jsonObject.accumulate("password", password);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }


    protected class LoginTask extends AsyncTask<String, Void, ArrayList<String>> {


        InputStream inputStream = null;

        String result = "";


        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected ArrayList<String> doInBackground(String... params) {

            ServiceHandler serviceHandler = new ServiceHandler();
                JSONObject valuePairs = null;

            if (email != null && password != null) {

                valuePairs = getLoginJSON();

            }
            int response = serviceHandler.authenticateUser(params[0], valuePairs);
            ArrayList<String> strings = new ArrayList<>();

            strings.add(String.valueOf(response));

            return strings;
        }


        @Override
        protected void onPostExecute(ArrayList<String> s) {
            super.onPostExecute(s);
            if (s.get(0).equals("200")) {


                onLoginSuccess();
            } else {
                onLoginFailed();
                return;
            }
        }

    }
}

