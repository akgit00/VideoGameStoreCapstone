package org.yearup.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.yearup.data.*;
import org.yearup.models.Profile;
import org.yearup.models.ShoppingCart;
import org.yearup.models.ShoppingCartItem;
import org.yearup.models.User;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

// convert this class to a REST controller
@RestController

// only logged in users should have access to these actions
@PreAuthorize("hasAnyRole('USER','ADMIN')")
@RequestMapping("/cart")
@CrossOrigin

public class ShoppingCartController
{
    // a shopping cart requires
    private ShoppingCartDao shoppingCartDao;
    private UserDao userDao;
    private ProductDao productDao;
    private ProfileDao profileDao;
    private OrderDao orderDao;

    //this is a constructor that injects the required DAO dependencies for the controller
    @Autowired
    public ShoppingCartController(ShoppingCartDao shoppingCartDao, UserDao userDao, ProductDao productDao, ProfileDao profileDao, OrderDao orderDao) {
        this.shoppingCartDao = shoppingCartDao;
        this.userDao = userDao;
        this.productDao = productDao;
        this.profileDao = profileDao;
        this.orderDao = orderDao;
    }



    @GetMapping("")
    // each method in this controller requires a Principal object as a parameter
    public ShoppingCart getCart(Principal principal)
    {
        try
        {
            // get the currently logged in username
            String userName = principal.getName();
            // find database user by userId
            User user = userDao.getByUserName(userName);
            int userId = user.getId();

            // use the shoppingcartDao to get all items in the cart and return the cart
            return shoppingCartDao.getByUserId(userId);
        }
        catch(Exception e)
        {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Oops... our bad." + e);
        }
    }

    // add a POST method to add a product to the cart - the url should be
    // https://localhost:8080/cart/products/15 (15 is the productId to be added
    @PostMapping("/products/{id}")
    @ResponseStatus(value = HttpStatus.CREATED)

    public ShoppingCart addToCart(Principal principal, @PathVariable int id){
        String userName = principal.getName();
        User user = userDao.getByUserName(userName);
        int userId = user.getId();

        shoppingCartDao.addToCart(id, userId);
        return shoppingCartDao.getByUserId(userId);
    }



    // add a PUT method to update an existing product in the cart - the url should be
    // https://localhost:8080/cart/products/15 (15 is the productId to be updated)
    // the BODY should be a ShoppingCartItem - quantity is the only value that will be updated

    @PutMapping("/products/{id}")
    public ShoppingCart updateQuantity(Principal principal, @PathVariable int id, @RequestBody ShoppingCartItem item){
        String userName = principal.getName();
        User user = userDao.getByUserName(userName);
        int userId = user.getId();

        shoppingCartDao.editCart(id, userId, item.getQuantity());
        return shoppingCartDao.getByUserId(userId);
    }


    // add a DELETE method to clear all products from the current users cart
    // https://localhost:8080/cart

    @DeleteMapping("")
    public ShoppingCart clearCart(Principal principal){
        String userName = principal.getName();
        User user = userDao.getByUserName(userName);
        int userID = user.getId();

        shoppingCartDao.clearCart(userID);
        return new ShoppingCart();
    }

    @PostMapping("/checkout")
    public Map<String, Object> checkout(Principal principal) {
        //retrieve the authenticated user from Spring Security
        User user = userDao.getByUserName(principal.getName());

        //get the user's profile, which contains shipping and address information
        Profile profile = profileDao.getProfileByUserID(user.getId());

        //get the user's current shopping cart, including items and totals
        ShoppingCart cart = shoppingCartDao.getByUserId(user.getId());

        //calculate the total cost of the items in the cart
        BigDecimal total = cart.getTotal();

        //create a new order in the database using profile & cart data
        orderDao.checkout(profile, cart);

        //clear the user's cart after successful checkout
        shoppingCartDao.clearCart(user.getId());

        //build a receipt response containing the total and confirmation message
        Map<String, Object> receipt = new HashMap<>();
        receipt.put("total", total);
        receipt.put("message", "Checkout Successful!");

        //return the receipt as API response
        return receipt;
    }
    }
