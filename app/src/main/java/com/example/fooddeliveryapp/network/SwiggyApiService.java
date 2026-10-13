
package com.example.fooddeliveryapp.network;

import com.example.fooddeliveryapp.model.SwiggyRequest;
import com.example.fooddeliveryapp.model.SwiggyResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface SwiggyApiService {

    @POST("api/v1/swiggy/call")
    Call<SwiggyResponse> callSwiggyTool(

            @Header("x-user-id")
            String userId,

            @Body
            SwiggyRequest request
    );
}

