package lk.ijse.dep.web.api;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbException;
import lk.ijse.dep.web.model.Customer;
import lk.ijse.dep.web.model.Item;
import org.apache.commons.dbcp2.BasicDataSource;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@WebServlet(name = "ItemServlet", urlPatterns = "/items")
public class ItemServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String code = req.getParameter("code");
        BasicDataSource cp = (BasicDataSource) getServletContext().getAttribute("cp");
        resp.setContentType("application/json");
        try ( Connection connection = cp.getConnection()) {
            PrintWriter out = resp.getWriter();
            PreparedStatement pstm = connection.prepareStatement("SELECT * FROM Item" + ((code != null) ? " WHERE code=?" : ""));
            if (code != null) {
                pstm.setObject(1, code);
            }
            ResultSet rst = pstm.executeQuery();
            List<Item> itemList = new ArrayList<>();
            while (rst.next()) {
                code = rst.getString(1);
                String description = rst.getString(2);
                double unitPrice = rst.getDouble(3);
                int qty = rst.getInt(4);
                itemList.add(new Item(code, description, qty,unitPrice));
            }

            if (code != null && itemList.isEmpty()) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            } else {
                Jsonb jsonb = JsonbBuilder.create();
                out.println(jsonb.toJson(itemList));
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }

    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        //get connection from connection pool and save this Customer Object
        BasicDataSource cp =(BasicDataSource ) getServletContext().getAttribute("cp");

        try {
            Jsonb jsonb = JsonbBuilder.create(); //entry point
            Item item = jsonb.fromJson(req.getReader(), Item.class);
            Connection connection = cp.getConnection();
            /*validate*/
            if(item.getCode() == null || item.getDescription() == null || item.getQty() == 0 || item.getUnitPrice() == 0){
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            if (!item.getCode().matches("I\\d{3}") || item.getDescription().trim().isEmpty()){
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            PreparedStatement pstm = connection.prepareStatement("INSERT INTO Item VALUES (?,?,?,?)");
            pstm.setString(1, item.getCode());
            pstm.setString(2, item.getDescription());
            pstm.setDouble(3, item.getUnitPrice());
            pstm.setInt(4, item.getQty());

            if(pstm.executeUpdate()>0){
                resp.setStatus(HttpServletResponse.SC_CREATED);
            }else {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }

        } catch (SQLIntegrityConstraintViolationException ex) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
        } catch (SQLException throwables){
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            throwables.printStackTrace();
        } catch (JsonbException exp){
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }

    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String code = req.getParameter("code");
        if (code == null || !code.matches("I\\d{3}")){
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        BasicDataSource cp = (BasicDataSource) getServletContext().getAttribute("cp");
        try (Connection connection = cp.getConnection()){
            Jsonb jsonb = JsonbBuilder.create();  //entry point
            Item item = jsonb.fromJson(req.getReader(), Item.class);

            /*validate*/
            if( item.getCode() != null || item.getDescription() == null || item.getQty() == 0 || item.getUnitPrice() == 0 ){
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            if ( item.getDescription().trim().isEmpty() ){
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            PreparedStatement pstm = connection.prepareStatement("SELECT * FROM Item WHERE code=?");
            pstm.setObject(1, code);
            if(pstm.executeQuery().next()) {
                pstm = connection.prepareStatement("UPDATE Item SET description=?, qtyOnHand=?, unitPrice=? WHERE code=?");
                pstm.setObject(1, item.getDescription());
                pstm.setObject(2, item.getQty());
                pstm.setObject(3, item.getUnitPrice());
                pstm.setObject(4, code);
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

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String code = req.getParameter("code");
        if (code == null || !code.matches("I\\d{3}")){
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        BasicDataSource cp = (BasicDataSource) getServletContext().getAttribute("cp");
        try (Connection connection = cp.getConnection()){
            PreparedStatement pstm = connection.prepareStatement("SELECT  * FROM Item WHERE code=?");
            pstm.setObject(1, code);
            if(pstm.executeQuery().next()) {
                pstm = connection.prepareStatement("DELETE FROM Item WHERE code=?");
                pstm.setObject(1, code);
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
}
