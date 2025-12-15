package com.example.justdoit;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.justdoit.config.Config;
import com.example.justdoit.dto.zadachi.ZadachaItemDTO;
import com.example.justdoit.network.RetrofitClient;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditTaskActivity extends BaseActivity {

    private EditText titleInput;
    private ImageView imagePreview;
    private Uri selectedImageUri;
    private long taskId = -1;
    private String currentImageName;

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
        setContentView(R.layout.activity_edit_task);

        titleInput = findViewById(R.id.editTaskTitleInput);
        imagePreview = findViewById(R.id.editTaskImagePreview);

        findViewById(R.id.editChooseImageButton)
                .setOnClickListener(v -> imagePicker.launch("image/*"));

        taskId = getIntent().getLongExtra("task_id", -1);
        currentImageName = getIntent().getStringExtra("task_image");
        String taskName = getIntent().getStringExtra("task_name");

        if (taskName != null) {
            titleInput.setText(taskName);
        }

        String url = currentImageName != null && !currentImageName.isEmpty()
                ? Config.IMAGES_URL + "200_" + currentImageName
                : Config.IMAGES_URL + "default.jpg";

        Glide.with(this)
                .load(url)
                .apply(new RequestOptions().override(300))
                .into(imagePreview);
    }

    public void onUpdateClick(View view) {
        String title = titleInput.getText().toString().trim();

        if (title.isEmpty()) {
            toast("Введіть назву задачі");
            return;
        }
        if (taskId == -1) {
            toast("Немає ідентифікатора задачі");
            return;
        }

        updateTask(taskId, title, selectedImageUri);
    }

    private void updateTask(long id, String title, Uri imageUri) {
        String mimeType = imageUri != null ? getContentResolver().getType(imageUri) : null;
        if (mimeType == null) mimeType = "image/jpeg";

        RequestBody titlePart =
                RequestBody.create(title, MultipartBody.FORM);

        MultipartBody.Part imagePart = null;
        if(imageUri != null) {
            String imagePath = getImagePath(imageUri);
            if (imagePath != null) {
                File file = new File(imagePath);
                RequestBody requestBody = RequestBody.create(MediaType.parse("multipart/form-data"), file);
                imagePart = MultipartBody.Part.createFormData("image", file.getName(), requestBody);
            }
        }

        RetrofitClient.getInstance()
                .getZadachiApi()
                .update(id, titlePart, imagePart)
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        toast("Задача оновлена");
                        goToMain();
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Log.e("EditTaskActivity", "onFailure type: " + t.getClass().getName());
                        Log.e("EditTaskActivity", "message: " + t.getMessage(), t);
                        toast("Помилка: " + t.getMessage());
                    }
                });
    }

    private String getImagePath(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);

        if (cursor != null) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            String imagePath = cursor.getString(column_index);
            cursor.close();
            return imagePath;
        }

        return null;
    }
    private void toast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }
}


