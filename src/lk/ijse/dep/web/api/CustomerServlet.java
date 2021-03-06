package lk.ijse.dep.web.api;

import jakarta.json.Json;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbException;
import jakarta.json.stream.JsonParsingException;
import lk.ijse.dep.web.model.Customer;
import org.apache.commons.dbcp2.BasicDataSource;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@WebServlet(name = "CustomerServlet", urlPatterns = "/customers")
public class CustomerServlet extends HttpServlet {

   /* @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.addHeader("Access-Control-Allow-Origin", "http://localhost:3000");
        resp.addHeader("Access-Control-Allow-Headers", "Content-Type");
        resp.addHeader("Access-Control-Allow-Methods", "GET PUT POST DELETE");

    }*/

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        /*CORS Policy*/
//        resp.addHeader("Access-Control-Allow-Origin", "http://localhost:3000");

        resp.setContentType("application/json");
        //get connection from connection pool and save this Customer Object
        BasicDataSource cp =(BasicDataSource ) getServletContext().getAttribute("cp");

        try {
            Jsonb jsonb = JsonbBuilder.create(); //entry point
            Customer customer = jsonb.fromJson(req.getReader(), Customer.class);
            Connection connection = cp.getConnection();
            /*validate*/
            if(customer.getId() == null || customer.getName() == null || customer.getAddress() == null){
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            if (!customer.getId().matches("C\\d{3}") || customer.getName().trim().isEmpty() || customer.getAddress().trim().isEmpty()){
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            PreparedStatement pstm = connection.prepareStatement("INSERT INTO Customer VALUES (?,?,?,?)");
            pstm.setString(1, customer.getId());
            pstm.setString(2, customer.getName());
            pstm.setString(3, customer.getAddress());
            pstm.setDouble(4, 0);

            if(pstm.executeUpdate()>0){
                resp.setStatus(HttpServletResponse.SC_CREATED);
            }else {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                System.out.println("check 2");
            }

        } catch (SQLIntegrityConstraintViolationException ex) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
        } catch (SQLException throwables){
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            System.out.println("check 1");
            throwables.printStackTrace();
        } catch (JsonbException exp){
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }

    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        /*CORS Policy*/
//        resp.addHeader("Access-Control-Allow-Origin", "http://localhost:3000");

        String id = req.getParameter("id");
        BasicDataSource cp = (BasicDataSource) getServletContext().getAttribute("cp");
        resp.setContentType("application/json");
        try ( Connection connection = cp.getConnection()) {
                PrintWriter out = resp.getWriter();
                PreparedStatement pstm = connection.prepareStatement("SELECT * FROM Customer" + ((id != null) ? " WHERE id=?" : ""));
                if (id != null) {
                    pstm.setObject(1, id);
                }
                ResultSet rst = pstm.executeQuery();
                List<Customer> customerList = new ArrayList<>();
                while (rst.next()) {
                    id = rst.getString(1);
                    String name = rst.getString(2);
                    String address = rst.getString(3);
                    //double salary = rst.getDouble(4);
                    customerList.add(new Customer(id, name, address));
                }

                if (id != null && customerList.isEmpty()) {
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                } else {
                    Jsonb jsonb = JsonbBuilder.create();
                    out.println(jsonb.toJson(customerList));
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }


    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        /*CORS Policy*/
//        resp.addHeader("Access-Control-Allow-Origin", "http://localhost:3000");

        String id = req.getParameter("id");
        if (id == null || !id.matches("C\\d{3}")){
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        BasicDataSource cp = (BasicDataSource) getServletContext().getAttribute("cp");
        try (Connection connection = cp.getConnection()){
            PreparedStatement pstm = connection.prepareStatement("SELECT  * FROM Customer WHERE id=?");
            pstm.setObject(1, id);
            if(pstm.executeQuery().next()) {
                pstm = connection.prepareStatement("DELETE FROM Customer WHERE id=?");
                pstm.setObject(1, id);
                boolean success = pstm.executeUpdate() > 0;
                if (success) {
                    resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
                } else {
                    resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                }
            }else {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (SQLIntegrityConstraintViolationException ex){
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
        } catch (SQLException throwables) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            throwables.printStackTrace();
        }


    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        /*CORS Policy*/
//        resp.addHeader("Access-Control-Allow-Origin", "http://localhost:3000");

        String id = req.getParameter("id");
        if (id == null || !id.matches("C\\d{3}")){
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        BasicDataSource cp = (BasicDataSource) getServletContext().getAttribute("cp");
        try (Connection connection = cp.getConnection()){
            Jsonb jsonb = JsonbBuilder.create();  //entry point
            Customer customer = jsonb.fromJson(req.getReader(), Customer.class);

            /*validate*/
            if(customer.getId() != null || customer.getName() == null || customer.getAddress() == null){
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            if ( customer.getName().trim().isEmpty() || customer.getAddress().trim().isEmpty()){
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            PreparedStatement pstm = connection.prepareStatement("SELECT * FROM Customer WHERE id=?");
            pstm.setObject(1, id);
            if(pstm.executeQuery().next()) {
                pstm = connection.prepareStatement("UPDATE Customer SET name=?, address=? WHERE id=?");
                pstm.setObject(1, customer.getName());
                pstm.setObject(2, customer.getAddress());
                pstm.setObject(3, id);
                if (pstm.executeUpdate() > 0) {
                    resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
                } else {
                    resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                }
            }else{
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } catch (JsonbException exp){
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }

    }
}
