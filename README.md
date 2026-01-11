# Inventory Management System  

A modern Java application for managing product inventory with an easy-to-use interface.  

## ✨ Features  
- 📦 **Product Management** – Add, edit, delete, and search products  
- 🗂️ **Category Organization** – Organize products into categories  
- 📊 **Dashboard** – View stats and stock alerts  
- 📝 **Activity Logs** – Track all user actions  
- 🔐 **Secure Login** – Password-protected access  

## 🛠️ Setup Guide  

### 1. Install Requirements  
- **Java 8+** – [Download Java](https://www.java.com)  
- **MySQL** – [Download MySQL](https://dev.mysql.com/downloads/)  

### 2. Setup Database  
Open MySQL and run:  
```sql
CREATE DATABASE inventory_db;
USE inventory_db;

-- Create tables
CREATE TABLE categories (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE products (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(200) NOT NULL,
    category_id INT,
    quantity INT DEFAULT 0,
    price DECIMAL(10,2) DEFAULT 0.00,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE activity_logs (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user VARCHAR(50) NOT NULL,
    action VARCHAR(100) NOT NULL,
    details TEXT,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### 3. Configure Connection  
Edit the `InventoryManagementSystem.java` file:  
```java
private static final String URL = "jdbc:mysql://localhost:3306/inventory_db";
private static final String USER = "root";      // Your MySQL username
private static final String PASS = "password";  // Your MySQL password
```

### 4. Add MySQL Driver  
Download `mysql-connector-java-8.0.33.jar` from:  
🔗 https://dev.mysql.com/downloads/connector/j/  

Add to project:  
- **Eclipse**: Right-click project → Build Path → Add External JARs  
- **IntelliJ**: File → Project Structure → Libraries → + → Java  
- **Command Line**: `java -cp "mysql-connector-java-8.0.33.jar;." InventoryManagementSystem`  

## 🚀 How to Run  
1. **Compile**: `javac InventoryManagementSystem.java`  
2. **Run**: `java InventoryManagementSystem`  
3. **Login**:  
   - Username: `kabeer`  
   - Password: `kabeershahani`  

## 📁 Project Structure  
```
📂 InventorySystem
├── 📄 InventoryManagementSystem.java
├── 📄 mysql-connector-java-8.0.33.jar
└── 📄 README.md
```

## 🆘 Need Help?  
**Contact**: Kabeer Shahani  
**Email**: 📧 kabeershahani747@gmail.com  
