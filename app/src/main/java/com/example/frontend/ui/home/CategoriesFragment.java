package com.example.frontend.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.frontend.R;
import com.example.frontend.model.ApiResponse;
import com.example.frontend.model.Category;
import com.example.frontend.remote.ApiClient;
import com.example.frontend.remote.ApiService;
import com.example.frontend.ui.adapter.CategoryAdapter;
import com.example.frontend.util.JsonParser;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CategoriesFragment extends Fragment {

    private RecyclerView recyclerViewCategories;
    private CategoryAdapter categoryAdapter;
    private List<Category> categories = new ArrayList<>();
    private ApiService apiService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_categories, container, false);

        initViews(view);
        setupRecyclerView();
        loadCategories();

        return view;
    }

    private void initViews(View view) {
        recyclerViewCategories = view.findViewById(R.id.recyclerViewCategories);
        apiService = ApiClient.getApiService();
    }

    private void setupRecyclerView() {
        categoryAdapter = new CategoryAdapter(categories, this::onCategoryClick);
        recyclerViewCategories.setLayoutManager(new GridLayoutManager(getContext(), 2));
        recyclerViewCategories.setAdapter(categoryAdapter);
    }

    private void loadCategories() {
        apiService.getAllCategories().enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse apiResponse = response.body();
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        try {
                            // Parse data từ Object về List<Category>
                            List<Category> categoryList = parseCategoriesFromData(apiResponse.getData());
                            categories.clear();
                            categories.addAll(categoryList);
                            categoryAdapter.notifyDataSetChanged();
                        } catch (Exception e) {
                            showError("Lỗi parse dữ liệu: " + e.getMessage());
                        }
                    } else {
                        showError("Không thể tải danh mục: " + apiResponse.getMessage());
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

    private List<Category> parseCategoriesFromData(Object data) {
        List<Category> categoryList = JsonParser.parseCategories(data);
        return categoryList != null ? categoryList : new ArrayList<>();
    }

    private void onCategoryClick(Category category) {
        // Navigate to products by category
        Toast.makeText(getContext(), "Chọn danh mục: " + category.getCategoryName(), Toast.LENGTH_SHORT).show();

        // Navigate to ProductsFragment with category filter
        ProductsFragment productsFragment = ProductsFragment.newInstance(category.getCategoryId(), category.getCategoryName());

        // Get parent activity and show fragment
        if (getActivity() instanceof HomeActivity) {
            ((HomeActivity) getActivity()).showFragmentFromChild(productsFragment);
        }
    }

    private void showError(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
    }
}
