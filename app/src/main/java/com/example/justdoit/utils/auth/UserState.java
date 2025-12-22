package com.example.justdoit.utils.auth;

import android.util.Log;
import com.auth0.android.jwt.JWT;

public class UserState {
    private static UserState instance;
    private String name;
    private String email;
    private String image;

    private UserState() {}

    public static synchronized UserState getInstance() {
        if (instance == null) instance = new UserState();
        return instance;
    }

    public void setUserFromToken(String token) {
        if (token == null || token.isEmpty()) return;
        try {
            JWT jwt = new JWT(token);

            this.name = jwt.getClaim("name").asString();
            this.email = jwt.getClaim("email").asString();
            this.image = jwt.getClaim("image").asString();

            Log.d("UserState", "Юзер розпарсений: " + name + ", " + image);
        } catch (Exception e) {
            Log.e("UserState", "Помилка декодування JWT: " + e.getMessage());
        }
    }

    public String getName() { return name; }
    public String getImage() { return image; }
    public boolean isLoggedIn() { return name != null; }

    public void clear() {
        name = null;
        email = null;
        image = null;
    }
}