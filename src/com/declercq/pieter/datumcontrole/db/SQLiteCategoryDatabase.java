package com.declercq.pieter.datumcontrole.db;

import com.declercq.pieter.datumcontrole.model.entity.Category;
import com.declercq.pieter.datumcontrole.model.exception.DatabaseException;
import com.declercq.pieter.datumcontrole.model.exception.DomainException;
import com.declercq.pieter.datumcontrole.model.exception.ErrorMessages;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

/**
 *
 * @author Pieter Declercq
 * @version 3.0
 */
public class SQLiteCategoryDatabase implements CategoryDatabase {

    private Connection connection;
    private PreparedStatement statement;
    String url;

    public SQLiteCategoryDatabase(String url) throws DatabaseException {
        try {
            Class.forName("org.sqlite.JDBC");
            this.url = url;
        } catch (ClassNotFoundException e) {
            throw new DatabaseException(ErrorMessages.DATABASE_DRIVER_NOT_LOADED, e);
        }
    }

    @Override
    public int size() throws DatabaseException {
        String query = "SELECT COUNT(name) AS size FROM category";
        int size = 0;
        initiateStatement(query);
        try {
            ResultSet r = statement.executeQuery();
            r.next();
            size = r.getInt("size");
        } catch (SQLException ex) {
            throw new DatabaseException(ErrorMessages.DATABASE_FAULT_IN_QUERY, ex);
        } finally {
            closeConnection();
        }
        return size;
    }

    @Override
    public void addCategory(Category category) throws DatabaseException {
        if (category == null) {
            throw new DatabaseException(ErrorMessages.CATEGORY_NULL);
        }
        String query = "INSERT INTO category (name, sublocations, color) VALUES (?, ?, ?)";
        initiateStatement(query);
        try {
            statement.setString(1, category.getName());
            statement.setInt(2, category.getSublocations());
            statement.setString(3, category.getColor());
            statement.execute();
        } catch (SQLException e) {
            throw new DatabaseException(ErrorMessages.CATEGORY_ALREADY_EXISTS, e);
        } finally {
            closeConnection();
        }
    }

    @Override
    public Category getCategory(String name) throws DatabaseException {
        if (name == null) {
            throw new DatabaseException(ErrorMessages.NAME_NULL);
        }
        String query = "SELECT * FROM category WHERE name = ?";
        Category category = null;
        initiateStatement(query);
        try {
            statement.setString(1, name);
            ResultSet result = statement.executeQuery();
            while (result.next()) {
                int sublocations = result.getInt("sublocations");
                String color = result.getString("color");
                category = new Category(name, sublocations, color);
            }
        } catch (SQLException e) {
            throw new DatabaseException(ErrorMessages.DATABASE_FAULT_IN_QUERY, e);
        } catch (DomainException ex) {
            throw new DatabaseException(ex);
        } finally {
            closeConnection();
        }
        if (category == null) {
            throw new DatabaseException(ErrorMessages.CATEGORY_NOT_FOUND);
        }
        return category;
    }

    @Override
    public Collection<Category> getAllCategories() throws DatabaseException {
        String query = "SELECT * FROM category";
        Collection<Category> categories = new ArrayList<>();
        initiateStatement(query);
        try {
            ResultSet result = statement.executeQuery();
            while (result.next()) {
                String name = result.getString("name");
                int sublocations = result.getInt("sublocations");
                String color = result.getString("color");
                Category category = new Category(name, sublocations, color);
                categories.add(category);
            }
        } catch (SQLException e) {
            throw new DatabaseException(ErrorMessages.DATABASE_FAULT_IN_QUERY, e);
        } catch (DomainException ex) {
            throw new DatabaseException(ex);
        } finally {
            closeConnection();
        }
        return categories;
    }

    @Override
    public void updateCategory(Category category) throws DatabaseException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void deleteCategory(String name) throws DatabaseException {
        if (name == null) {
            throw new DatabaseException(ErrorMessages.NAME_NULL);
        }
        String query = "DELETE FROM category WHERE name = ?";
        initiateStatement(query);
        try {
            statement.setString(1, name);
            statement.execute();
        } catch (SQLException e) {
            throw new DatabaseException(ErrorMessages.DATABASE_FAULT_IN_QUERY, e);
        } finally {
            closeConnection();
        }
    }

    private void initiateStatement(String query) throws DatabaseException {
        try {
            connection = DriverManager.getConnection(url);
            statement = connection.prepareStatement(query);
        } catch (SQLException ex) {
            throw new DatabaseException(ErrorMessages.DATABASE_NOT_FOUND, ex);
        }
    }

    private void closeConnection() throws DatabaseException {
        try {
            statement.close();
            connection.close();
        } catch (SQLException e) {
            throw new DatabaseException(ErrorMessages.DATABASE_CLOSSING_CONNECTION, e);
        }
    }

}
