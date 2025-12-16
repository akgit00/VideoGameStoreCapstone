package org.yearup.data.mysql;

import org.springframework.stereotype.Component;
import org.yearup.data.CategoryDao;
import org.yearup.models.Category;
import org.apache.ibatis.jdbc.SQL;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.sql.Connection;

@Component
public class MySqlCategoryDao extends MySqlDaoBase implements CategoryDao {

    public MySqlCategoryDao(DataSource ds) {

        super(ds);
    }

    @Override
    public List<Category> getAllCategories() {
        List<Category> categories = new ArrayList<>();

        try (
                Connection c = ds.getConnection();
                PreparedStatement q = c.prepareStatement("""
                        SELECT category_id, name, description
                        FROM categories
                        """);
                ResultSet r = q.executeQuery();
        ) {
            while (r.next()) {
                Category cat = new Category();
                cat.setCategoryId(r.getInt("Category_id"));
                cat.setName(r.getString("Name"));
                cat.setDescription(r.getString("Description"));

                categories.add(cat);
            }
        } catch (SQLException e) {
            System.out.println("Error getting all categories");
        }

        return categories;
    }


    @Override
    public Category getById(int categoryId) {
        Category cat = new Category();

        try(Connection c = ds.getConnection();
            PreparedStatement q = c.prepareStatement("""
                SELECT 
                    category_id, name, description
                FROM 
                    categories
                WHERE 
                    category_id = ?
                """)){
            q.setInt(1,categoryId);

            ResultSet r = q.executeQuery();

            if(r.next()){
                cat.setCategoryId(r.getInt("Category_id"));
                cat.setName(r.getString("Name"));
                cat.setDescription(r.getString("Description"));
            }else{
                throw new ResponseStatusException(HttpStatus.NOT_FOUND);
            }
        }catch(SQLException e){
            System.out.println("Error getting category with id: " + categoryId);
        }
        return cat;
    }

    @Override
    public Category create(Category category) {


    }

    @Override
    public void update(int categoryId, Category category)
    {
        // update category
    }

    @Override
    public void delete(int categoryId)
    {
        // delete category
    }

    private Category mapRow(ResultSet row) throws SQLException
    {
        int categoryId = row.getInt("category_id");
        String name = row.getString("name");
        String description = row.getString("description");

        Category category = new Category()
        {{
            setCategoryId(categoryId);
            setName(name);
            setDescription(description);
        }};

        return category;
    }

}
