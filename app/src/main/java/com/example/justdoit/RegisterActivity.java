package com.example.justdoit;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.example.justdoit.network.RetrofitClient;
import com.example.justdoit.utils.FileUtil;
import com.example.justdoit.utils.MyLogger;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends BaseActivity {

    private EditText firstNameInput, lastNameInput, emailInput, passwordInput;
    private ImageView imagePreview;
    private Uri selectedImageUri;

    private final ActivityResultLauncher<String> imagePicker =
            registerForActivityResult(
                    new ActivityResultContracts.GetContent(),
                    uri -> {
                        if (uri != null) {
                            selectedImageUri = uri;
                            imagePreview.setImageURI(uri);
                        }
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        firstNameInput = findViewById(R.id.firstName);
        lastNameInput  = findViewById(R.id.lastName);
        emailInput     = findViewById(R.id.email);
        passwordInput  = findViewById(R.id.password);
        imagePreview   = findViewById(R.id.imagePreview);

        findViewById(R.id.selectImage)
                .setOnClickListener(v -> imagePicker.launch("image/*"));
    }

    public void onRegisterClick(View view) {
        String fn = firstNameInput.getText().toString().trim();
        String ln = lastNameInput.getText().toString().trim();
        String em = emailInput.getText().toString().trim();
        String pw = passwordInput.getText().toString().trim();

        if (fn.isEmpty() || ln.isEmpty() || em.isEmpty() || pw.isEmpty()) {
            MyLogger.toast("Заповніть усі поля");
            return;
        }

        if (selectedImageUri == null) {
            MyLogger.toast("Додайте зображення");
            return;
        }

        uploadRegister(fn, ln, em, pw, selectedImageUri);
    }

    private void uploadRegister(String fn, String ln, String em, String pw, Uri uri) {
        RequestBody fnPart = RequestBody.create(fn, MultipartBody.FORM);
        RequestBody lnPart = RequestBody.create(ln, MultipartBody.FORM);
        RequestBody emPart = RequestBody.create(em, MultipartBody.FORM);
        RequestBody pwPart = RequestBody.create(pw, MultipartBody.FORM);

        MultipartBody.Part imagePart = null;
        String path = FileUtil.getImagePath(this, uri);
        if (path != null) {
            File file = new File(path);
            RequestBody body =
                    RequestBody.create(MediaType.parse("multipart/form-data"), file);
            imagePart = MultipartBody.Part.createFormData(
                    "ImageFile", file.getName(), body);
        }

        RetrofitClient.getInstance()
                .getAuthApi()
                .register(fnPart, lnPart, emPart, pwPart, imagePart)
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            MyLogger.toast("Реєстрація успішна");
                            finish();
                        } else {
                            MyLogger.toast("Помилка сервера: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        MyLogger.toast("Помилка: " + t.getMessage());
                    }
                });
    }
}
