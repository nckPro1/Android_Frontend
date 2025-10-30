package com.example.frontend.ui.product;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.frontend.R;
import com.example.frontend.model.ProductOption;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

public class ProductOptionsAdapter extends RecyclerView.Adapter<ProductOptionsAdapter.OptionViewHolder> {

    private List<ProductOption> options;
    private List<ProductOption> selectedOptions;
    private OnOptionClickListener listener;

    public interface OnOptionClickListener {
        void onOptionClick(ProductOption option, boolean isSelected);
    }

    public ProductOptionsAdapter(List<ProductOption> options, OnOptionClickListener listener) {
        android.util.Log.d("ProductOptionsAdapter", "Constructor called with " + (options != null ? options.size() : 0) + " options");
        this.options = options != null ? options : new ArrayList<>();
        this.selectedOptions = new ArrayList<>();
        this.listener = listener;
        android.util.Log.d("ProductOptionsAdapter", "Constructor completed successfully");
    }

    @NonNull
    @Override
    public OptionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        android.util.Log.d("ProductOptionsAdapter", "onCreateViewHolder called");
        try {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_product_option, parent, false);
            android.util.Log.d("ProductOptionsAdapter", "Layout inflated successfully");
            return new OptionViewHolder(view);
        } catch (Exception e) {
            android.util.Log.e("ProductOptionsAdapter", "Error in onCreateViewHolder: " + e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull OptionViewHolder holder, int position) {
        ProductOption option = options.get(position);
        android.util.Log.d("ProductOptionsAdapter", "Binding option " + position + ": " + option.getOptionName());
        holder.bind(option);
    }

    @Override
    public int getItemCount() {
        android.util.Log.d("ProductOptionsAdapter", "getItemCount: " + options.size());
        return options.size();
    }

    public List<ProductOption> getSelectedOptions() {
        return selectedOptions;
    }

    public void clearSelection() {
        selectedOptions.clear();
        notifyDataSetChanged();
    }

    public void updateOptions(List<ProductOption> newOptions) {
        android.util.Log.d("ProductOptionsAdapter", "updateOptions called with " + (newOptions != null ? newOptions.size() : 0) + " options");
        this.options = newOptions != null ? newOptions : new ArrayList<>();
        this.selectedOptions.clear(); // Clear previous selections
        notifyDataSetChanged();
        android.util.Log.d("ProductOptionsAdapter", "updateOptions completed, notifyDataSetChanged called");
    }

    class OptionViewHolder extends RecyclerView.ViewHolder {
        private MaterialCardView optionCard;
        private ImageView ivOptionIcon;
        private TextView tvOptionName;
        private TextView tvOptionType;
        private TextView tvIsRequired;
        private TextView tvPrice;
        private ImageView ivOptionCheck;

        public OptionViewHolder(@NonNull View itemView) {
            super(itemView);
            optionCard = itemView.findViewById(R.id.optionCard);
            ivOptionIcon = itemView.findViewById(R.id.ivOptionIcon);
            tvOptionName = itemView.findViewById(R.id.tvOptionName);
            tvOptionType = itemView.findViewById(R.id.tvOptionType);
            tvIsRequired = itemView.findViewById(R.id.tvIsRequired);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            ivOptionCheck = itemView.findViewById(R.id.ivOptionCheck);
        }

        public void bind(ProductOption option) {
            android.util.Log.d("ProductOptionsAdapter", "Binding option: " + option.getOptionName() + ", price: " + option.getPrice());

            tvOptionName.setText(option.getOptionName());
            tvOptionType.setText(option.getOptionType() != null ? option.getOptionType().toUpperCase() : "");

            // Debug price formatting
            String formattedPrice = option.getFormattedPrice();
            android.util.Log.d("ProductOptionsAdapter", "Formatted price: '" + formattedPrice + "'");
            tvPrice.setText(formattedPrice);

            // Show required indicator
            if (option.isRequired()) {
                tvIsRequired.setVisibility(View.VISIBLE);
            } else {
                tvIsRequired.setVisibility(View.GONE);
            }

            // Set icon based on option type
            setOptionIcon(option.getOptionType());

            // Check if this option is selected
            boolean isSelected = selectedOptions.contains(option);
            updateSelectionState(isSelected);

            // Set click listener
            itemView.setOnClickListener(v -> {
                String optionType = option.getOptionType() != null ? option.getOptionType().toUpperCase() : "";
                boolean isCurrentlySelected = selectedOptions.contains(option);

                // For "SIZE", it's single choice. For others, it's multiple choice.
                if ("SIZE".equals(optionType)) {
                    // If a size is clicked, it becomes the only selected size.
                    if (!isCurrentlySelected) {
                        // Remove any other selected size from the list.
                        java.util.Iterator<ProductOption> iterator = selectedOptions.iterator();
                        while (iterator.hasNext()) {
                            ProductOption selected = iterator.next();
                            if ("SIZE".equals(selected.getOptionType().toUpperCase())) {
                                iterator.remove();
                            }
                        }
                        selectedOptions.add(option);
                    }
                    // Clicking an already selected size does nothing.
                } else { // "TOPPING", "CUSTOMIZATION", etc.
                    // Toggle selection.
                    if (isCurrentlySelected) {
                        selectedOptions.remove(option);
                    } else {
                        selectedOptions.add(option);
                    }
                }

                // Update the UI
                notifyDataSetChanged();

                // Notify the listener (e.g., to update total price)
                if (listener != null) {
                    // We can just notify about the clicked option. The activity will recalculate the total.
                    listener.onOptionClick(option, selectedOptions.contains(option));
                }
            });
        }

        private void setOptionIcon(String optionType) {
            switch (optionType.toUpperCase()) {
                case "SIZE":
                    ivOptionIcon.setImageResource(R.drawable.logo_size);
                    break;
                case "TOPPING":
                    ivOptionIcon.setImageResource(R.drawable.logo_topping);
                    break;
                case "EXTRA":
                    ivOptionIcon.setImageResource(R.drawable.logo_extra);
                    break;
                default:
                    ivOptionIcon.setImageResource(R.drawable.logo_option_default);
                    break;
            }
        }

        private void updateSelectionState(boolean isSelected) {
            optionCard.setSelected(isSelected);
            ivOptionCheck.setVisibility(isSelected ? View.VISIBLE : View.GONE);
        }
    }
}