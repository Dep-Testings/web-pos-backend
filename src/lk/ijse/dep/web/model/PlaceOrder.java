package lk.ijse.dep.web.model;

/**
 * @author : Lucky Prabath <lucky.prabath94@gmail.com>
 * @since : 2020-12-29
 **/
public class PlaceOrder {
    private String orderId;
    private String itemCode;
    private int qty;
    private double unitPrice;

    public PlaceOrder() {
    }

    public PlaceOrder(String orderId, String itemCode, int qty, double unitPrice) {
        this.orderId = orderId;
        this.itemCode = itemCode;
        this.qty = qty;
        this.unitPrice = unitPrice;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getItemCode() {
        return itemCode;
    }

    public void setItemCode(String itemCode) {
        this.itemCode = itemCode;
    }

    public int getQty() {
        return qty;
    }

    public void setQty(int qty) {
        this.qty = qty;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
    }

    @Override
    public String toString() {
        return "PlaceOrder{" +
                "orderId='" + orderId + '\'' +
                ", itemCode='" + itemCode + '\'' +
                ", qty=" + qty +
                ", unitPrice=" + unitPrice +
                '}';
    }
}
