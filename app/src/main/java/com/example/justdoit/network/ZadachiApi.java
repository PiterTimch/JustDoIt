package com.example.justdoit.network;

import com.example.justdoit.dto.zadachi.ZadachaItemDTO;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.HTTP;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface ZadachiApi {
    @GET("/api/Zadachi")
    Call<List<ZadachaItemDTO>> list();

    @Multipart
    @POST("/api/Zadachi")
    Call<ZadachaItemDTO> create(
            @Part("Name") RequestBody name,
            @Part MultipartBody.Part image
    );

    @Multipart
    @PUT("/api/Zadachi")
    Call<Void> update(
            @Part("id") long id,
            @Part("Name") RequestBody name,
            @Part MultipartBody.Part image
    );

    @HTTP(method = "DELETE", path = "/api/Zadachi/range", hasBody = true)
    Call<Void> deleteRange(@Body List<Long> ids);

}
