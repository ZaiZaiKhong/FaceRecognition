package com.example.khong.facerecognition;

import android.content.Intent;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.khong.facerecognition.model.User;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.example.khong.facerecognition.Common.SERVER_IP;

public class RegisterActivity extends AppCompatActivity {

    @BindView(R.id.edtUsername) EditText edtUsername;
    @BindView(R.id.btnSubmit) Button btnSubmit;

    public int mIndex = 0;
    public static final int REQUEST_UPLOAD_FACE = 100;
    public static final String UPLOAD_FACE_URL = "http://" + SERVER_IP + "/api/uploadUserFace";
    public static final String UPLOAD_USER_DATA_URL = "http://" + SERVER_IP + "/api/uploadUserData";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.btnSubmit)
    public void onSubmitClick() {
        int i = 0;
        String username = edtUsername.getText().toString();
        User user = new User(username);
        postToServer(user);
        while (i < 3) {
            startActivityForResult(new Intent(MediaStore.ACTION_IMAGE_CAPTURE), REQUEST_UPLOAD_FACE);
            i++;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == REQUEST_UPLOAD_FACE) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            photo.compress(Bitmap.CompressFormat.PNG, 100, baos);
            byte[] imageBytes = baos.toByteArray();
            String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
            uploadToServer(encodedImage);
        }
    }

    private void uploadToServer(final String encodedImage) {
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest request = new StringRequest(Request.Method.POST, UPLOAD_FACE_URL
                , new Response.Listener<String>() {
            @Override
            public void onResponse(String response) { }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) { }
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

        mIndex++;
        if (mIndex == 2) {
            finish();
        }
    }

    private void postToServer(final User user) {
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest request = new StringRequest(Request.Method.POST, UPLOAD_USER_DATA_URL
                , new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Toast.makeText(getApplicationContext(), R.string.msg_notify, Toast.LENGTH_LONG).show();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("RESPONSE", "");
            }
        }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("username", user.getUsername());
                return params;
            }
        };
        queue.add(request);
    }
}
