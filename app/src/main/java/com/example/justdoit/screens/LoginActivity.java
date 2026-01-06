package com.example.justdoit.screens;

import android.os.Bundle;
import android.view.View;

import com.example.justdoit.BaseActivity;
import com.example.justdoit.R;
import com.example.justdoit.application.HomeApplication;
import com.example.justdoit.dto.auth.AuthResponse;
import com.example.justdoit.dto.auth.LoginRequestDTO;
import com.example.justdoit.network.RetrofitClient;
import com.example.justdoit.utils.CommonUtils;
import com.example.justdoit.utils.MyLogger;
import com.example.justdoit.utils.validation.logic.FieldValidator;
import com.example.justdoit.utils.validation.logic.FormValidator;
import com.example.justdoit.utils.validation.rules.EmailRule;
import com.example.justdoit.utils.validation.rules.MinLengthRule;
import com.example.justdoit.utils.validation.rules.RequiredRule;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends BaseActivity {

    private TextInputLayout emailLayout, passwordLayout;
    private TextInputEditText emailInput, passwordInput;

    private FormValidator formValidator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (HomeApplication.getInstance().isAuth()){
            goToMain();
        }

        initViews();
        initValidator();
    }

    private void initViews() {
        emailLayout     = findViewById(R.id.emailLayout);
        passwordLayout  = findViewById(R.id.passwordLayout);

        emailInput      = findViewById(R.id.email);
        passwordInput   = findViewById(R.id.password);
    }

    private void initValidator() {
        formValidator = new FormValidator()
                .addField(
                        new FieldValidator(emailLayout, emailInput)
                                .addRule(new RequiredRule("Введіть email"))
                                .addRule(new EmailRule("Некоректний email"))
                )
                .addField(
                        new FieldValidator(passwordLayout, passwordInput)
                                .addRule(new RequiredRule("Введіть пароль"))
                                .addRule(new MinLengthRule(6, "Мінімум 6 символів"))
                );
    }

    public void onLoginClick(View view) {

        if (!formValidator.validate()) {
            return;
        }

        uploadLogin(
                emailInput.getText().toString().trim(),
                passwordInput.getText().toString().trim()
        );
    }

    private void uploadLogin(String em, String pw) {

        CommonUtils.showLoading();

        LoginRequestDTO dto = new LoginRequestDTO(em, pw);

        RetrofitClient.getInstance()
                .getAuthApi()
                .login(dto)
                .enqueue(new Callback<AuthResponse>() {

                    @Override
                    public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                        CommonUtils.hideLoading();

                        if (response.isSuccessful()) {
                            String token = response.body().getToken();
//                            SessionManager sessionManager = new SessionManager(RegisterActivity.this);
//                            sessionManager.saveToken(token);

                            HomeApplication.getInstance().saveJwtToken(token);

                            MyLogger.toast("Вхід успішний");
                            goToMain();
                            finish();
                        } else {
                            MyLogger.toast("Помилка сервера: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<AuthResponse> call, Throwable t) {
                        CommonUtils.hideLoading();
                        MyLogger.toast("Помилка: " + t.getMessage());
                    }
                });
    }
}
