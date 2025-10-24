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
        this.options = options != null ? options : new ArrayList<>();
        this.selectedOptions = new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public OptionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product_option, parent, false);
        return new OptionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OptionViewHolder holder, int position) {
        ProductOption option = options.get(position);
        holder.bind(option);
    }

    @Override
    public int getItemCount() {
        return options.size();
    }

    public List<ProductOption> getSelectedOptions() {
        return selectedOptions;
    }

    public void clearSelection() {
        selectedOptions.clear();
        notifyDataSetChanged();
    }

    class OptionViewHolder extends RecyclerView.ViewHolder {
        private MaterialCardView optionCard;
        private ImageView ivOptionIcon;
        private TextView tvOptionName;
        private TextView tvOptionType;
        private TextView tvIsRequired;
        private TextView tvExtraPrice;
        private ImageView ivOptionCheck;

        public OptionViewHolder(@NonNull View itemView) {
            super(itemView);
            optionCard = itemView.findViewById(R.id.optionCard);
            ivOptionIcon = itemView.findViewById(R.id.ivOptionIcon);
            tvOptionName = itemView.findViewById(R.id.tvOptionName);
            tvOptionType = itemView.findViewById(R.id.tvOptionType);
            tvIsRequired = itemView.findViewById(R.id.tvIsRequired);
            tvExtraPrice = itemView.findViewById(R.id.tvExtraPrice);
            ivOptionCheck = itemView.findViewById(R.id.ivOptionCheck);
        }

        public void bind(ProductOption option) {
            android.util.Log.d("ProductOptionsAdapter", "Binding option: " + option.getOptionName() + ", extraPrice: " + option.getExtraPrice());

            tvOptionName.setText(option.getOptionName());
            tvOptionType.setText(option.getOptionType() != null ? option.getOptionType().toUpperCase() : "");

            // Debug price formatting
            String formattedPrice = option.getFormattedPrice();
            android.util.Log.d("ProductOptionsAdapter", "Formatted price: '" + formattedPrice + "'");
            tvExtraPrice.setText(formattedPrice);

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
                boolean wasSelected = selectedOptions.contains(option);

                if (wasSelected) {
                    selectedOptions.remove(option);
                } else {
                    // Check if we can select this option
                    if (canSelectOption(option)) {
                        selectedOptions.add(option);
                    }
                }

                notifyItemChanged(getAdapterPosition());

                if (listener != null) {
                    listener.onOptionClick(option, !wasSelected);
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

        private boolean canSelectOption(ProductOption option) {
            if (option.isRequired()) {
                // For required options, only allow one selection per type
                String optionType = option.getOptionType();
                for (ProductOption selected : selectedOptions) {
                    if (selected.getOptionType().equals(optionType)) {
                        return false; // Already have one of this type
                    }
                }
                return true;
            } else {
                // For optional options, check maxSelections
                int currentCount = 0;
                for (ProductOption selected : selectedOptions) {
                    if (selected.getOptionType().equals(option.getOptionType())) {
                        currentCount++;
                    }
                }
                Integer maxSelections = option.getMaxSelections();
                return maxSelections == null || currentCount < maxSelections;
            }
        }
    }
}