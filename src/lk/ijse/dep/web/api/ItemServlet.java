package lk.ijse.dep.web.api;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import lk.ijse.dep.web.model.Item;
import org.apache.commons.dbcp2.BasicDataSource;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

@WebServlet(name = "ItemServlet", urlPatterns = "/items")
public class ItemServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        //
        BasicDataSource cp = (BasicDataSource) getServletContext().getAttribute("cp");

        resp.addHeader("Access-Control-Allow-Origin", "http://localhost:3000");
        resp.setContentType("application/json");
        try (PrintWriter out = resp.getWriter()) {


            try {
                //Class.forName("com.mysql.jdbc.Driver"); //no need this connection
                Connection connection = cp.getConnection();
                //Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/dep6", "root", "1234");
                Statement stm = connection.createStatement();
                ResultSet rst = stm.executeQuery("SELECT * FROM Item");

                List<Item> itemList = new ArrayList<>();

                while (rst.next()) {
                    String code = rst.getString(1);
                    String description = rst.getString(2);
                    double unitPrice = rst.getDouble(3);
                    int qty = rst.getInt(4);
                    itemList.add(new Item(code, description, qty, unitPrice));
                }
                Jsonb jsonb = JsonbBuilder.create();
                out.println(jsonb.toJson(itemList));
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }

        }
    }
}
