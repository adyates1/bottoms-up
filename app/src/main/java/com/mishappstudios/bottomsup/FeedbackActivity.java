package com.mishappstudios.bottomsup;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

import static com.mishappstudios.bottomsup.Constants.FEEDBACK_SERVER_URL;
import static com.mishappstudios.bottomsup.Constants.SERVER_VERIFICATION_KEY;

/**
 * Class for giving feedback via e-mail form
 */
public class FeedbackActivity extends ImmersiveSlidingAppCompatActivity {

    /**
     * Called when the Activity is created
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback_form);

    }

    /**
     * Called when the user submits the form
     * @param view
     */
    public void feedbackSubmitClicked(View view){
        final String name = ((EditText) findViewById(R.id.feedBackNameTxt)).getText().toString();
        final String feedback = ((EditText) findViewById(R.id.feedbackFeedbackTxt)).getText().toString();
        final String id = BottomsUpStorageHelper.getFirebaseInstanceID(getApplicationContext());
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest postRequest = new StringRequest(Request.Method.POST, FEEDBACK_SERVER_URL,
                response -> {
                    // response
                    Log.d("Response", response);
                    Toast.makeText(getApplicationContext(), response, Toast.LENGTH_LONG).show();
                },
                error -> {
                    // error
                    Log.d("Error.Response", error.getMessage());
                    Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("fullname", name);
                params.put("getVerifyKey", SERVER_VERIFICATION_KEY);
                params.put("instanceid", id);
                params.put("Tmessage", feedback);
                return params;
            }

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<>();
                params.put("Content-Type", "application/x-www-form-urlencoded");
                return params;
            }
        };
        queue.add(postRequest);
        finish();
    }
}
