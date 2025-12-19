package org.yearup.data.mysql;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.yearup.data.CategoryDao;
import org.yearup.models.Category;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Component     //marks this class as a Spring Bean so it can be auto-wired
public class MySqlCategoryDao extends MySqlDaoBase implements CategoryDao {

    //constructor injection of DataSource, passed to parent MySqlDaoBase
    public MySqlCategoryDao(DataSource ds){
        super(ds);
        //stores DataSource in the protected variable `ds`
    }

    @Override
    public List<Category> getAllCategories() {

        List<Category> categories = new ArrayList<>();

        //try-with-resources automatically closes DB connection, statement & result set
        try(
                Connection c = ds.getConnection();      // Open a DB connection
                PreparedStatement q = c.prepareStatement("""
                        SELECT category_id, name, description
                        FROM categories
                        """);                            // SQL query to retrieve all categories
                ResultSet r = q.executeQuery();          // Execute query and store results
        ){
            //loop through result rows
            while(r.next()){
                Category cat = new Category();
                //map each column to a Category object
                cat.setCategoryId(r.getInt("Category_id"));
                cat.setName(r.getString("Name"));
                cat.setDescription(r.getString("Description"));

                categories.add(cat); // Add to the list
            }
        }catch(SQLException e){
            System.out.println("Error getting all categories " + e);
        }

        return categories;
        //return all categories retrieved
    }

    @Override
    public Category getById(int categoryId) {

        Category cat = new Category();

        //get category from DB using its ID
        try(Connection c = ds.getConnection();
            PreparedStatement q = c.prepareStatement("""
                SELECT
                    category_id, name, description
                FROM
                    categories
                WHERE
                    category_id = ?
                """)){
            q.setInt(1,categoryId);  // Bind parameter to query

            try(
                    ResultSet r = q.executeQuery()
            ){
                if(r.next()){
                    //populate object if record is found
                    cat.setCategoryId(r.getInt("Category_id"));
                    cat.setName(r.getString("Name"));
                    cat.setDescription(r.getString("Description"));
                }else{
                    //throw 404 if category doesn't exist
                    throw new ResponseStatusException(HttpStatus.NOT_FOUND);
                }
            }

        }catch(SQLException e){
            System.out.println("Error getting category with id: " + categoryId);
        }
        return cat;
    }

    @Override
    public Category create(Category category) {

        //insert a new category into the database
        try(Connection c = ds.getConnection();
            PreparedStatement q = c.prepareStatement("""
                INSERT INTO Categories(Name, Description) VALUES(?,?)
                """)){
            q.setString(1, category.getName());         // Bind values to placeholders
            q.setString(2, category.getDescription());

            q.executeUpdate();                          // Execute INSERT
        }catch (SQLException e){
            System.out.println("Error adding category");
        }
        return category;
    }

    @Override
    public void update(int categoryId, Category category) {

        //update fields using COALESCE so nulls keep original values
        try(Connection c = ds.getConnection();
            PreparedStatement q = c.prepareStatement("""
                UPDATE
                    categories
                SET
                    Category_ID = COALESCE(?, Category_ID),
                    Name = COALESCE(?, Name),
                    Description = COALESCE(?, Description)
                WHERE
                    Category_ID = ?
                """)){

            //if null or zero, keep original ID
            if(category.getCategoryId() == null || category.getCategoryId() == 0){
                q.setNull(1, Types.INTEGER);
            }else{
                q.setInt(1, category.getCategoryId());
            }
            q.setString(2, category.getName());         // Null-safe update
            q.setString(3, category.getDescription());

            q.setInt(4, categoryId);                    // WHERE clause target

            q.executeUpdate();                          // Execute update
        }catch(SQLException e){
            System.out.println("Error updating category");
        }

    }

    @Override
    public void delete(int categoryId) {
        //remove category from DB by ID
        try(Connection c = ds.getConnection();
            PreparedStatement q = c.prepareStatement("""
                DELETE FROM Categories
                WHERE Category_id = ?
                """)){
            q.setInt(1, categoryId);      // Bind id

            q.executeUpdate();            // Execute delete
        }catch(SQLException e){
            System.out.println("Error removing category" + e);
        }

    }

    //helper method to map a result row to a Category object
    private Category mapRow(ResultSet row) throws SQLException {
        int categoryId = row.getInt("category_id");
        String name = row.getString("name");
        String description = row.getString("description");

        //double-brace initialization (creates anonymous object w/ setters)
        Category category = new Category(){{
            setCategoryId(categoryId);
            setName(name);
            setDescription(description);
        }};

        return category;
    }

}