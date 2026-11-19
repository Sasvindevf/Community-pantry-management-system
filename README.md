# Community-pantry-management-system

**Student Name:** [Your Name]  
**Student ID:** [Your Student ID]  
**Course:** [Your Course Name]  
**Programming Language:** Java

## 1. Project Description

The Community Pantry Management System is a Java-based application designed to manage pantry items and community requests.

The system allows users to register pantry items, check available stock, submit requests and process requests using different scheduling methods. It also helps users monitor low-stock items and save or load pantry records.

## 2. Features

- **Register Item:** Add new pantry items with an item ID, name and initial stock.
- **Find Item:** Search for pantry items using their item ID.
- **Register Request:** Create requests for available pantry items.
- **FIFO Processing:** Process requests based on their arrival order (First In, First Out).
- **Priority Processing:** Process requests according to priority level (High, Normal and Low).
- **Request History:** View all registered requests and their current status.
- **Low-Stock Report:** Display items with stock levels below or equal to a selected threshold.
- **Save Snapshot:** Save pantry items and request records into a file.
- **Load Snapshot:** Load previously saved pantry records.
- **Summary Report:** Display the total number of fulfilled, rejected and pending requests.

## 3. Source Code Files

The project contains four Java source files:

| File Name | Description |
|---|---|
| Main.java | Contains the main menu and user interaction. |
| PantryItem.java | Stores pantry item details and manages stock. |
| PantryRequest.java | Stores request information, priority and status. |
| PantrySystem.java | Handles item management, request processing, sorting and file storage. |

## 4. How to Run

1. Install Java JDK (Java 17 or later recommended).
2. Open the project folder using Eclipse, IntelliJ IDEA or another Java IDE.
3. Make sure all four Java files are in the same source folder.
4. Open `Main.java`.
5. Run the `Main` class.
6. Select an option from the menu to use the system.

### Running Through Command Prompt

Compile the Java files:

```bash
javac Main.java PantryItem.java PantryRequest.java PantrySystem.java
```

Run the program:

```bash
java Main
```

## 5. Request Priority Levels

The system supports three priority levels:

| Priority | Level |
|---|---|
| 1 | High |
| 2 | Normal |
| 3 | Low |

In priority processing, high-priority requests are processed first. Requests with the same priority are processed according to their arrival order.

## 6. Data Storage

The system uses `pantry-data.csv` to save and load pantry records.

Users can select:
- Option 8 to save a snapshot.
- Option 9 to load a previously saved snapshot.

The saved data includes pantry items, stock quantities, request details, arrival numbers, priority levels and request statuses.

## 7. Request Status

Each request has one of the following statuses:

- **PENDING:** The request is waiting to be processed.
- **FULFILLED:** The request has been successfully processed.
- **REJECTED:** The request cannot be fulfilled because there is insufficient stock.

## 8. Project Objective

The main objective of this project is to provide a simple and organised way to manage community pantry inventory and requests. It also demonstrates the use of Java programming concepts such as classes, objects, ArrayList, HashMap, sorting algorithms and file handling.

---

**End of README**
