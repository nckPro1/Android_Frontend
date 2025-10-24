package com.example.frontend.ui.order;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.frontend.R;
import com.example.frontend.model.Order;
import com.example.frontend.util.PriceFormatter;

import java.util.List;

public class OrderHistoryAdapter extends RecyclerView.Adapter<OrderHistoryAdapter.OrderHistoryViewHolder> {

    private List<Order> orders;
    private OnOrderClickListener onOrderClickListener;

    public interface OnOrderClickListener {
        void onOrderClick(Order order);
    }

    public OrderHistoryAdapter(List<Order> orders, OnOrderClickListener listener) {
        this.orders = orders;
        this.onOrderClickListener = listener;
        Log.d("OrderHistoryAdapter", "Adapter created with " + orders.size() + " orders");
    }

    @NonNull
    @Override
    public OrderHistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order_history, parent, false);
        return new OrderHistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderHistoryViewHolder holder, int position) {
        Order order = orders.get(position);
        Log.d("OrderHistoryAdapter", "Binding order at position " + position + ": " + order.getOrderNumber());
        holder.bind(order);
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    class OrderHistoryViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewOrderNumber;
        private TextView textViewOrderDate;
        private TextView textViewOrderStatus;
        private TextView textViewPaymentStatus;
        private TextView textViewTotalAmount;
        private TextView textViewItemCount;

        public OrderHistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewOrderNumber = itemView.findViewById(R.id.textViewOrderNumber);
            textViewOrderDate = itemView.findViewById(R.id.textViewOrderDate);
            textViewOrderStatus = itemView.findViewById(R.id.textViewOrderStatus);
            textViewPaymentStatus = itemView.findViewById(R.id.textViewPaymentStatus);
            textViewTotalAmount = itemView.findViewById(R.id.textViewTotalAmount);
            textViewItemCount = itemView.findViewById(R.id.textViewItemCount);
        }

        public void bind(Order order) {
            textViewOrderNumber.setText(order.getOrderNumber());

            // Order date
            if (order.getCreatedAt() != null) {
                textViewOrderDate.setText("Ngày: " + order.getCreatedAt().toString().substring(0, 10));
            } else {
                textViewOrderDate.setText("Ngày: --");
            }

            // Order status
            textViewOrderStatus.setText(getOrderStatusDisplay(order.getOrderStatus()));
            textViewOrderStatus.setTextColor(getOrderStatusColor(order.getOrderStatus()));

            // Payment status
            textViewPaymentStatus.setText(getPaymentStatusDisplay(order.getPaymentStatus()));
            textViewPaymentStatus.setTextColor(getPaymentStatusColor(order.getPaymentStatus()));

            // Total amount
            textViewTotalAmount.setText(PriceFormatter.format(order.getFinalAmount()));

            // Item count
            int itemCount = order.getOrderItems() != null ? order.getOrderItems().size() : 0;
            textViewItemCount.setText(itemCount + " sản phẩm");

            // Click listener
            itemView.setOnClickListener(v -> {
                if (onOrderClickListener != null) {
                    onOrderClickListener.onOrderClick(order);
                }
            });
        }

        private String getOrderStatusDisplay(Order.OrderStatus status) {
            if (status == null) return "Không xác định";

            switch (status) {
                case PENDING: return "Chờ xử lý";
                case CONFIRMED: return "Đã nhận";
                case DELIVERING: return "Đang giao";
                case DONE: return "Thanh toán thành công";
                default: return status.toString();
            }
        }

        private int getOrderStatusColor(Order.OrderStatus status) {
            if (status == null) return itemView.getContext().getColor(R.color.text_secondary);

            switch (status) {
                case PENDING: return itemView.getContext().getColor(R.color.warning);
                case CONFIRMED: return itemView.getContext().getColor(R.color.primary);
                case DELIVERING: return itemView.getContext().getColor(R.color.primary);
                case DONE: return itemView.getContext().getColor(R.color.success);
                default: return itemView.getContext().getColor(R.color.text_secondary);
            }
        }

        private String getPaymentStatusDisplay(Order.PaymentStatus status) {
            if (status == null) return "Không xác định";

            switch (status) {
                case PENDING: return "Chờ thanh toán";
                case COMPLETED: return "Đã thanh toán";
                case FAILED: return "Thanh toán thất bại";
                case REFUNDED: return "Đã hoàn tiền";
                default: return status.toString();
            }
        }

        private int getPaymentStatusColor(Order.PaymentStatus status) {
            if (status == null) return itemView.getContext().getColor(R.color.text_secondary);

            switch (status) {
                case PENDING: return itemView.getContext().getColor(R.color.warning);
                case COMPLETED: return itemView.getContext().getColor(R.color.success);
                case FAILED: return itemView.getContext().getColor(R.color.error);
                case REFUNDED: return itemView.getContext().getColor(R.color.text_secondary);
                default: return itemView.getContext().getColor(R.color.text_secondary);
            }
        }
    }
}
