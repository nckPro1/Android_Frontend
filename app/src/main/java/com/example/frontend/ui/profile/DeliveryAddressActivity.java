package com.example.frontend.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.frontend.R;
import com.example.frontend.local.TokenManager;
import com.example.frontend.model.ApiResponse;
import com.example.frontend.model.UserDto;
import com.example.frontend.model.User;
import com.example.frontend.remote.ApiClient;
import com.example.frontend.remote.ApiService;
import com.example.frontend.util.UserManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DeliveryAddressActivity extends AppCompatActivity {

    private static final String TAG = "DeliveryAddressActivity";

    private TokenManager tokenManager;
    private ApiService apiService;
    private UserManager userManager;

    // Views
    private Spinner spinnerCity;
    private Spinner spinnerDistrict;
    private Spinner spinnerWard;
    private TextInputEditText etStreet;
    private TextInputEditText etAddress; // For backward compatibility
    private MaterialButton btnSaveAddress;
    private ProgressBar progressBar;
    private MaterialCardView cardAddressInfo;

    private UserDto currentUser;
    private List<String> cities = new ArrayList<>();
    private List<String> districts = new ArrayList<>();
    private List<String> wards = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_address);

        try {
            tokenManager = new TokenManager(this);
            apiService = ApiClient.getClient().create(ApiService.class);
            userManager = UserManager.getInstance(this);

            setupViews();
            loadCities();
            loadUserProfile();
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage(), e);
            Toast.makeText(this, "Lỗi khởi tạo: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void setupViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        spinnerCity = findViewById(R.id.spinnerCity);
        spinnerDistrict = findViewById(R.id.spinnerDistrict);
        spinnerWard = findViewById(R.id.spinnerWard);
        etStreet = findViewById(R.id.etStreet);
        etAddress = findViewById(R.id.etAddress);
        btnSaveAddress = findViewById(R.id.btnSaveAddress);
        progressBar = findViewById(R.id.progressBar);
        cardAddressInfo = findViewById(R.id.cardAddressInfo);

        btnSaveAddress.setOnClickListener(v -> saveDeliveryAddress());

        // Set up spinners
        setupSpinners();
    }

    private void setupSpinners() {
        // City spinner
        spinnerCity.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) { // Skip "Chọn thành phố"
                    String selectedCity = cities.get(position - 1);
                    loadDistricts(selectedCity);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // District spinner
        spinnerDistrict.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0 && spinnerCity.getSelectedItemPosition() > 0) { // Skip "Chọn quận/huyện"
                    String selectedCity = cities.get(spinnerCity.getSelectedItemPosition() - 1);
                    String selectedDistrict = districts.get(position - 1);
                    loadWards(selectedCity, selectedDistrict);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void loadCities() {
        showLoading(true);
        Call<ApiResponse<List<String>>> call = apiService.getAvailableCities();
        call.enqueue(new Callback<ApiResponse<List<String>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<String>>> call, Response<ApiResponse<List<String>>> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    cities = response.body().getData();
                    setupCitySpinner();
                } else {
                    Toast.makeText(DeliveryAddressActivity.this, "Không thể tải danh sách thành phố", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<String>>> call, Throwable t) {
                showLoading(false);
                Log.e(TAG, "Error loading cities: " + t.getMessage(), t);
                Toast.makeText(DeliveryAddressActivity.this, "Lỗi tải danh sách thành phố", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadDistricts(String city) {
        showLoading(true);
        Call<ApiResponse<List<String>>> call = apiService.getDistrictsByCity(city);
        call.enqueue(new Callback<ApiResponse<List<String>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<String>>> call, Response<ApiResponse<List<String>>> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    districts = response.body().getData();
                    setupDistrictSpinner();
                } else {
                    Toast.makeText(DeliveryAddressActivity.this, "Không thể tải danh sách quận/huyện", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<String>>> call, Throwable t) {
                showLoading(false);
                Log.e(TAG, "Error loading districts: " + t.getMessage(), t);
                Toast.makeText(DeliveryAddressActivity.this, "Lỗi tải danh sách quận/huyện", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadWards(String city, String district) {
        showLoading(true);
        Call<ApiResponse<List<String>>> call = apiService.getWardsByDistrict(city, district);
        call.enqueue(new Callback<ApiResponse<List<String>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<String>>> call, Response<ApiResponse<List<String>>> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    wards = response.body().getData();
                    setupWardSpinner();
                } else {
                    Toast.makeText(DeliveryAddressActivity.this, "Không thể tải danh sách phường/xã", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<String>>> call, Throwable t) {
                showLoading(false);
                Log.e(TAG, "Error loading wards: " + t.getMessage(), t);
                Toast.makeText(DeliveryAddressActivity.this, "Lỗi tải danh sách phường/xã", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupCitySpinner() {
        List<String> cityOptions = new ArrayList<>();
        cityOptions.add("Chọn thành phố");
        cityOptions.addAll(cities);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, cityOptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCity.setAdapter(adapter);
    }

    private void setupDistrictSpinner() {
        List<String> districtOptions = new ArrayList<>();
        districtOptions.add("Chọn quận/huyện");
        districtOptions.addAll(districts);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, districtOptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDistrict.setAdapter(adapter);

        // Reset ward spinner
        spinnerWard.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new String[]{"Chọn phường/xã"}));
    }

    private void setupWardSpinner() {
        List<String> wardOptions = new ArrayList<>();
        wardOptions.add("Chọn phường/xã");
        wardOptions.addAll(wards);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, wardOptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerWard.setAdapter(adapter);
    }

    private void loadUserProfile() {
        try {
            showLoading(true);
            Call<UserDto> call = apiService.getUserProfile("Bearer " + tokenManager.getAccessToken());
            call.enqueue(new Callback<UserDto>() {
                @Override
                public void onResponse(Call<UserDto> call, Response<UserDto> response) {
                    showLoading(false);
                    if (response.isSuccessful() && response.body() != null) {
                        currentUser = response.body();
                        displayCurrentAddress();
                    } else {
                        Toast.makeText(DeliveryAddressActivity.this, "Không thể tải thông tin", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<UserDto> call, Throwable t) {
                    showLoading(false);
                    Log.e(TAG, "Error loading user profile: " + t.getMessage(), t);
                    Toast.makeText(DeliveryAddressActivity.this, "Lỗi tải thông tin người dùng", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            showLoading(false);
            Log.e(TAG, "Error in loadUserProfile: " + e.getMessage(), e);
            Toast.makeText(this, "Lỗi tải thông tin: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void displayCurrentAddress() {
        if (currentUser != null) {
            // Display address components if available
            if (currentUser.getUserCity() != null && !currentUser.getUserCity().isEmpty()) {
                // Set city spinner
                int cityIndex = cities.indexOf(currentUser.getUserCity());
                if (cityIndex >= 0) {
                    spinnerCity.setSelection(cityIndex + 1); // +1 for "Chọn thành phố"
                }

                // Load districts and set district
                if (currentUser.getUserDistrict() != null && !currentUser.getUserDistrict().isEmpty()) {
                    loadDistricts(currentUser.getUserCity());
                    // Note: District will be set after districts are loaded
                }
            }

            // Display street address
            if (currentUser.getUserStreet() != null && !currentUser.getUserStreet().isEmpty()) {
                etStreet.setText(currentUser.getUserStreet());
            }

            // Display full address for backward compatibility
            if (currentUser.getAddress() != null && !currentUser.getAddress().isEmpty()) {
                etAddress.setText(currentUser.getAddress());
            }
        }
    }

    private void saveDeliveryAddress() {
        try {
            // Validate input
            if (spinnerCity.getSelectedItemPosition() == 0) {
                Toast.makeText(this, "Vui lòng chọn thành phố", Toast.LENGTH_SHORT).show();
                return;
            }

            if (spinnerDistrict.getSelectedItemPosition() == 0) {
                Toast.makeText(this, "Vui lòng chọn quận/huyện", Toast.LENGTH_SHORT).show();
                return;
            }

            if (spinnerWard.getSelectedItemPosition() == 0) {
                Toast.makeText(this, "Vui lòng chọn phường/xã", Toast.LENGTH_SHORT).show();
                return;
            }

            String street = etStreet.getText().toString().trim();
            if (street.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập tên đường, số nhà", Toast.LENGTH_SHORT).show();
                return;
            }

            // Get selected values
            String selectedCity = cities.get(spinnerCity.getSelectedItemPosition() - 1);
            String selectedDistrict = districts.get(spinnerDistrict.getSelectedItemPosition() - 1);
            String selectedWard = wards.get(spinnerWard.getSelectedItemPosition() - 1);

            // Create full address
            String fullAddress = street + ", " + selectedWard + ", " + selectedDistrict + ", " + selectedCity;

            // Update user profile
            UserDto updatedUser = new UserDto();
            updatedUser.setUserCity(selectedCity);
            updatedUser.setUserDistrict(selectedDistrict);
            updatedUser.setUserWard(selectedWard);
            updatedUser.setUserStreet(street);
            updatedUser.setAddress(fullAddress); // For backward compatibility

            showLoading(true);
            Call<UserDto> call = apiService.updateUserProfile("Bearer " + tokenManager.getAccessToken(), updatedUser);
            call.enqueue(new Callback<UserDto>() {
                @Override
                public void onResponse(Call<UserDto> call, Response<UserDto> response) {
                    showLoading(false);
                    if (response.isSuccessful() && response.body() != null) {
                        Toast.makeText(DeliveryAddressActivity.this, "Cập nhật địa chỉ thành công", Toast.LENGTH_SHORT).show();

                        // Update local user data
                        userManager.refreshUserData(response.body());

                        // Return to previous activity
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("address_updated", true);
                        setResult(RESULT_OK, resultIntent);
                        finish();
                    } else {
                        Toast.makeText(DeliveryAddressActivity.this, "Cập nhật địa chỉ thất bại", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<UserDto> call, Throwable t) {
                    showLoading(false);
                    Log.e(TAG, "Error updating address: " + t.getMessage(), t);
                    Toast.makeText(DeliveryAddressActivity.this, "Lỗi cập nhật địa chỉ", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            showLoading(false);
            Log.e(TAG, "Error in saveDeliveryAddress: " + e.getMessage(), e);
            Toast.makeText(this, "Lỗi cập nhật địa chỉ: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnSaveAddress.setEnabled(!show);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}