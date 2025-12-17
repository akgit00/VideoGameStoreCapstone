package org.yearup.data.mysql;

import org.springframework.stereotype.Component;
import org.yearup.data.ShoppingCartDao;
import org.yearup.models.Product;
import org.yearup.models.ShoppingCart;
import org.yearup.models.ShoppingCartItem;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Component
public class MySqlShoppingCartDao extends MySqlDaoBase implements ShoppingCartDao {

    //this is a constructor that passes the DataSource to the base class
    public MySqlShoppingCartDao(DataSource ds) {
        super(ds);
    }

    @Override
    public ShoppingCart getByUserId(int userId) {
        //create an empty ShoppingCart that we will fill with results
        ShoppingCart cart = new ShoppingCart();

        //open a database connection and prepare a SQL query
        try(Connection c = ds.getConnection();
            PreparedStatement q = c.prepareStatement("""
                    SELECT user_id, S.product_id, quantity, P.*
                    FROM shopping_cart S
                    JOIN products P ON (P.product_id = S.Product_ID)
                    WHERE user_ID = ?
                    """)) //get all cart items for this user and join with product details
        {
            //bind the userId to the query parameter
            q.setInt(1, userId);

            //execute the query and receive the result set
            ResultSet r = q.executeQuery();

            //loop through each row in the result
            while(r.next()){
                //create a Product model and set its fields using the row data
                Product product = new Product();
                int quantity = r.getInt("quantity");

                product.setProductId(r.getInt("product_id"));
                product.setName(r.getString("name"));
                product.setPrice(r.getBigDecimal("price"));
                product.setCategoryId(r.getInt("category_id"));
                product.setDescription(r.getString("description"));
                product.setSubCategory(r.getString("subcategory"));
                product.setStock(r.getInt("stock"));
                product.setFeatured(r.getBoolean("featured"));
                product.setImageUrl(r.getString("image_url"));

                //create a ShoppingCartItem containing product + quantity + user
                ShoppingCartItem cartItem = new ShoppingCartItem(product, userId, quantity);

                //add the item to the cart
                cart.add(cartItem);
            }
        }catch(SQLException e){
            //log any SQL/connection errors
            System.out.println("Error getting cart by userid " + e);
        }

        //return the populated ShoppingCart (empty if none found)
        return cart;
    }

    @Override
    public void addToCart(int productId, int userID) {
        try (
                Connection c = ds.getConnection();
                /* insert the product into the cart with quantity 1.
                if the user already has this product in their cart, update the existing row
                by incrementing quantity instead of creating a duplicate row
                 */
                PreparedStatement q = c.prepareStatement("""
                    INSERT INTO shopping_cart (user_id, product_id, quantity) VALUES (?, ?, 1)
                    ON DUPLICATE KEY UPDATE quantity = quantity + 1
                    """)
        ) {
            //bind user ID and product ID to the query
            q.setInt(1, userID);
            q.setInt(2, productId);

            q.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error adding to cart: " + e);
        }
    }

    @Override
    public void clearCart(int userID) {
        try (
                Connection c = ds.getConnection();
                //delete all cart entries for the given user
                PreparedStatement q = c.prepareStatement("""
                    DELETE FROM shopping_cart
                    WHERE user_id = ?
                    """)
        ) {
            q.setInt(1, userID);

            q.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error clearing cart: " + e);
        }
    }

    @Override
    public void editCart(int productID, int userID, int quantity) {
        //update the product quantity in the cart for the user
        //only update rows where quantity is >= 1
        try (
                Connection c = ds.getConnection();
                PreparedStatement q = c.prepareStatement("""
                    UPDATE shopping_cart
                    SET quantity = ?
                    WHERE user_id = ?
                    AND product_id = ?
                    AND quantity >= 1
                    """)
        ) {
            q.setInt(1, quantity);
            q.setInt(2, userID);
            q.setInt(3, productID);

            q.executeUpdate();

            /* if the requested quantity is zero
            remove the item completely instead of leaving a row with quantity 0.
             */
            if (quantity == 0) {
                try (
                        PreparedStatement rQ = c.prepareStatement("""
                            DELETE FROM shopping_cart
                            WHERE quantity = 0
                            """)
                ) {
                    rQ.executeUpdate();
                } catch (SQLException e) {
                    System.out.println("Error removing item: " + e);
                }
            }

        } catch (SQLException e) {
            System.out.println("Error editing cart: " + e);
        }
    }
}