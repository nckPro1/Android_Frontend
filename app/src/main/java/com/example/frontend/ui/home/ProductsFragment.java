package com.example.frontend.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
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

public class ProductsFragment extends Fragment {

    private RecyclerView recyclerViewProducts;
    private ProductAdapter productAdapter;
    private List<Product> products = new ArrayList<>();
    private ApiService apiService;

    // Parameters
    private Long categoryId;
    private String categoryName;

    public static ProductsFragment newInstance(Long categoryId, String categoryName) {
        ProductsFragment fragment = new ProductsFragment();
        Bundle args = new Bundle();
        args.putLong("categoryId", categoryId != null ? categoryId : -1);
        args.putString("categoryName", categoryName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            categoryId = getArguments().getLong("categoryId", -1);
            categoryName = getArguments().getString("categoryName", "Tất cả sản phẩm");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_products, container, false);

        initViews(view);
        setupRecyclerView();
        loadProducts();

        return view;
    }

    private void initViews(View view) {
        recyclerViewProducts = view.findViewById(R.id.recyclerViewProducts);
        apiService = ApiClient.getApiService();
    }

    private void setupRecyclerView() {
        productAdapter = new ProductAdapter(products, null); // Không cần listener nữa
        recyclerViewProducts.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewProducts.setAdapter(productAdapter);
    }

    private void loadProducts() {
        Call<ApiResponse> call;

        if (categoryId != null && categoryId > 0) {
            // Load products by category
            call = apiService.getProductsByCategory(categoryId);
        } else {
            // Load all products (this would need pagination in real app)
            call = apiService.getFeaturedProducts(); // Using featured as example
        }

        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse apiResponse = response.body();
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        try {
                            // Parse data từ Object về List<Product>
                            List<Product> productList = parseProductsFromData(apiResponse.getData());
                            products.clear();
                            products.addAll(productList);
                            productAdapter.notifyDataSetChanged();
                        } catch (Exception e) {
                            showError("Lỗi parse dữ liệu: " + e.getMessage());
                        }
                    } else {
                        showError("Không thể tải sản phẩm: " + apiResponse.getMessage());
                    }
                } else {
                    showError("Lỗi kết nối: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                showError("Lỗi mạng: " + t.getMessage());
            }
        });
    }

    private List<Product> parseProductsFromData(Object data) {
        List<Product> productList = JsonParser.parseProducts(data);
        return productList != null ? productList : new ArrayList<>();
    }

    // Method này không còn cần thiết vì ProductAdapter tự xử lý navigation

    private void showError(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
    }
}
