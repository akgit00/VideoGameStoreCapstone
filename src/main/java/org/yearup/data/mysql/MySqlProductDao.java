package org.yearup.data.mysql;

import org.springframework.stereotype.Component;
import org.yearup.models.Product;
import org.yearup.data.ProductDao;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Component //marks this class as a Spring bean so it can be injected where needed
public class MySqlProductDao extends MySqlDaoBase implements ProductDao
{
    //pass the DataSource to the base class to manage connections
    public MySqlProductDao(DataSource dataSource)
    {
        super(dataSource);
    }

    @Override
    public List<Product> search(Integer categoryId, BigDecimal minPrice, BigDecimal maxPrice, String subCategory)
    {
        List<Product> products = new ArrayList<>();

        //SQL query uses OR logic to allow optional filters using -1 / "" as default values
        String sql = "SELECT * FROM products " +
                "WHERE (category_id = ? OR ? = -1) " +
                "   AND (price >= ? OR ? = -1) " +
                "   AND (price >= ? OR ? = -1) " +
                "   AND (subcategory = ? OR ? = '') ";

        //normalize null inputs to fallback values for filtering logic
        categoryId = categoryId == null ? -1 : categoryId;
        minPrice = minPrice == null ? new BigDecimal("-1") : minPrice;
        maxPrice = maxPrice == null ? new BigDecimal("-1") : maxPrice;
        subCategory = subCategory == null ? "" : subCategory;

        //connection + prepared statement using try-with-resources to avoid leaks
        try (Connection connection = getConnection())
        {
            PreparedStatement statement = connection.prepareStatement(sql);

            //set filter values twice due to OR matching structure
            statement.setInt(1, categoryId);
            statement.setInt(2, categoryId);
            statement.setBigDecimal(3, minPrice);
            statement.setBigDecimal(4, minPrice);
            statement.setBigDecimal(5,maxPrice);
            statement.setBigDecimal(6,maxPrice);
            statement.setString(7, subCategory);
            statement.setString(8, subCategory);

            ResultSet row = statement.executeQuery();

            //map every row to a Product model object
            while (row.next())
            {
                Product product = mapRow(row);
                products.add(product);
            }
        }
        catch (SQLException e)
        {
            //export exception as unchecked runtime for simplicity
            throw new RuntimeException(e);
        }

        return products;
    }

    @Override
    public List<Product> listByCategoryId(int categoryId)
    {
        List<Product> products = new ArrayList<>();

        //simple query filtered only by category
        String sql = "SELECT * FROM products " +
                " WHERE category_id = ? ";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql))
        {
            statement.setInt(1, categoryId);

            ResultSet row = statement.executeQuery();

            //loop and map each row to a product
            while (row.next())
            {
                Product product = mapRow(row);
                products.add(product);
            }
        }
        catch (SQLException e)
        {
            System.out.println("Error listing product by categoryid " + e);
        }

        return products;
    }

    @Override
    public Product getById(int productId)
    {
        String sql = "SELECT * FROM products WHERE product_id = ?";
        try (Connection connection = getConnection())
        {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, productId);

            ResultSet row = statement.executeQuery();

            //if product exists, convert that row to product model
            if (row.next())
            {
                return mapRow(row);
            }
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public Product create(Product product)
    {
        //insert statement with all product properties
        String sql = "INSERT INTO products(name, price, category_id, description, subcategory, image_url, stock, featured) " +
                " VALUES (?, ?, ?, ?, ?, ?, ?, ?);";

        try (Connection connection = getConnection())
        {
            //ask for generated keys because we want the auto-increment ID back
            PreparedStatement statement = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
            statement.setString(1, product.getName());
            statement.setBigDecimal(2, product.getPrice());
            statement.setInt(3, product.getCategoryId());
            statement.setString(4, product.getDescription());
            statement.setString(5, product.getSubCategory());
            statement.setString(6, product.getImageUrl());
            statement.setInt(7, product.getStock());
            statement.setBoolean(8, product.isFeatured());

            int rowsAffected = statement.executeUpdate();

            if (rowsAffected > 0) {
                //retrieve ID from auto-increment column
                ResultSet generatedKeys = statement.getGeneratedKeys();

                if (generatedKeys.next()) {
                    int orderId = generatedKeys.getInt(1);

                    //return newly inserted product
                    return getById(orderId);
                }
            }
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public void update(int productId, Product product)
    {
        //COALESCE allows partial updates — only non-null fields overwrite data
        String sql = "UPDATE products" +
                " SET name = COALESCE(?, name) " +
                "   , price = COALESCE(?, price) " +
                "   , category_id = COALESCE(?,category_id) " +
                "   , description = COALESCE(?,description) " +
                "   , subcategory = COALESCE(?, subcategory) " +
                "   , image_url = COALESCE(?, image_url) " +
                "   , stock = COALESCE(?, stock) " +
                "   , featured = COALESCE(?, featured) " +
                " WHERE product_id = ?;";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)
        ){
            statement.setString(1, product.getName());
            statement.setBigDecimal(2, product.getPrice());
            statement.setObject(3, product.getCategoryId());
            statement.setString(4, product.getDescription());
            statement.setString(5, product.getSubCategory());
            statement.setString(6, product.getImageUrl());
            statement.setObject(7, product.getStock());
            statement.setObject(8, product.isFeatured());
            statement.setInt(9, productId);

            statement.executeUpdate();
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(int productId)
    {
        //delete product by primary key
        String sql = "DELETE FROM products " +
                " WHERE product_id = ?;";

        try (Connection connection = getConnection())
        {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, productId);

            statement.executeUpdate();
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }

    //this is a helper method to convert a ResultSet row into a Product model instance
    protected static Product mapRow(ResultSet row) throws SQLException
    {
        int productId = row.getInt("product_id");
        String name = row.getString("name");
        BigDecimal price = row.getBigDecimal("price");
        int categoryId = row.getInt("category_id");
        String description = row.getString("description");
        String subCategory = row.getString("subcategory");
        int stock = row.getInt("stock");
        boolean isFeatured = row.getBoolean("featured");
        String imageUrl = row.getString("image_url");

        //return product object containing DB values
        return new Product(productId, name, price, categoryId, description, subCategory, stock, isFeatured, imageUrl);
    }
}