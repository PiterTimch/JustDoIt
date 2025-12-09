package com.example.justdoit.network;

import com.example.justdoit.dto.zadachi.ZadachaItemDTO;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface ZadachiApi {
    @GET("/zadachi")
    public Call<List<ZadachaItemDTO>> list();
}
