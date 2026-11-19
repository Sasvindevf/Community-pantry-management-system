public class PantryItem {
    private final String id;
    private final String name;
    private int stock;

    public PantryItem(String id, String name, int stock) {
        this.id = id;
        this.name = name;
        this.stock = stock;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public int getStock() { return stock; }

    void reduceStock(int quantity) {
        if (quantity < 0 || quantity > stock) {
            throw new IllegalArgumentException("Invalid stock reduction.");
        }
        stock -= quantity;
    }

    PantryItem copy() {
        return new PantryItem(id, name, stock);
    }
}
