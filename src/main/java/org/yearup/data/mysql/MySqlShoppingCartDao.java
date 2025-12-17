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

    //this is a helper method to convert a database ResultSet row into a Product object
    protected static Product mapRow(ResultSet row) throws SQLException {
        int productId = row.getInt("product_id");
        String name = row.getString("name");
        BigDecimal price = row.getBigDecimal("price");
        int categoryId = row.getInt("category_id");
        String description = row.getString("description");
        String subCategory = row.getString("subcategory");
        int stock = row.getInt("stock");
        boolean isFeatured = row.getBoolean("featured");
        String imageUrl = row.getString("image_url");

        //return a fully constructed Product
        return new Product(productId, name, price, categoryId, description, subCategory, stock, isFeatured, imageUrl);
    }
}