package org.yearup.models;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.yearup.data.OrderDao;
import org.yearup.data.ProductDao;
import org.yearup.data.ProfileDao;
import org.yearup.data.ShoppingCartDao;

import java.math.BigDecimal;

@Service //marks this as a Spring service component (business logic layer)
public class CheckoutService {

    // DAOs needed to complete the checkout process
    private ShoppingCartDao shoppingCartDao;
    private OrderDao orderDao;
    private ProductDao productDao;
    private ProfileDao profileDao;

    // Constructor injection for dependency management (recommended way)
    @Autowired
    public CheckoutService(ShoppingCartDao shoppingCartDao, OrderDao orderDao, ProductDao productDao, ProfileDao profileDao) {
        this.shoppingCartDao = shoppingCartDao;
        this.orderDao = orderDao;
        this.productDao = productDao;
        this.profileDao = profileDao;
    }

    /**
     *  @Transactional ensures:
     * - creating the order
     * - updating stock
     * - adding order items
     * - and clearing the cart
     * happen as ONE atomic unit of work.
     *
     * If any part fails (errors, SQL exception, etc.),
     * the ENTIRE transaction is rolled back.
     *
     * This prevents issues like:
     *  - charging a user
     *  - without reducing inventory
     *  - or leaving a cart uncleared
     *
     * Basically, all-or-nothing safety.
     */
    @Transactional
    public BigDecimal checkout(int userId){

        //retrieve the user's current shopping cart
        ShoppingCart cart = shoppingCartDao.getByUserId(userId);

        //retrieve customer profile (shipping & contact info, etc.)
        Profile profile = profileDao.getProfileByUserID(userId);

        //create the order and return its order ID
        int orderId = orderDao.createOrder(profile, cart);

        //loop through each item in the cart:
        //reduce stock from inventory
        //write individual line items to order_items table
        cart.getItems().values().forEach(item -> {
            orderDao.updateStock(item.getProductId(), item.getQuantity());
            orderDao.addOrderToDatabase(orderId, item);
        });

        //clear the cart after successful checkout
        shoppingCartDao.clearCart(userId);

        //return the total amount charged to the user
        return cart.getTotal();
    }
}