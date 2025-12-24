package com.tcon.ecom.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VendorAnalytics {
    private List<SalesTrend> salesTrend;
    private List<CategoryBreakdown> categoryBreakdown;
    private List<ProductListResponse> topProducts;
    private List<TrafficSource> trafficSources;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SalesTrend {
        private String date;
        private BigDecimal revenue;
        private Integer orders;
        private Integer visitors;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryBreakdown {
        private String category;
        private BigDecimal revenue;
        private Double percentage;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrafficSource {
        private String source;
        private Integer visits;
        private Double percentage;
    }
}

