import java.nio.file.Path;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static final Scanner INPUT = new Scanner(System.in);
    private static final PantrySystem SYSTEM = new PantrySystem();
    private static final Path SAVE_FILE = Path.of("pantry-data.csv");

    public static void main(String[] args) {
        boolean running = true;
        while (running) {
            printMenu();
            int choice = readInt("Choice: ");
            switch (choice) {
                case 1 -> registerItem();
                case 2 -> findItem();
                case 3 -> registerRequest();
                case 4 -> processRequests(false);
                case 5 -> processRequests(true);
                case 6 -> displayHistory();
                case 7 -> lowStockReport();
                case 8 -> saveData();
                case 9 -> loadData();
                case 10 -> displaySummary();
                case 11 -> {
                    running = false;
                    System.out.println("Goodbye.");
                }
                default -> System.out.println("Invalid menu choice.");
            }
        }
    }

    private static void printMenu() {
        System.out.println("\nCOMMUNITY PANTRY MANAGEMENT SYSTEM");
        System.out.println("1. Register item");
        System.out.println("2. Find item");
        System.out.println("3. Register request");
        System.out.println("4. Process pending requests (FIFO)");
        System.out.println("5. Process pending requests (PRIORITY)");
        System.out.println("6. Display request history");
        System.out.println("7. Low-stock report");
        System.out.println("8. Save snapshot");
        System.out.println("9. Load snapshot");
        System.out.println("10. Display summary");
        System.out.println("11. Exit");
    }

    private static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = INPUT.nextLine().trim();
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException ex) {
                System.out.println("Please enter a whole number.");
            }
        }
    }

    private static void registerItem() {
        System.out.print("Item ID (e.g. R001): ");
        String id = INPUT.nextLine();
        System.out.print("Item name: ");
        String name = INPUT.nextLine();
        int stock = readInt("Initial stock (0-1000): ");
        try {
            SYSTEM.registerItem(id, name, stock);
            System.out.println("Item registered.");
        } catch (IllegalArgumentException ex) {
            System.out.println("Registration failed: " + ex.getMessage());
        }
    }

    private static void findItem() {
        System.out.print("Item ID: ");
        PantryItem item = SYSTEM.findItem(INPUT.nextLine());
        if (item == null) System.out.println("Item not found.");
        else System.out.println(item.getId() + " | " + item.getName() + " | Stock: " + item.getStock());
    }

    private static void registerRequest() {
        System.out.print("Request ID (e.g. Q001): ");
        String requestId = INPUT.nextLine();
        System.out.print("Item ID: ");
        String itemId = INPUT.nextLine();
        int quantity = readInt("Quantity (1-1000): ");
        int priority = readInt("Priority (1 HIGH, 2 NORMAL, 3 LOW): ");
        try {
            SYSTEM.registerRequest(requestId, itemId, quantity, priority);
            System.out.println("Request registered as PENDING.");
        } catch (IllegalArgumentException ex) {
            System.out.println("Registration failed: " + ex.getMessage());
        }
    }

    private static void processRequests(boolean priorityMode) {
        PantrySystem.ProcessingResult result = SYSTEM.processPending(priorityMode);
        System.out.println(priorityMode ? "PRIORITY processing:" : "FIFO processing:");
        if (result.getLines().isEmpty()) System.out.println("No pending requests.");
        for (String line : result.getLines()) System.out.println(line);
        System.out.println("Stock checks: " + result.getStockChecks());
    }

    private static void displayHistory() {
        List<PantryRequest> requests = SYSTEM.getRequests();
        if (requests.isEmpty()) {
            System.out.println("No requests recorded.");
            return;
        }
        for (PantryRequest request : requests) {
            System.out.println(request.getRequestId() + " | Item " + request.getItemId()
                + " | Qty " + request.getQuantity() + " | Arrival " + request.getArrivalNumber()
                + " | Priority " + request.getPriority() + " | " + request.getStatus());
        }
    }

    private static void lowStockReport() {
        int threshold = readInt("Enter non-negative stock threshold: ");
        try {
            List<PantryItem> low = SYSTEM.getLowStockItems(threshold);
            if (low.isEmpty()) System.out.println("No low-stock items.");
            for (PantryItem item : low) {
                System.out.println(item.getId() + " | " + item.getName() + " | Stock: " + item.getStock());
            }
        } catch (IllegalArgumentException ex) {
            System.out.println("Invalid threshold: " + ex.getMessage());
        }
    }

    private static void saveData() {
        try {
            SYSTEM.save(SAVE_FILE);
            System.out.println("Saved to " + SAVE_FILE.toAbsolutePath());
        } catch (Exception ex) {
            System.out.println("Save failed: " + ex.getMessage());
        }
    }

    private static void loadData() {
        try {
            SYSTEM.load(SAVE_FILE);
            System.out.println("Snapshot loaded.");
        } catch (Exception ex) {
            System.out.println("Load failed: " + ex.getMessage());
        }
    }

    private static void displaySummary() {
        System.out.println("FULFILLED: " + SYSTEM.countStatus(PantryRequest.FULFILLED));
        System.out.println("REJECTED: " + SYSTEM.countStatus(PantryRequest.REJECTED));
        System.out.println("PENDING: " + SYSTEM.countStatus(PantryRequest.PENDING));
    }
}
