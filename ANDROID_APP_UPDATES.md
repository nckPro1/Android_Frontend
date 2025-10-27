# Android App Updates - Address & Shipping System

## Overview
Android app đã được cập nhật để sử dụng hệ thống địa chỉ mới với thành phố/quận/phường thay vì GPS coordinates.

## Model Updates

### 1. User Model
```java
public class User {
    // Removed GPS coordinates
    // private BigDecimal latitude;
    // private BigDecimal longitude;
    
    // Added address components
    private String userCity;
    private String userDistrict;
    private String userWard;
    private String userStreet;
    
    // Helper methods
    public boolean hasAddressComponents() {
        return userCity != null && !userCity.trim().isEmpty() &&
               userDistrict != null && !userDistrict.trim().isEmpty() &&
               userWard != null && !userWard.trim().isEmpty() &&
               userStreet != null && !userStreet.trim().isEmpty();
    }
    
    public String getFullAddress() {
        if (hasAddressComponents()) {
            return userStreet + ", " + userWard + ", " + userDistrict + ", " + userCity;
        }
        return address != null ? address : "";
    }
}
```

### 2. Order Model
```java
public class Order {
    // Added address components
    private String deliveryCity;
    private String deliveryDistrict;
    private String deliveryWard;
    private String deliveryStreet;
    
    // Updated CreateOrderRequest
    public static class CreateOrderRequest {
        private String deliveryCity;
        private String deliveryDistrict;
        private String deliveryWard;
        private String deliveryStreet;
        // ... other fields
    }
}
```

### 3. ShippingCalculationResponse
```java
public class ShippingCalculationResponse {
    private BigDecimal shippingFee;
    private BigDecimal distanceKm;
    private Integer estimatedDurationMinutes;
    private boolean fromCache;
    private String description;
    
    // Helper methods for backward compatibility
    public BigDecimal getDistance() { return distanceKm; }
    public String getShippingFeeDescription() { return description; }
    public boolean isDeliverable() { 
        return shippingFee != null && shippingFee.compareTo(BigDecimal.ZERO) > 0; 
    }
}
```

## New Models

### 1. AddressData
```java
public class AddressData {
    private List<String> cities;
    private List<String> districts;
    private List<String> wards;
}
```

### 2. ShippingCalculationRequest
```java
public class ShippingCalculationRequest {
    private BigDecimal orderAmount;
    private String deliveryCity;
    private String deliveryDistrict;
    private String deliveryWard;
    private String deliveryStreet;
}
```

### 3. StoreInfo
```java
public class StoreInfo {
    private String storeName;
    private String storePhone;
    private String storeEmail;
    private String storeCity;
    private String storeDistrict;
    private String storeWard;
    private String storeStreet;
    private String storeDescription;
    
    public String getFullAddress() {
        return storeStreet + ", " + storeWard + ", " + storeDistrict + ", " + storeCity;
    }
}
```

## API Service Updates

### New Endpoints
```java
public interface ApiService {
    // Address data endpoints
    @GET("/api/app/cities")
    Call<ApiResponse<List<String>>> getAvailableCities();
    
    @GET("/api/app/districts")
    Call<ApiResponse<List<String>>> getDistrictsByCity(@Query("city") String city);
    
    @GET("/api/app/wards")
    Call<ApiResponse<List<String>>> getWardsByDistrict(@Query("city") String city, @Query("district") String district);
    
    // Shipping calculation with address components
    @POST("/api/app/calculate-shipping")
    Call<ApiResponse<ShippingCalculationResponse>> calculateShippingWithAddress(@Body ShippingCalculationRequest request);
    
    // Store info
    @GET("/api/app/store-info")
    Call<ApiResponse<StoreInfo>> getStoreInfo();
    
    // Delivery availability check
    @POST("/api/app/check-delivery-availability")
    Call<ApiResponse<DeliveryAvailabilityResponse>> checkDeliveryAvailability(@Body DeliveryLocationRequest request);
}
```

## Activity Updates

### 1. DeliveryAddressActivity
- **New UI**: Spinner-based address selection (City → District → Ward)
- **Street Input**: Text input for specific street and house number
- **API Integration**: Loads cities, districts, wards from backend
- **User Experience**: Cascading dropdowns for better UX

**Key Features:**
```java
private void loadCities() {
    Call<ApiResponse<List<String>>> call = apiService.getAvailableCities();
    // Handle response and populate city spinner
}

private void loadDistricts(String city) {
    Call<ApiResponse<List<String>>> call = apiService.getDistrictsByCity(city);
    // Handle response and populate district spinner
}

private void loadWards(String city, String district) {
    Call<ApiResponse<List<String>>> call = apiService.getWardsByDistrict(city, district);
    // Handle response and populate ward spinner
}
```

### 2. CheckoutActivity
- **Shipping Calculation**: Uses address components instead of GPS
- **User Address**: Automatically loads user's saved address
- **Real-time Updates**: Recalculates shipping when address changes

**Key Features:**
```java
private void calculateShippingFee() {
    ShippingCalculationRequest request = new ShippingCalculationRequest();
    request.setOrderAmount(subtotal);
    request.setDeliveryCity(currentUser.getUserCity());
    request.setDeliveryDistrict(currentUser.getUserDistrict());
    request.setDeliveryWard(currentUser.getUserWard());
    request.setDeliveryStreet(currentUser.getUserStreet());
    
    Call<ApiResponse<ShippingCalculationResponse>> call = apiService.calculateShippingWithAddress(request);
    // Handle response and update UI
}
```

## Layout Updates

### activity_delivery_address.xml
- **Spinners**: City, District, Ward selection
- **Text Input**: Street and house number
- **Auto-generated**: Full address display
- **Instructions**: Updated guidance for users

**Key Components:**
```xml
<Spinner android:id="@+id/spinnerCity" />
<Spinner android:id="@+id/spinnerDistrict" />
<Spinner android:id="@+id/spinnerWard" />
<TextInputEditText android:id="@+id/etStreet" />
<TextInputEditText android:id="@+id/etAddress" android:enabled="false" />
```

## Implementation Guide

### 1. Address Selection Flow
1. **Load Cities**: Call `/api/app/cities` on activity start
2. **User Selects City**: Load districts via `/api/app/districts?city={city}`
3. **User Selects District**: Load wards via `/api/app/wards?city={city}&district={district}`
4. **User Enters Street**: Manual input for street and house number
5. **Save Address**: Update user profile with all components

### 2. Shipping Calculation Flow
1. **Get User Address**: Load user profile with address components
2. **Create Request**: Build `ShippingCalculationRequest` with order amount and address
3. **Call API**: Use `/api/app/calculate-shipping` endpoint
4. **Update UI**: Display shipping fee and delivery time

### 3. Order Creation Flow
1. **Validate Address**: Ensure all address components are filled
2. **Create Order Request**: Include address components in `CreateOrderRequest`
3. **Submit Order**: Call `/api/orders` with complete address data
4. **Handle Response**: Navigate to success page or show error

## Backward Compatibility

### Existing Features
- **Address Field**: Still supported for users who haven't updated
- **GPS Coordinates**: Removed but won't break existing functionality
- **Old API Calls**: Fallback to default shipping fee if new API fails

### Migration Strategy
1. **Gradual Rollout**: New address system works alongside old system
2. **User Prompt**: Encourage users to update their address
3. **Fallback Logic**: Handle cases where address components are missing

## Error Handling

### API Failures
```java
private void calculateShippingFee() {
    Call<ApiResponse<ShippingCalculationResponse>> call = apiService.calculateShippingWithAddress(request);
    call.enqueue(new Callback<ApiResponse<ShippingCalculationResponse>>() {
        @Override
        public void onFailure(Call<ApiResponse<ShippingCalculationResponse>> call, Throwable t) {
            // Fallback to default shipping fee
            shippingFee = BigDecimal.valueOf(15000);
            updateUI();
        }
    });
}
```

### Validation
```java
private void saveDeliveryAddress() {
    if (spinnerCity.getSelectedItemPosition() == 0) {
        Toast.makeText(this, "Vui lòng chọn thành phố", Toast.LENGTH_SHORT).show();
        return;
    }
    // ... other validations
}
```

## Testing Checklist

### Address Selection
- [ ] Cities load correctly on app start
- [ ] Districts load when city is selected
- [ ] Wards load when district is selected
- [ ] Street input accepts text
- [ ] Full address is generated correctly

### Shipping Calculation
- [ ] Shipping fee calculates with address components
- [ ] Fallback works when API fails
- [ ] UI updates with new shipping fee
- [ ] Different fees for different areas

### Order Creation
- [ ] Order includes address components
- [ ] Validation prevents incomplete addresses
- [ ] Success/error handling works
- [ ] Cart is cleared after successful order

### User Experience
- [ ] Smooth transitions between spinners
- [ ] Loading indicators during API calls
- [ ] Clear error messages
- [ ] Intuitive address input flow

## Performance Considerations

### API Calls
- **Caching**: Consider caching city/district/ward data
- **Batch Loading**: Load all data at once if possible
- **Offline Support**: Store address data locally

### UI Performance
- **Spinner Updates**: Use efficient adapters
- **Text Watchers**: Debounce address changes
- **Memory Management**: Clear unused data

## Future Enhancements

### Planned Features
- **Address Search**: Search for specific addresses
- **Recent Addresses**: Save frequently used addresses
- **Address Validation**: Verify address exists
- **Delivery Zones**: Show delivery coverage areas

### Technical Improvements
- **Offline Mode**: Work without internet
- **Address Autocomplete**: Suggest addresses as user types
- **Map Integration**: Optional map view for address selection
- **Multi-language**: Support for different languages






