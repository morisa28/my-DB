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
import com.example.mall.entity.OrderOperationLog;
import com.example.mall.entity.Product;
import com.example.mall.entity.User;
import com.example.mall.exception.BusinessException;
import com.example.mall.mapper.AddressMapper;
import com.example.mall.mapper.CartItemMapper;
import com.example.mall.mapper.OrderInfoMapper;
import com.example.mall.mapper.OrderItemMapper;
import com.example.mall.mapper.OrderOperationLogMapper;
import com.example.mall.mapper.ProductMapper;
import com.example.mall.mapper.UserMapper;
import com.example.mall.security.LoginUser;
import com.example.mall.security.UserContext;
import com.example.mall.service.OrderService;
import com.example.mall.vo.OrderCreateResultVO;
import com.example.mall.vo.OrderDetailVO;
import com.example.mall.vo.OrderItemVO;
import com.example.mall.vo.OrderOperationLogVO;
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

    private static final String ACTION_CREATE_ORDER = "CREATE_ORDER";
    private static final String ACTION_SUBMIT_PAYMENT_NOTE = "SUBMIT_PAYMENT_NOTE";
    private static final String ACTION_CONFIRM_PAYMENT = "CONFIRM_PAYMENT";
    private static final String ACTION_SHIP_ORDER = "SHIP_ORDER";
    private static final String ACTION_CANCEL_ORDER = "CANCEL_ORDER";
    private static final String ACTION_CONFIRM_RECEIPT = "CONFIRM_RECEIPT";

    private final AddressMapper addressMapper;
    private final CartItemMapper cartItemMapper;
    private final ProductMapper productMapper;
    private final OrderItemMapper orderItemMapper;
    private final OrderOperationLogMapper orderOperationLogMapper;
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

        int claimedRows = cartItemMapper.delete(new LambdaQueryWrapper<CartItem>()
                .eq(CartItem::getUserId, userId)
                .in(CartItem::getId, dto.getCartItemIds()));
        if (claimedRows != dto.getCartItemIds().size()) {
            throw new BusinessException("购物车数据已被结算，请刷新后重试");
        }

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

        recordOrderOperation(order, ACTION_CREATE_ORDER, null, STATUS_WAITING_PAYMENT, "用户创建订单");
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
        int affectedRows = baseMapper.updatePaymentNoteIfWaiting(
                id,
                UserContext.userId(),
                normalizeText(dto == null ? null : dto.getPaymentNote())
        );
        if (affectedRows == 0) {
            throw new BusinessException("订单状态已变化，请刷新后重试");
        }
        recordOrderOperation(order, ACTION_SUBMIT_PAYMENT_NOTE, STATUS_WAITING_PAYMENT, STATUS_WAITING_PAYMENT, "用户提交付款备注");
    }

    @Override
    @Transactional
    public void confirmPayment(Long id, AdminPaymentConfirmDTO dto) {
        OrderInfo order = getExistingOrder(id);
        if (!Integer.valueOf(STATUS_WAITING_PAYMENT).equals(order.getStatus())) {
            throw new BusinessException("只有待支付订单可以确认收款");
        }
        String adminRemark = mergeAdminRemark(order, dto == null ? null : dto.getAdminRemark());
        int affectedRows = baseMapper.confirmPaymentIfWaiting(id, LocalDateTime.now(), adminRemark);
        if (affectedRows == 0) {
            throw new BusinessException("订单状态已变化，请刷新后重试");
        }
        recordOrderOperation(order, ACTION_CONFIRM_PAYMENT, STATUS_WAITING_PAYMENT, STATUS_WAITING_SHIPMENT, normalizeText(dto == null ? null : dto.getAdminRemark()));
    }

    @Override
    @Transactional
    public void cancelOrder(Long id) {
        OrderInfo order = getOwnOrder(id);
        if (!Integer.valueOf(STATUS_WAITING_PAYMENT).equals(order.getStatus())) {
            throw new BusinessException("只有待支付订单可以取消");
        }
        int affectedRows = baseMapper.cancelIfWaitingPayment(order.getId(), UserContext.userId(), LocalDateTime.now());
        if (affectedRows == 0) {
            throw new BusinessException("订单状态已变化，请刷新后重试");
        }
        restoreStockAndSales(order.getId());
        recordOrderOperation(order, ACTION_CANCEL_ORDER, STATUS_WAITING_PAYMENT, STATUS_CANCELED, "用户取消待支付订单");
    }

    @Override
    @Transactional
    public void confirmReceipt(Long id) {
        OrderInfo order = getOwnOrder(id);
        if (!Integer.valueOf(STATUS_SHIPPED).equals(order.getStatus())) {
            throw new BusinessException("只有已发货订单可以确认收货");
        }
        LocalDateTime now = LocalDateTime.now();
        int affectedRows = baseMapper.confirmReceiptIfShipped(order.getId(), UserContext.userId(), now, now);
        if (affectedRows == 0) {
            throw new BusinessException("订单状态已变化，请刷新后重试");
        }
        recordOrderOperation(order, ACTION_CONFIRM_RECEIPT, STATUS_SHIPPED, STATUS_FINISHED, "用户确认收货");
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
    public List<OrderOperationLogVO> listAdminOrderLogs(Long id) {
        if (getById(id) == null) {
            throw new BusinessException("订单不存在");
        }
        return orderOperationLogMapper.selectByOrderId(id).stream()
                .map(this::toOrderOperationLogVO)
                .toList();
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
        int affectedRows = baseMapper.shipIfWaitingShipment(
                id,
                shippingNo,
                mergeAdminRemark(order, adminRemark),
                LocalDateTime.now()
        );
        if (affectedRows == 0) {
            throw new BusinessException("订单状态已变化，请刷新后重试");
        }
        recordOrderOperation(order, ACTION_SHIP_ORDER, STATUS_WAITING_SHIPMENT, STATUS_SHIPPED, buildShipRemark(shippingNo, adminRemark));
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

    private OrderOperationLogVO toOrderOperationLogVO(OrderOperationLog log) {
        OrderOperationLogVO vo = new OrderOperationLogVO();
        BeanUtils.copyProperties(log, vo);
        return vo;
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

    private String mergeAdminRemark(OrderInfo order, String newRemark) {
        String remark = normalizeText(newRemark);
        return remark == null ? order.getAdminRemark() : remark;
    }

    private String buildShipRemark(String shippingNo, String adminRemark) {
        if (shippingNo != null && adminRemark != null) {
            return "物流单号：" + shippingNo + "；备注：" + adminRemark;
        }
        if (shippingNo != null) {
            return "物流单号：" + shippingNo;
        }
        return adminRemark;
    }

    private void recordOrderOperation(OrderInfo order, String action, Integer fromStatus, Integer toStatus, String remark) {
        LoginUser operator = UserContext.get();
        OrderOperationLog log = new OrderOperationLog();
        log.setOrderId(order.getId());
        log.setOrderNo(order.getOrderNo());
        log.setOperatorId(operator.getId());
        log.setOperatorUsername(operator.getUsername());
        log.setOperatorRole(operator.getRole());
        log.setAction(action);
        log.setFromStatus(fromStatus);
        log.setToStatus(toStatus);
        log.setRemark(normalizeText(remark));
        orderOperationLogMapper.insert(log);
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
