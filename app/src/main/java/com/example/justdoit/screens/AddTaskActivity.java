package com.example.justdoit.screens;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.example.justdoit.BaseActivity;
import com.example.justdoit.R;
import com.example.justdoit.dto.zadachi.ZadachaItemDTO;
import com.example.justdoit.network.RetrofitClient;
import com.example.justdoit.utils.CommonUtils;
import com.example.justdoit.utils.ImagePickerCropper;
import com.example.justdoit.utils.MyLogger;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.example.justdoit.utils.validation.logic.FieldValidator;
import com.example.justdoit.utils.validation.logic.FormValidator;
import com.example.justdoit.utils.validation.rules.RequiredRule;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class AddTaskActivity extends BaseActivity {

    private TextInputLayout titleLayout;
    private TextInputEditText titleInput;

    private ImageView imagePreview;
    private Uri selectedImageUri;

    private ImagePickerCropper imagePicker;
    private FormValidator formValidator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        initViews();
        initValidator();
        initImagePicker();
    }

    private void initViews() {
        titleLayout = findViewById(R.id.taskTitleLayout);
        titleInput  = findViewById(R.id.taskTitleInput);
        imagePreview = findViewById(R.id.taskImagePreview);
    }

    private void initValidator() {
        formValidator = new FormValidator()
                .addField(
                        new FieldValidator(titleLayout, titleInput)
                                .addRule(new RequiredRule("Введіть назву задачі"))
                );
    }

    private void initImagePicker() {
        imagePicker = new ImagePickerCropper(this);

        findViewById(R.id.chooseImageButton).setOnClickListener(v ->
                imagePicker.pick(uri -> {
                    selectedImageUri = uri;
                    imagePreview.setImageURI(uri);
                })
        );
    }

    public void onSaveClick(View view) {

        if (!formValidator.validate()) return;

        if (selectedImageUri == null) {
            MyLogger.toast("Додайте зображення");
            return;
        }

        uploadTask(titleInput.getText().toString().trim(), selectedImageUri);
    }

    private void uploadTask(String title, Uri imageUri) {

        RequestBody titlePart =
                RequestBody.create(title, MultipartBody.FORM);

        MultipartBody.Part imagePart = createImagePart(imageUri);
        if (imagePart == null) {
            MyLogger.toast("Не вдалося підготувати зображення");
            return;
        }

        CommonUtils.showLoading();

        RetrofitClient.getInstance()
                .getZadachiApi()
                .create(titlePart, imagePart)
                .enqueue(new Callback<ZadachaItemDTO>() {
                    @Override
                    public void onResponse(Call<ZadachaItemDTO> call,
                                           Response<ZadachaItemDTO> response) {
                        CommonUtils.hideLoading();

                        if (response.isSuccessful()) {
                            MyLogger.toast("Задача створена");
                            goToMain();
                        } else {
                            MyLogger.toast("Помилка сервера: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<ZadachaItemDTO> call, Throwable t) {
                        CommonUtils.hideLoading();
                        MyLogger.toast("Помилка: " + t.getMessage());
                    }
                });
    }

    private MultipartBody.Part createImagePart(Uri uri) {
        try (InputStream is = getContentResolver().openInputStream(uri);
             ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {

            byte[] data = new byte[8192];
            int n;
            while ((n = is.read(data)) != -1) {
                buffer.write(data, 0, n);
            }

            RequestBody body = RequestBody.create(
                    MediaType.parse("image/*"),
                    buffer.toByteArray()
            );

            return MultipartBody.Part.createFormData(
                    "image",
                    "task.jpg",
                    body
            );

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
