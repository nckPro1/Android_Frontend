package com.example.frontend.ui.search;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.frontend.R;
import com.example.frontend.model.ApiResponse;
import com.example.frontend.model.Product;
import com.example.frontend.remote.ApiClient;
import com.example.frontend.remote.ApiService;
import com.example.frontend.ui.adapter.ProductAdapter;
import com.example.frontend.util.JsonParser;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchResultsActivity extends AppCompatActivity {

    private TextView tvTitle;
    private ImageView ivBack;
    private RecyclerView rvResults;
    private ProgressBar progressBar;
    private ProductAdapter productAdapter;
    private List<Product> products = new ArrayList<>();
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_results);

        tvTitle = findViewById(R.id.tvTitle);
        ivBack = findViewById(R.id.ivBack);
        rvResults = findViewById(R.id.rvResults);
        progressBar = findViewById(R.id.progressBar);

        apiService = ApiClient.getApiService();

        productAdapter = new ProductAdapter(products, null);
        rvResults.setLayoutManager(new LinearLayoutManager(this));
        rvResults.setAdapter(productAdapter);

        ivBack.setOnClickListener(v -> finish());

        String query = getIntent().getStringExtra("query");
        if (query == null) query = "";
        tvTitle.setText("Kết quả: " + query);

        performSearch(query);
    }

    private void performSearch(String query) {
        progressBar.setVisibility(View.VISIBLE);
        apiService.searchProducts(query).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    try {
                        List<Product> result = JsonParser.parseProducts(response.body().getData());
                        products.clear();
                        products.addAll(result);
                        productAdapter.notifyDataSetChanged();
                    } catch (Exception e) {
                        showMessage("Lỗi parse dữ liệu: " + e.getMessage());
                    }
                } else {
                    products.clear();
                    productAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                showMessage("Lỗi tìm kiếm: " + t.getMessage());
            }
        });
    }

    private void showMessage(String msg) {
        android.widget.Toast.makeText(this, msg, android.widget.Toast.LENGTH_LONG).show();
    }
}


