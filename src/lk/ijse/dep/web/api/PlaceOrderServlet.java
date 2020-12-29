package lk.ijse.dep.web.api;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbException;
import lk.ijse.dep.web.model.Customer;
import lk.ijse.dep.web.model.PlaceOrder;
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

@WebServlet(name = "PlaceOrderServlet", urlPatterns = "/placeOrder")
public class PlaceOrderServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        //get connection from connection pool and save this Customer Object
        BasicDataSource cp =(BasicDataSource ) getServletContext().getAttribute("cp");

        try {
            Jsonb jsonb = JsonbBuilder.create(); //entry point
            PlaceOrder placeOrder = jsonb.fromJson(req.getReader(), PlaceOrder.class);
            Connection connection = cp.getConnection();
            /*validate*/
            if(placeOrder.getOrderId() == null || placeOrder.getItemCode() == null || placeOrder.getQty() <= 0 ||
                    placeOrder.getUnitPrice() <= 0){
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            if (!placeOrder.getOrderId().matches("OD\\d{3}") || !placeOrder.getItemCode().matches("I\\d{3}") ){
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            PreparedStatement pstm = connection.prepareStatement("INSERT INTO OrderDetail VALUES (?,?,?,?)");
            pstm.setString(1, placeOrder.getOrderId());
            pstm.setString(2, placeOrder.getItemCode());
            pstm.setInt(3, placeOrder.getQty());
            pstm.setDouble(4, placeOrder.getUnitPrice());

            if(pstm.executeUpdate()>0){
                resp.setStatus(HttpServletResponse.SC_CREATED);
            }else {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }

        } catch (SQLIntegrityConstraintViolationException ex) {
            /*todo: find the reason for the error coming in pstm execution*/
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
        } catch (SQLException throwables){
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            throwables.printStackTrace();
        } catch (JsonbException exp){
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }

    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

    }
}
