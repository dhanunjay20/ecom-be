package com.tcon.ecom.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VendorDashboardStats {
    private BigDecimal totalRevenue;
    private Double revenueChange;
    private Integer totalOrders;
    private Double ordersChange;
    private Integer totalProducts;
    private Double productsChange;
    private Integer totalCustomers;
    private Double customersChange;
    private Integer lowStockProducts;
    private Integer pendingOrders;
    private List<OrderResponse> recentOrders;
    private List<TopProduct> topProducts;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopProduct {
        private String id;
        private String name;
        private BigDecimal revenue;
        private Integer unitsSold;
    }
}

