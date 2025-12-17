package org.yearup.data;

import org.yearup.models.ShoppingCart;
import java.util.List;

public interface ShoppingCartDao
{
    ShoppingCart getByUserId(int userId);
    // add additional method signatures here

    void addToCart(int productID, int userID);

    void clearCart(int userID);

    public void editCart(int productID, int userID,int quantity);
}
