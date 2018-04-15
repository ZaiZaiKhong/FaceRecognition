package com.example.khong.facerecognition;

import android.content.Intent;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.example.khong.facerecognition.Common.SERVER_IP;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.btnCheckIn) Button btnCheckIn;
    @BindView(R.id.btnCheckOut) Button btnCheckOut;

    public static final int REQUEST_CHECK_IN = 1;
    public static final int REQUEST_CHECK_OUT = 2;
    public static final String CHECK_IN_URL = "http://" + SERVER_IP + "/api/checkin";
    public static final String CHECK_OUT_URL = "http://" + SERVER_IP + "/api/checkout";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.btnCheckIn)
    public void onBtnCheckInClick() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_CHECK_IN);
    }

    @OnClick(R.id.btnCheckOut)
    public void onBtnCheckOutClick() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_CHECK_OUT);
    }

    @OnClick(R.id.btnRegister)
    public void onBtnRegisterClick() {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
                Bitmap photo = (Bitmap) data.getExtras().get("data");
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                photo.compress(Bitmap.CompressFormat.PNG, 100, baos);
                byte[] imageBytes = baos.toByteArray();
                String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
            if (requestCode == REQUEST_CHECK_IN) {
                postToServer(encodedImage, CHECK_IN_URL, "Welcome back! ");
            } else if(requestCode == REQUEST_CHECK_OUT) {
                postToServer(encodedImage, CHECK_OUT_URL, "Goodbye! ");
            }
        }
    }

    private void postToServer(final String encodedImage, String url, final String message) {
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest request = new StringRequest(Request.Method.POST, url
                , new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                JsonParser parser = new JsonParser();
                JsonObject json = parser.parse(response).getAsJsonObject();

                if(json.get("status").getAsString().equals("OK")) {
                    Toast.makeText(getApplicationContext(), message + json.getAsJsonObject("data").get("username").getAsString() ,Toast.LENGTH_LONG).show();
                } else if(json.get("status").getAsString().equals("FAIL")) {
                    Toast.makeText(getApplicationContext(), R.string.msg_fail, Toast.LENGTH_LONG).show();
                }
            }
        }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(getApplicationContext(), R.string.msg_error, Toast.LENGTH_LONG).show();
                }
            }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("image", encodedImage);
                return params;
            }
        };
        queue.add(request);
    }
}
