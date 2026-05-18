package com.example.mall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.mall.entity.Product;
import com.example.mall.vo.LowStockProductVO;
import com.example.mall.vo.ProductRankVO;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface ProductMapper extends BaseMapper<Product> {

    @Update("""
            UPDATE product
            SET stock = stock - #{quantity}, sales = sales + #{quantity}
            WHERE id = #{productId}
              AND stock >= #{quantity}
              AND status = 1
            """)
    int safeDecreaseStock(Long productId, Integer quantity);

    @Update("""
            UPDATE product
            SET stock = stock + #{quantity}, sales = GREATEST(sales - #{quantity}, 0)
            WHERE id = #{productId}
            """)
    int restoreStockFromCanceledOrder(Long productId, Integer quantity);

    @Select("""
            SELECT id AS productId, name AS productName, stock
            FROM product
            WHERE status = 1 AND stock < 10
            ORDER BY stock ASC, id ASC
            LIMIT 10
            """)
    List<LowStockProductVO> selectLowStockProducts();

    @Select("""
            SELECT oi.product_id AS productId,
                   oi.product_name AS productName,
                   SUM(oi.quantity) AS sales,
                   SUM(oi.total_price) AS salesAmount
            FROM order_item oi
            INNER JOIN order_info o ON oi.order_id = o.id
            WHERE o.status IN (1, 2, 3)
            GROUP BY oi.product_id, oi.product_name
            ORDER BY sales DESC, salesAmount DESC
            LIMIT 5
            """)
    List<ProductRankVO> selectTopProducts();
}
