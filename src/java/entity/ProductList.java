/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package entity;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;

/**
 *
 * @author c0653602
 */
public class ProductList {
     private List<Product> productList;

    public ProductList() {
        productList = new ArrayList<>();
        try (Connection conn = getConnection()) {
            String query = "SELECT * FROM product";
            PreparedStatement pstmt = conn.prepareStatement(query);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Product p = new Product(rs.getInt("productID"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getInt("quantity"));
                productList.add(p);
            }
        } catch (SQLException ex) {
            Logger.getLogger(ProductList.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public JsonArray toJSON() {
        JsonArrayBuilder json = Json.createArrayBuilder();
        for (Product p : productList) {
            json.add(p.toJSON());
        }
        return json.build();
    }

    public Product get(int productID) {
        Product result = null;
        for (Product p : productList) {
            if (p.getProductID() == productID) {
                result = p;
            }
        }
        return result;
    }

    public void set(int productID, Product p) {
        int result = doUpdate(
                "update product SET name = ?, description = ?, quantity = ? where productID = ?",
                p.getName(),
                p.getDescription(),
                String.valueOf(p.getQuantity()),
                String.valueOf(productID));
        if (result > 0) {
            Product original = get(productID);
            original.setName(p.getName());
            original.setDescription(p.getDescription());
            original.setQuantity(p.getQuantity());
        }

    }

    public void add(Product p) throws Exception {
        int result = doUpdate(
                "INSERT into product (productID, name, description, quantity) values (?, ?, ?, ?)",
                String.valueOf(p.getProductID()),
                p.getName(),
                p.getDescription(),
                String.valueOf(p.getQuantity()));
        if (result > 0) {
            productList.add(p);
        } else {
            throw new Exception("Error Inserting");
        }
    }
    


    public void remove(Product p) throws Exception {
        remove(p.getProductID());
    }

    public void remove(int productID) throws Exception {
        int result = doUpdate("DELETE from product where productID = ?",
                String.valueOf(productID));
        if (result > 0) {
            Product original = get(productID);
            productList.remove(original);
        } else {
            throw new Exception("Delete failed");
        }

    }

    private Connection getConnection() throws SQLException {
        Connection conn = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            String jdbc = "jdbc:mysql://localhost/fastenlt";
            conn = (Connection) DriverManager.getConnection(jdbc, "root", "");
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ProductList.class.getName()).log(Level.SEVERE, null, ex);
        }
        return conn;
    }

//    private String getResults(String query, String... params) throws SQLException {
//        StringBuilder sb = new StringBuilder();
//        try (Connection conn = getConnection()) {
//            PreparedStatement pstmt = conn.prepareStatement(query);
//            for (int i = 1; i <= params.length; i++) {
//                pstmt.setString(i, params[i - 1]);
//            }
//            ResultSet rs = pstmt.executeQuery();
//            while (rs.next()) {
//                sb.append(String.format("%s\t%s\t%s\n", rs.getInt("id"), rs.getString("name"),
//                        rs.getString("description"), rs.getInt("quantity")));
//            }
//        } catch (SQLException ex) {
//            Logger.getLogger(ProductList.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        return sb.toString();
//    }
    private int doUpdate(String query, String... params) {
        int numChanges = 0;
        try (Connection conn = getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(query);
            for (int i = 1; i <= params.length; i++) {
                pstmt.setString(i, params[i - 1]);
            }
            numChanges = pstmt.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(ProductList.class.getName()).log(Level.SEVERE, null, ex);
        }
        return numChanges;
    }

    
    
}
