package com.example.justdoit;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.justdoit.dto.zadachi.ZadachaItemDTO;
import com.example.justdoit.network.RetrofitClient;
import com.example.justdoit.utils.FileUtil;
import com.example.justdoit.utils.UriRequestBody;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddTaskActivity extends AppCompatActivity {

    private EditText titleInput;
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
        setContentView(R.layout.activity_add_task);

        titleInput = findViewById(R.id.taskTitleInput);
        imagePreview = findViewById(R.id.taskImagePreview);

        findViewById(R.id.chooseImageButton)
                .setOnClickListener(v -> imagePicker.launch("image/*"));

        findViewById(R.id.saveTaskButton)
                .setOnClickListener(v -> onSaveClick());
    }

    private void onSaveClick() {
        String title = titleInput.getText().toString().trim();

        if (title.isEmpty()) {
            toast("Введіть назву задачі");
            return;
        }
        if (selectedImageUri == null) {
            toast("Додайте зображення");
            return;
        }

        uploadTask(title, selectedImageUri);
    }

    private void uploadTask(String title, Uri imageUri) {
        String mimeType = getContentResolver().getType(imageUri);
        if (mimeType == null) mimeType = "image/jpeg";

        RequestBody titlePart =
                RequestBody.create(title, MultipartBody.FORM);

        RequestBody imageBody =
                new UriRequestBody(this, imageUri, mimeType);

        MultipartBody.Part imagePart =
                MultipartBody.Part.createFormData(
                        "Image",
                        FileUtil.getFileName(this, imageUri),
                        imageBody
                );

        RetrofitClient.getInstance()
                .getZadachiApi()
                .create(titlePart, imagePart)
                .enqueue(new Callback<ZadachaItemDTO>() {
                    @Override
                    public void onResponse(Call<ZadachaItemDTO> call, Response<ZadachaItemDTO> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            toast("Задача створена");
                            setResult(RESULT_OK);
                            finish();
                        } else if (response.isSuccessful() && response.body() == null) {
                            Log.d("AddTaskActivity", "Response successful but body is null. Code: " + response.code());
                            toast("Задача створена");
                            setResult(RESULT_OK);
                            finish();
                        } else {
                            String errorBody = "";
                            try {
                                if (response.errorBody() != null) {
                                    errorBody = response.errorBody().string();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            Log.e("AddTaskActivity", "Server error: " + response.code() + ", body: " + errorBody);
                            toast("Помилка сервера: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<ZadachaItemDTO> call, Throwable t) {
                        Log.e("AddTaskActivity", "onFailure type: " + t.getClass().getName());
                        Log.e("AddTaskActivity", "message: " + t.getMessage(), t);
                        toast("Помилка: " + t.getMessage());
                    }
                });
        }

    private void toast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }
}
