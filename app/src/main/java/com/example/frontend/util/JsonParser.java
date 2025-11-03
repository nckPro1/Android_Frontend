package com.example.frontend.util;

import com.example.frontend.model.Category;
import com.example.frontend.model.Product;
import com.example.frontend.model.ProductOption;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Collections;
import java.util.ArrayList;
import java.util.Map;

public class JsonParser {
    private static final Gson gson = new Gson();

    public static List<Category> parseCategories(Object data) {
        try {
            if (data == null) return Collections.emptyList();
            if (data instanceof List) {
                Type listType = new TypeToken<List<Category>>(){}.getType();
                List<Category> result = gson.fromJson(gson.toJson(data), listType);
                return result != null ? result : Collections.emptyList();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    public static List<Product> parseProducts(Object data) {
        try {
            if (data == null) return Collections.emptyList();
            // If backend returns a Page object, extract content
            if (data instanceof Map) {
                Object content = ((Map<?, ?>) data).get("content");
                if (content instanceof List) {
                    Type listType = new TypeToken<List<Product>>(){}.getType();
                    List<Product> pageList = gson.fromJson(gson.toJson(content), listType);
                    return pageList != null ? pageList : Collections.emptyList();
                }
            }
            if (data instanceof List) {
                Type listType = new TypeToken<List<Product>>(){}.getType();
                List<Product> result = gson.fromJson(gson.toJson(data), listType);
                return result != null ? result : Collections.emptyList();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
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

    // Generic helpers for Page responses
    public static List<Map<String, Object>> extractPageContentAsList(Object data) {
        try {
            android.util.Log.d("JsonParser", "extractPageContentAsList: data type: " + (data != null ? data.getClass().getSimpleName() : "null"));

            if (data instanceof Map) {
                Map<?, ?> dataMap = (Map<?, ?>) data;
                android.util.Log.d("JsonParser", "extractPageContentAsList: data is Map, keys: " + dataMap.keySet());

                Object content = dataMap.get("content");
                android.util.Log.d("JsonParser", "extractPageContentAsList: content type: " + (content != null ? content.getClass().getSimpleName() : "null"));
                android.util.Log.d("JsonParser", "extractPageContentAsList: content: " + content);

                if (content instanceof List) {
                    List<?> contentList = (List<?>) content;
                    android.util.Log.d("JsonParser", "extractPageContentAsList: content is List, size: " + contentList.size());

                    if (!contentList.isEmpty()) {
                        Object firstItem = contentList.get(0);
                        android.util.Log.d("JsonParser", "extractPageContentAsList: first item type: " + (firstItem != null ? firstItem.getClass().getSimpleName() : "null"));
                        android.util.Log.d("JsonParser", "extractPageContentAsList: first item: " + firstItem);
                    }

                    // Convert to JSON string and parse back to ensure proper type conversion
                    String contentJson = gson.toJson(content);
                    android.util.Log.d("JsonParser", "extractPageContentAsList: content JSON string: " + contentJson);

                    Type listType = new TypeToken<List<Map<String, Object>>>(){}.getType();
                    List<Map<String, Object>> list = gson.fromJson(contentJson, listType);
                    android.util.Log.d("JsonParser", "extractPageContentAsList: parsed list size: " + (list != null ? list.size() : 0));

                    return list != null ? list : Collections.emptyList();
                } else {
                    android.util.Log.w("JsonParser", "extractPageContentAsList: content is not a List");
                }
            } else {
                android.util.Log.w("JsonParser", "extractPageContentAsList: data is not a Map");
            }
        } catch (Exception e) {
            android.util.Log.e("JsonParser", "extractPageContentAsList error: " + e.getMessage(), e);
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    public static List<String> extractPageContentAsStringList(Object data) {
        try {
            if (data instanceof Map) {
                Object content = ((Map<?, ?>) data).get("content");
                if (content instanceof List) {
                    Type listType = new TypeToken<List<String>>(){}.getType();
                    List<String> list = gson.fromJson(gson.toJson(content), listType);
                    return list != null ? list : Collections.emptyList();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }
}
