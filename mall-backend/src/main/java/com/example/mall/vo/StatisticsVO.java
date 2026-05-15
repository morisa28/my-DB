package com.example.mall.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class StatisticsVO {
    private Long totalOrders;
    private BigDecimal totalSalesAmount;
    private Long waitingShipOrders;
    private Long totalProducts;
    private List<LowStockProductVO> lowStockProducts;
    private List<ProductRankVO> topProducts;
}

