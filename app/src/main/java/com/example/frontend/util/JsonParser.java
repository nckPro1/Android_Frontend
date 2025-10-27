package com.example.frontend.util;

import com.example.frontend.model.Category;
import com.example.frontend.model.Product;
import com.example.frontend.model.ProductOption;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

public class JsonParser {
    private static final Gson gson = new Gson();

    public static List<Category> parseCategories(Object data) {
        try {
            if (data instanceof List) {
                Type listType = new TypeToken<List<Category>>(){}.getType();
                return gson.fromJson(gson.toJson(data), listType);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<Product> parseProducts(Object data) {
        try {
            if (data instanceof List) {
                Type listType = new TypeToken<List<Product>>(){}.getType();
                return gson.fromJson(gson.toJson(data), listType);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Product parseProduct(Object data) {
        try {
            return gson.fromJson(gson.toJson(data), Product.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Category parseCategory(Object data) {
        try {
            return gson.fromJson(gson.toJson(data), Category.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<ProductOption> parseProductOptions(Object data) {
        if (data == null) {
            android.util.Log.d("JsonParser", "parseProductOptions: data is null");
            return null;
        }
        try {
            android.util.Log.d("JsonParser", "parseProductOptions: data type: " + data.getClass().getSimpleName());
            String json = gson.toJson(data);
            android.util.Log.d("JsonParser", "parseProductOptions: raw json: " + json);
            
            Type optionListType = new TypeToken<List<ProductOption>>() {}.getType();
            List<ProductOption> result = gson.fromJson(json, optionListType);
            android.util.Log.d("JsonParser", "parseProductOptions: parsed count: " + (result != null ? result.size() : 0));
            
            // Log each parsed option
            if (result != null) {
                for (int i = 0; i < result.size(); i++) {
                    ProductOption option = result.get(i);
                    android.util.Log.d("JsonParser", "Parsed option " + i + ": " + option.toString());
                }
            }
            
            return result;
        } catch (Exception e) {
            android.util.Log.e("JsonParser", "parseProductOptions error: " + e.getMessage(), e);
            e.printStackTrace();
            return null;
        }
    }

    public static ProductOption parseProductOption(Object data) {
        try {
            return gson.fromJson(gson.toJson(data), ProductOption.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
