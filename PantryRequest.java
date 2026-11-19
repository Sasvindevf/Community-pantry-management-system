public class PantryRequest {
    public static final String PENDING = "PENDING";
    public static final String FULFILLED = "FULFILLED";
    public static final String REJECTED = "REJECTED";

    private final String requestId;
    private final String itemId;
    private final int quantity;
    private final int arrivalNumber;
    private final int priority;
    private String status;

    public PantryRequest(String requestId, String itemId, int quantity,
                         int arrivalNumber, int priority, String status) {
        this.requestId = requestId;
        this.itemId = itemId;
        this.quantity = quantity;
        this.arrivalNumber = arrivalNumber;
        this.priority = priority;
        this.status = status;
    }

    public String getRequestId() { return requestId; }
    public String getItemId() { return itemId; }
    public int getQuantity() { return quantity; }
    public int getArrivalNumber() { return arrivalNumber; }
    public int getPriority() { return priority; }
    public String getStatus() { return status; }

    void setStatus(String status) { this.status = status; }

    PantryRequest copy() {
        return new PantryRequest(requestId, itemId, quantity, arrivalNumber, priority, status);
    }
}
