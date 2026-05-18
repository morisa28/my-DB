package com.example.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.mall.common.PageResult;
import com.example.mall.dto.AdminPaymentConfirmDTO;
import com.example.mall.dto.OrderCreateDTO;
import com.example.mall.dto.OrderQueryDTO;
import com.example.mall.dto.PaymentNoteDTO;
import com.example.mall.dto.ShipOrderDTO;
import com.example.mall.entity.Address;
import com.example.mall.entity.CartItem;
import com.example.mall.entity.OrderInfo;
import com.example.mall.entity.OrderItem;
import com.example.mall.entity.Product;
import com.example.mall.entity.User;
import com.example.mall.exception.BusinessException;
import com.example.mall.mapper.AddressMapper;
import com.example.mall.mapper.CartItemMapper;
import com.example.mall.mapper.OrderInfoMapper;
import com.example.mall.mapper.OrderItemMapper;
import com.example.mall.mapper.ProductMapper;
import com.example.mall.mapper.UserMapper;
import com.example.mall.security.UserContext;
import com.example.mall.service.OrderService;
import com.example.mall.vo.OrderCreateResultVO;
import com.example.mall.vo.OrderDetailVO;
import com.example.mall.vo.OrderItemVO;
import com.example.mall.vo.OrderVO;
import com.example.mall.vo.StatisticsVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl extends ServiceImpl<OrderInfoMapper, OrderInfo> implements OrderService {
    private static final int STATUS_WAITING_PAYMENT = 0;
    private static final int STATUS_WAITING_SHIPMENT = 1;
    private static final int STATUS_SHIPPED = 2;
    private static final int STATUS_FINISHED = 3;
    private static final int STATUS_CANCELED = 4;

    private final AddressMapper addressMapper;
    private final CartItemMapper cartItemMapper;
    private final ProductMapper productMapper;
    private final OrderItemMapper orderItemMapper;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public OrderCreateResultVO createOrder(OrderCreateDTO dto) {
        Long userId = UserContext.userId();
        Address address = addressMapper.selectById(dto.getAddressId());
        if (address == null || !userId.equals(address.getUserId())) {
            throw new BusinessException("收货地址不存在");
        }

        List<CartItem> cartItems = cartItemMapper.selectList(new LambdaQueryWrapper<CartItem>()
                .eq(CartItem::getUserId, userId)
                .in(CartItem::getId, dto.getCartItemIds()));
        if (cartItems.size() != dto.getCartItemIds().size()) {
            throw new BusinessException("购物车数据已变化，请刷新后重试");
        }

        Map<Long, Product> productMap = productMapper.selectBatchIds(
                cartItems.stream().map(CartItem::getProductId).collect(Collectors.toSet())
        ).stream().collect(Collectors.toMap(Product::getId, product -> product));

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem cartItem : cartItems) {
            Product product = productMap.get(cartItem.getProductId());
            if (product == null || !Integer.valueOf(1).equals(product.getStatus())) {
                throw new BusinessException("商品不存在或已下架");
            }
            if (cartItem.getQuantity() > product.getStock()) {
                throw new BusinessException("商品库存不足：" + product.getName());
            }
            BigDecimal itemTotal = product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            totalAmount = totalAmount.add(itemTotal);

            OrderItem item = new OrderItem();
            item.setProductId(product.getId());
            item.setProductName(product.getName());
            item.setProductPrice(product.getPrice());
            item.setQuantity(cartItem.getQuantity());
            item.setTotalPrice(itemTotal);
            item.setProductImage(product.getImageUrl());
            orderItems.add(item);
        }

        OrderInfo order = new OrderInfo();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setTotalAmount(totalAmount);
        order.setStatus(STATUS_WAITING_PAYMENT);
        order.setReceiverName(address.getReceiverName());
        order.setReceiverPhone(address.getReceiverPhone());
        order.setReceiverAddress(formatAddress(address));
        save(order);

        // 数据库课程展示重点：订单主表、明细快照、库存扣减、购物车清理必须同一事务提交或回滚。
        for (OrderItem item : orderItems) {
            item.setOrderId(order.getId());
            orderItemMapper.insert(item);
            int affectedRows = productMapper.safeDecreaseStock(item.getProductId(), item.getQuantity());
            if (affectedRows == 0) {
                throw new BusinessException("库存扣减失败：" + item.getProductName());
            }
        }

        cartItemMapper.delete(new LambdaQueryWrapper<CartItem>()
                .eq(CartItem::getUserId, userId)
                .in(CartItem::getId, dto.getCartItemIds()));

        return new OrderCreateResultVO(order.getId(), order.getOrderNo(), order.getTotalAmount());
    }

    @Override
    public PageResult<OrderVO> pageCurrentUserOrders(OrderQueryDTO query) {
        Page<OrderInfo> page = pageOrders(query, UserContext.userId());
        return toOrderPage(page, true);
    }

    @Override
    public OrderDetailVO getCurrentUserOrderDetail(Long id) {
        OrderInfo order = getById(id);
        if (order == null || !UserContext.userId().equals(order.getUserId())) {
            throw new BusinessException("订单不存在");
        }
        return toOrderDetailVO(order);
    }

    @Override
    @Transactional
    public void submitPaymentNote(Long id, PaymentNoteDTO dto) {
        OrderInfo order = getOwnOrder(id);
        if (!Integer.valueOf(STATUS_WAITING_PAYMENT).equals(order.getStatus())) {
            throw new BusinessException("只有待支付订单可以提交付款备注");
        }
        order.setPaymentNote(normalizeText(dto == null ? null : dto.getPaymentNote()));
        updateById(order);
    }

    @Override
    @Transactional
    public void confirmPayment(Long id, AdminPaymentConfirmDTO dto) {
        OrderInfo order = getExistingOrder(id);
        if (!Integer.valueOf(STATUS_WAITING_PAYMENT).equals(order.getStatus())) {
            throw new BusinessException("只有待支付订单可以确认收款");
        }
        order.setStatus(STATUS_WAITING_SHIPMENT);
        order.setPayTime(LocalDateTime.now());
        updateAdminRemark(order, dto == null ? null : dto.getAdminRemark());
        updateById(order);
    }

    @Override
    @Transactional
    public void cancelOrder(Long id) {
        OrderInfo order = getOwnOrder(id);
        if (!Integer.valueOf(STATUS_WAITING_PAYMENT).equals(order.getStatus())) {
            throw new BusinessException("只有待支付订单可以取消");
        }
        restoreStockAndSales(order.getId());
        order.setStatus(STATUS_CANCELED);
        order.setCancelTime(LocalDateTime.now());
        updateById(order);
    }

    @Override
    @Transactional
    public void confirmReceipt(Long id) {
        OrderInfo order = getOwnOrder(id);
        if (!Integer.valueOf(STATUS_SHIPPED).equals(order.getStatus())) {
            throw new BusinessException("只有已发货订单可以确认收货");
        }
        LocalDateTime now = LocalDateTime.now();
        order.setStatus(STATUS_FINISHED);
        order.setFinishTime(now);
        order.setConfirmTime(now);
        updateById(order);
    }

    @Override
    public PageResult<OrderVO> pageAdminOrders(OrderQueryDTO query) {
        Page<OrderInfo> page = pageOrders(query, null);
        return toOrderPage(page, true);
    }

    @Override
    public OrderDetailVO getAdminOrderDetail(Long id) {
        OrderInfo order = getById(id);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        return toOrderDetailVO(order);
    }

    @Override
    @Transactional
    public void shipOrder(Long id, ShipOrderDTO dto) {
        OrderInfo order = getExistingOrder(id);
        if (!Integer.valueOf(STATUS_WAITING_SHIPMENT).equals(order.getStatus())) {
            throw new BusinessException("只有已确认收款的订单可以发货");
        }
        String shippingNo = normalizeText(dto == null ? null : dto.getShippingNo());
        String adminRemark = normalizeText(dto == null ? null : dto.getAdminRemark());
        if (shippingNo == null && adminRemark == null) {
            throw new BusinessException("请填写物流单号或发货备注");
        }
        order.setStatus(STATUS_SHIPPED);
        order.setShippingNo(shippingNo);
        updateAdminRemark(order, adminRemark);
        order.setShipTime(LocalDateTime.now());
        updateById(order);
    }

    @Override
    public StatisticsVO statistics() {
        StatisticsVO vo = new StatisticsVO();
        vo.setTotalOrders(baseMapper.countAllOrders());
        vo.setTotalSalesAmount(baseMapper.sumPaidAmount());
        vo.setWaitingShipOrders(baseMapper.countWaitingShipOrders());
        vo.setTotalProducts(productMapper.selectCount(null));
        vo.setLowStockProducts(productMapper.selectLowStockProducts());
        vo.setTopProducts(productMapper.selectTopProducts());
        return vo;
    }

    private Page<OrderInfo> pageOrders(OrderQueryDTO query, Long userId) {
        LambdaQueryWrapper<OrderInfo> wrapper = new LambdaQueryWrapper<OrderInfo>()
                .eq(userId != null, OrderInfo::getUserId, userId)
                .eq(query.getStatus() != null, OrderInfo::getStatus, query.getStatus())
                .orderByDesc(OrderInfo::getCreateTime);
        return page(new Page<>(query.getPage(), query.getSize()), wrapper);
    }

    private PageResult<OrderVO> toOrderPage(Page<OrderInfo> page, boolean withItems) {
        List<OrderVO> records = page.getRecords().stream()
                .map(order -> toOrderVO(order, withItems))
                .toList();
        return new PageResult<>(page.getTotal(), page.getCurrent(), page.getSize(), records);
    }

    private OrderVO toOrderVO(OrderInfo order, boolean withItems) {
        OrderVO vo = new OrderVO();
        BeanUtils.copyProperties(order, vo);
        User user = userMapper.selectById(order.getUserId());
        vo.setUsername(user == null ? null : user.getUsername());
        if (withItems) {
            vo.setItems(orderItemMapper.selectItemsByOrderId(order.getId()));
        }
        return vo;
    }

    private OrderDetailVO toOrderDetailVO(OrderInfo order) {
        OrderVO source = toOrderVO(order, true);
        OrderDetailVO detail = new OrderDetailVO();
        BeanUtils.copyProperties(source, detail);
        detail.setItems(source.getItems());
        return detail;
    }

    private OrderInfo getExistingOrder(Long id) {
        OrderInfo order = getById(id);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        return order;
    }

    private OrderInfo getOwnOrder(Long id) {
        OrderInfo order = getById(id);
        if (order == null || !Objects.equals(UserContext.userId(), order.getUserId())) {
            throw new BusinessException("订单不存在");
        }
        return order;
    }

    private void restoreStockAndSales(Long orderId) {
        List<OrderItem> items = orderItemMapper.selectList(new LambdaQueryWrapper<OrderItem>()
                .eq(OrderItem::getOrderId, orderId));
        for (OrderItem item : items) {
            int affectedRows = productMapper.restoreStockFromCanceledOrder(item.getProductId(), item.getQuantity());
            if (affectedRows == 0) {
                throw new BusinessException("库存恢复失败：" + item.getProductName());
            }
        }
    }

    private void updateAdminRemark(OrderInfo order, String newRemark) {
        String remark = normalizeText(newRemark);
        if (remark != null) {
            order.setAdminRemark(remark);
        }
    }

    private String normalizeText(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String formatAddress(Address address) {
        return String.join("",
                address.getProvince() == null ? "" : address.getProvince(),
                address.getCity() == null ? "" : address.getCity(),
                address.getDetailAddress());
    }

    private String generateOrderNo() {
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        return "M" + time + suffix;
    }
}
