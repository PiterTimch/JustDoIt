package com.example.justdoit;

import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.justdoit.network.RetrofitClient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddTaskActivity extends AppCompatActivity {

    private ImageView imagePreview;
    private Uri selectedImageUri;

    private final ActivityResultLauncher<String> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    imagePreview.setImageURI(uri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_task);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.addTaskRoot), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        EditText titleInput = findViewById(R.id.taskTitleInput);
        imagePreview = findViewById(R.id.taskImagePreview);
        Button chooseImageButton = findViewById(R.id.chooseImageButton);
        Button saveButton = findViewById(R.id.saveTaskButton);

        chooseImageButton.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));

        saveButton.setOnClickListener(v -> {
            String title = titleInput.getText().toString().trim();
            if (title.isEmpty()) {
                Toast.makeText(this, "Введіть назву задачі", Toast.LENGTH_SHORT).show();
                return;
            }
            if (selectedImageUri == null) {
                Toast.makeText(this, "Додайте зображення", Toast.LENGTH_SHORT).show();
                return;
            }
            uploadTask(title, selectedImageUri);
        });
    }

    private void uploadTask(String title, Uri imageUri) {
        try {
            String mimeType = getContentResolver().getType(imageUri);
            if (mimeType == null) mimeType = "image/jpeg";

            byte[] imageBytes = readBytesFromUri(imageUri);
            RequestBody imageBody = RequestBody.create(imageBytes, MediaType.parse(mimeType));

            String fileName = resolveFileName(imageUri);
            MultipartBody.Part imagePart = MultipartBody.Part.createFormData(
                    "Image",
                    fileName,
                    imageBody
            );

            RequestBody namePart = RequestBody.create(title, MultipartBody.FORM);

            RetrofitClient.getInstance()
                    .getZadachiApi()
                    .create(namePart, imagePart)
                    .enqueue(new Callback<com.example.justdoit.dto.zadachi.ZadachaItemDTO>() {
                        @Override
                        public void onResponse(Call<com.example.justdoit.dto.zadachi.ZadachaItemDTO> call, Response<com.example.justdoit.dto.zadachi.ZadachaItemDTO> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(AddTaskActivity.this, "Задача створена", Toast.LENGTH_SHORT).show();
                                finish();
                            } else {
                                Toast.makeText(AddTaskActivity.this, "Помилка сервера: " + response.code(), Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<com.example.justdoit.dto.zadachi.ZadachaItemDTO> call, Throwable t) {
                            Toast.makeText(AddTaskActivity.this, "Мережна помилка: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        } catch (IOException e) {
            Toast.makeText(this, "Не вдалося прочитати зображення", Toast.LENGTH_SHORT).show();
        }
    }

    private byte[] readBytesFromUri(Uri uri) throws IOException {
        try (InputStream inputStream = getContentResolver().openInputStream(uri);
             ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
            if (inputStream == null) throw new IOException("empty input stream");
            byte[] data = new byte[4096];
            int nRead;
            while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            return buffer.toByteArray();
        }
    }

    private String resolveFileName(Uri uri) {
        String name = null;
        if ("content".equals(uri.getScheme())) {
            try (android.database.Cursor cursor = getContentResolver()
                    .query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME);
                    if (index >= 0) {
                        name = cursor.getString(index);
                    }
                }
            }
        }
        if (name == null || name.isEmpty()) {
            name = "upload_" + System.currentTimeMillis() + ".jpg";
        }
        return name;
    }
}

