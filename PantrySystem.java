import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Core Community Pantry Management System.
 * IDs are normalised to uppercase. Only pending requests are processed.
 */
public class PantrySystem {
    private final Map<String, PantryItem> items = new HashMap<>();
    private final List<PantryRequest> requests = new ArrayList<>();
    private int nextArrivalNumber = 1;

    public static class ProcessingResult {
        private final List<String> lines;
        private final int stockChecks;
        ProcessingResult(List<String> lines, int stockChecks) {
            this.lines = lines;
            this.stockChecks = stockChecks;
        }
        public List<String> getLines() { return new ArrayList<>(lines); }
        public int getStockChecks() { return stockChecks; }
    }

    public static String normalizeId(String id) {
        return id == null ? "" : id.trim().toUpperCase();
    }

    public static boolean validItemId(String id) {
        return normalizeId(id).matches("[A-Z][0-9]{3}");
    }

    public static boolean validRequestId(String id) {
        return normalizeId(id).matches("Q[0-9]{3}");
    }

    public void registerItem(String idInput, String nameInput, int stock) {
        String id = normalizeId(idInput);
        String name = nameInput == null ? "" : nameInput.trim();

        if (!validItemId(id)) throw new IllegalArgumentException("Item ID must be one letter followed by three digits.");
        if (items.containsKey(id)) throw new IllegalArgumentException("Duplicate item ID.");
        if (name.length() < 1 || name.length() > 40) throw new IllegalArgumentException("Item name must be 1 to 40 characters.");
        if (stock < 0 || stock > 1000) throw new IllegalArgumentException("Initial stock must be between 0 and 1000.");

        items.put(id, new PantryItem(id, name, stock));
    }

    public PantryItem findItem(String idInput) {
        return items.get(normalizeId(idInput));
    }

    public void registerRequest(String requestIdInput, String itemIdInput,
                                int quantity, int priority) {
        String requestId = normalizeId(requestIdInput);
        String itemId = normalizeId(itemIdInput);

        if (!validRequestId(requestId)) throw new IllegalArgumentException("Request ID must be Q followed by three digits.");
        if (findRequest(requestId) != null) throw new IllegalArgumentException("Request ID has already been used.");
        if (!items.containsKey(itemId)) throw new IllegalArgumentException("Unknown item ID.");
        if (quantity < 1 || quantity > 1000) throw new IllegalArgumentException("Quantity must be between 1 and 1000.");
        if (priority < 1 || priority > 3) throw new IllegalArgumentException("Priority must be 1 (HIGH), 2 (NORMAL) or 3 (LOW).");

        PantryRequest request = new PantryRequest(
            requestId, itemId, quantity, nextArrivalNumber++, priority, PantryRequest.PENDING);
        requests.add(request);
    }

    private PantryRequest findRequest(String id) {
        for (PantryRequest request : requests) {
            if (request.getRequestId().equals(id)) return request;
        }
        return null;
    }

    public List<PantryItem> getItems() {
        List<PantryItem> result = new ArrayList<>();
        for (PantryItem item : items.values()) result.add(item.copy());
        result.sort(Comparator.comparing(PantryItem::getId));
        return result;
    }

    public List<PantryRequest> getRequests() {
        List<PantryRequest> result = new ArrayList<>();
        for (PantryRequest request : requests) result.add(request.copy());
        result.sort(Comparator.comparingInt(PantryRequest::getArrivalNumber));
        return result;
    }

    public List<PantryItem> getLowStockItems(int threshold) {
        if (threshold < 0) throw new IllegalArgumentException("Threshold cannot be negative.");
        List<PantryItem> result = new ArrayList<>();
        for (PantryItem item : items.values()) {
            if (item.getStock() <= threshold) result.add(item.copy());
        }
        result.sort(Comparator.comparing(PantryItem::getId));
        return result;
    }

    public int countStatus(String status) {
        int count = 0;
        for (PantryRequest request : requests) {
            if (request.getStatus().equals(status)) count++;
        }
        return count;
    }

    public ProcessingResult processPending(boolean priorityMode) {
        List<PantryRequest> pending = new ArrayList<>();
        for (PantryRequest request : requests) {
            if (request.getStatus().equals(PantryRequest.PENDING)) pending.add(request);
        }

        if (priorityMode) {
            mergeSort(pending);
        } else {
            pending.sort(Comparator.comparingInt(PantryRequest::getArrivalNumber));
        }

        List<String> output = new ArrayList<>();
        int checks = 0;
        for (PantryRequest request : pending) {
            PantryItem item = items.get(request.getItemId());
            checks++;
            int before = item.getStock();
            if (before >= request.getQuantity()) {
                item.reduceStock(request.getQuantity());
                request.setStatus(PantryRequest.FULFILLED);
            } else {
                request.setStatus(PantryRequest.REJECTED);
            }
            output.add(request.getRequestId() + " | " + request.getStatus()
                + " | " + request.getItemId() + " stock: " + before + " -> " + item.getStock());
        }
        return new ProcessingResult(output, checks);
    }

    /** Recursive merge sort by priority ascending, then arrival number ascending. */
    public static void mergeSort(List<PantryRequest> list) {
        if (list == null || list.size() <= 1) return;
        int middle = list.size() / 2;
        List<PantryRequest> left = new ArrayList<>(list.subList(0, middle));
        List<PantryRequest> right = new ArrayList<>(list.subList(middle, list.size()));
        mergeSort(left);
        mergeSort(right);
        mergeInto(list, left, right);
    }

    private static boolean comesFirst(PantryRequest a, PantryRequest b) {
        if (a.getPriority() != b.getPriority()) return a.getPriority() < b.getPriority();
        return a.getArrivalNumber() <= b.getArrivalNumber();
    }

    private static void mergeInto(List<PantryRequest> target,
                                  List<PantryRequest> left,
                                  List<PantryRequest> right) {
        int i = 0, j = 0, k = 0;
        while (i < left.size() && j < right.size()) {
            if (comesFirst(left.get(i), right.get(j))) target.set(k++, left.get(i++));
            else target.set(k++, right.get(j++));
        }
        while (i < left.size()) target.set(k++, left.get(i++));
        while (j < right.size()) target.set(k++, right.get(j++));
    }

    /**
     * Snapshot format:
     * ITEM,id,base64(name),stock
     * REQUEST,id,itemId,quantity,arrival,priority,status
     * Loading validates all rows into temporary structures before replacing live data.
     */
    public void save(Path file) throws IOException {
        List<String> lines = new ArrayList<>();
        lines.add("PANTRY_SNAPSHOT_V1");
        for (PantryItem item : getItems()) {
            String encodedName = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(item.getName().getBytes(StandardCharsets.UTF_8));
            lines.add("ITEM," + item.getId() + "," + encodedName + "," + item.getStock());
        }
        for (PantryRequest request : getRequests()) {
            lines.add("REQUEST," + request.getRequestId() + "," + request.getItemId()
                + "," + request.getQuantity() + "," + request.getArrivalNumber()
                + "," + request.getPriority() + "," + request.getStatus());
        }
        Path absolute = file.toAbsolutePath();
        Path parent = absolute.getParent();
        if (parent != null) Files.createDirectories(parent);
        Path temp = absolute.resolveSibling(absolute.getFileName().toString() + ".tmp");
        Files.write(temp, lines, StandardCharsets.UTF_8);
        try {
            Files.move(temp, absolute, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException ex) {
            Files.move(temp, absolute, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    public void load(Path file) throws IOException {
        List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        if (lines.isEmpty() || !lines.get(0).equals("PANTRY_SNAPSHOT_V1")) {
            throw new IOException("Invalid snapshot header.");
        }

        Map<String, PantryItem> loadedItems = new HashMap<>();
        List<PantryRequest> loadedRequests = new ArrayList<>();
        Set<String> requestIds = new HashSet<>();
        Set<Integer> arrivals = new HashSet<>();
        int maxArrival = 0;

        try {
            for (int lineNo = 1; lineNo < lines.size(); lineNo++) {
                String line = lines.get(lineNo);
                if (line.isBlank()) throw new IllegalArgumentException("Blank record at line " + (lineNo + 1));
                String[] parts = line.split(",", -1);

                if (parts[0].equals("ITEM")) {
                    if (parts.length != 4) throw new IllegalArgumentException("Invalid ITEM record.");
                    String id = normalizeId(parts[1]);
                    String name = new String(Base64.getUrlDecoder().decode(parts[2]), StandardCharsets.UTF_8);
                    int stock = Integer.parseInt(parts[3]);
                    if (!validItemId(id) || !id.equals(parts[1]) || loadedItems.containsKey(id)
                        || name.trim().isEmpty() || name.trim().length() > 40 || !name.equals(name.trim())
                        || stock < 0 || stock > 1000) {
                        throw new IllegalArgumentException("Invalid or duplicate item record.");
                    }
                    loadedItems.put(id, new PantryItem(id, name, stock));
                } else if (parts[0].equals("REQUEST")) {
                    if (parts.length != 7) throw new IllegalArgumentException("Invalid REQUEST record.");
                    String id = normalizeId(parts[1]);
                    String itemId = normalizeId(parts[2]);
                    int quantity = Integer.parseInt(parts[3]);
                    int arrival = Integer.parseInt(parts[4]);
                    int priority = Integer.parseInt(parts[5]);
                    String status = parts[6];
                    if (!validRequestId(id) || !id.equals(parts[1]) || !requestIds.add(id)
                        || !loadedItems.containsKey(itemId) || !itemId.equals(parts[2])
                        || quantity < 1 || quantity > 1000 || arrival < 1
                        || !arrivals.add(arrival) || priority < 1 || priority > 3
                        || !(status.equals(PantryRequest.PENDING)
                          || status.equals(PantryRequest.FULFILLED)
                          || status.equals(PantryRequest.REJECTED))) {
                        throw new IllegalArgumentException("Invalid or duplicate request record.");
                    }
                    loadedRequests.add(new PantryRequest(id, itemId, quantity, arrival, priority, status));
                    maxArrival = Math.max(maxArrival, arrival);
                } else {
                    throw new IllegalArgumentException("Unknown record type.");
                }
            }
        } catch (RuntimeException ex) {
            throw new IOException("Snapshot rejected: " + ex.getMessage(), ex);
        }

        // Commit only after every record has passed validation.
        items.clear();
        items.putAll(loadedItems);
        requests.clear();
        requests.addAll(loadedRequests);
        nextArrivalNumber = maxArrival + 1;
    }
}
