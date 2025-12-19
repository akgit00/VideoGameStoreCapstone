package org.yearup.data.mysql;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.yearup.models.Profile;
import org.yearup.data.ProfileDao;

import javax.sql.DataSource;
import java.sql.*;

@Component //marks this as a Spring bean, so it can be dependency-injected
public class MySqlProfileDao extends MySqlDaoBase implements ProfileDao
{
    //constructor receives a DataSource object (configured in Spring)
    //and passes it to the base class, which handles DB connections.
    public MySqlProfileDao(DataSource dataSource)
    {
        super(dataSource);
    }

    @Override
    public Profile create(Profile profile)
    {
        //SQL INSERT query to add a new profile record to the DB
        String sql = "INSERT INTO profiles (user_id, first_name, last_name, phone, email, address, city, state, zip) " +
                " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        //try-with-resources ensures the Connection is closed automatically
        try(Connection connection = getConnection())
        {
            //prepare a statement and request generated keys
            PreparedStatement ps = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);

            //map the profile object's values into the SQL insert parameters
            ps.setInt(1, profile.getUserId());
            ps.setString(2, profile.getFirstName());
            ps.setString(3, profile.getLastName());
            ps.setString(4, profile.getPhone());
            ps.setString(5, profile.getEmail());
            ps.setString(6, profile.getAddress());
            ps.setString(7, profile.getCity());
            ps.setString(8, profile.getState());
            ps.setString(9, profile.getZip());

            //executes the INSERT statement
            ps.executeUpdate();

            //return the same profile object (it is already populated)
            return profile;
        }
        catch (SQLException e)
        {
            //wrap SQL exception into a runtime exception
            //so callers don't have to handle checked exceptions
            throw new RuntimeException(e);
        }
    }

    @Override
    public Profile update(int userId, Profile profile) {
        try (
                //get a DB connection
                Connection c = ds.getConnection();

                /*
                 * prepare an UPDATE statement that updates only the fields
                 * provided in the profile object.
                 * COALESCE ensures that if a value is null,
                 * the existing column value will remain unchanged.
                 */
                PreparedStatement q = c.prepareStatement("""
                    UPDATE 
                        Profiles
                    SET 
                        first_name = COALESCE(?, first_name),
                        last_name = COALESCE(?, last_name),
                        phone = COALESCE(?, phone),
                        email = COALESCE(?, email),
                        address = COALESCE(?, address),
                        city = COALESCE(?, city),
                        state = COALESCE(?, state),
                        zip = COALESCE(?, zip)
                    WHERE
                        user_id = ?
                    """)
        ) {
            //bind parameters from the Profile object to the SQL query
            q.setString(1, profile.getFirstName());
            q.setString(2, profile.getLastName());
            q.setString(3, profile.getPhone());
            q.setString(4, profile.getEmail());
            q.setString(5, profile.getAddress());
            q.setString(6, profile.getCity());
            q.setString(7, profile.getState());
            q.setString(8, profile.getZip());
            q.setInt(9, userId);

            //execute the update operation
            q.executeUpdate();
        }
        catch (SQLException e)
        {
            System.out.println("Error updating profile" + e);
        }
        return null; //method could return updated profile if desired
    }

    public Profile getProfileByUserID(int userID) {
        //create an empty Profile instance to populate
        Profile profile = new Profile();

        try (
                Connection c = ds.getConnection();

                //create a SELECT query to retrieve profile by user ID
                PreparedStatement q = c.prepareStatement("""
                    SELECT user_id, first_name, last_name, phone, email, address, city, state, zip
                    FROM Profiles
                    WHERE user_id = ?
                    """)
        ) {
            //bind the user ID parameter to the query
            q.setInt(1, userID);

            //execute query and receive results
            ResultSet r = q.executeQuery();

            if (r.next()) {
                //populate the profile object with DB column values
                profile.setUserId(userID);
                profile.setFirstName(r.getString("first_name"));
                profile.setLastName(r.getString("last_name"));
                profile.setPhone(r.getString("phone"));
                profile.setEmail(r.getString("email"));
                profile.setAddress(r.getString("address"));
                profile.setCity(r.getString("city"));
                profile.setState(r.getString("state"));
                profile.setZip(r.getString("zip"));
            }
            else
            {
                //if no record exists, return HTTP 404
                throw new ResponseStatusException(HttpStatus.NOT_FOUND);
            }
        }
        catch (SQLException e)
        {
            System.out.println("Error getting profile" + e);
        }

        //return the populated profile object
        return profile;
    }
}