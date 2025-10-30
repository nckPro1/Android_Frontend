package com.example.frontend.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.frontend.R;
import com.example.frontend.model.PaymentMethod;
import com.squareup.picasso.Picasso;

import java.util.List;

public class PaymentMethodAdapter extends RecyclerView.Adapter<PaymentMethodAdapter.PaymentMethodViewHolder> {

    private List<PaymentMethod> paymentMethods;
    private OnPaymentMethodSelectedListener listener;
    private int selectedPosition = -1;

    public interface OnPaymentMethodSelectedListener {
        void onPaymentMethodSelected(PaymentMethod paymentMethod);
    }

    public PaymentMethodAdapter(List<PaymentMethod> paymentMethods, OnPaymentMethodSelectedListener listener) {
        this.paymentMethods = paymentMethods;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PaymentMethodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_payment_method, parent, false);
        return new PaymentMethodViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PaymentMethodViewHolder holder, int position) {
        PaymentMethod paymentMethod = paymentMethods.get(position);
        holder.bind(paymentMethod, position);
    }

    @Override
    public int getItemCount() {
        return paymentMethods.size();
    }

    class PaymentMethodViewHolder extends RecyclerView.ViewHolder {
        private RadioButton radioButton;
        private ImageView imageViewIcon;
        private TextView textViewName;
        private TextView textViewDescription;

        public PaymentMethodViewHolder(@NonNull View itemView) {
            super(itemView);
            radioButton = itemView.findViewById(R.id.radioButton);
            imageViewIcon = itemView.findViewById(R.id.imageViewIcon);
            textViewName = itemView.findViewById(R.id.textViewName);
            textViewDescription = itemView.findViewById(R.id.textViewDescription);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    selectedPosition = position;
                    notifyDataSetChanged();
                    if (listener != null) {
                        listener.onPaymentMethodSelected(paymentMethods.get(position));
                    }
                }
            });
        }

        public void bind(PaymentMethod paymentMethod, int position) {
            textViewName.setText(paymentMethod.getMethodName());
            textViewDescription.setText(paymentMethod.getDescription());
            radioButton.setChecked(selectedPosition == position);

            // Load icon
            if (paymentMethod.getIconUrl() != null && !paymentMethod.getIconUrl().isEmpty()) {
                Picasso.get()
                        .load(paymentMethod.getIconUrl())
                        .placeholder(R.drawable.ic_payment_default)
                        .error(R.drawable.ic_payment_default)
                        .into(imageViewIcon);
            } else {
                imageViewIcon.setImageResource(R.drawable.ic_payment_default);
            }

            // Disable if not active
            itemView.setEnabled(paymentMethod.isActive());
            radioButton.setEnabled(paymentMethod.isActive());
        }
    }
}











