package org.yearup.models;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.yearup.data.OrderDao;
import org.yearup.data.ProductDao;
import org.yearup.data.ProfileDao;
import org.yearup.data.ShoppingCartDao;

import java.math.BigDecimal;

@Service
public class CheckoutService {

    private ShoppingCartDao shoppingCartDao;
    private OrderDao orderDao;
    private ProductDao productDao;
    private ProfileDao profileDao;

    @Autowired
    public CheckoutService(ShoppingCartDao shoppingCartDao, OrderDao orderDao, ProductDao productDao, ProfileDao profileDao) {
        this.shoppingCartDao = shoppingCartDao;
        this.orderDao = orderDao;
        this.productDao = productDao;
        this.profileDao = profileDao;
    }

    @Transactional
    public BigDecimal checkout(int userId){


        ShoppingCart cart = shoppingCartDao.getByUserId(userId);


        Profile profile = profileDao.getProfileByUserID(userId);


        orderDao.createOrder(profile,cart);


        cart.getItems().values().forEach(item -> {
            productDao.updateStock(item.getProductId(), item.getQuantity());
            orderDao.addOrderToDatabase(userId, item);
        });


        shoppingCartDao.clearCart(userId);


        return cart.getTotal();
    }
}