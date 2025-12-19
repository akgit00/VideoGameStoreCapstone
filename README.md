## 🎮 Pixel Palace Video Game Store

🕹 A Spring Boot REST API project for managing video games, customers, orders, and inventory.

---

## 📚 Table of Contents

- [Overview](#overview)
- [Features](#features)
- [How It Works](#how-it-works)
- [Database](#database)
- [API-Requests-Using-Insomnia￼](#api-requests-using-insomnia)
- [Screenshots](#screenshots)
- [My Favorite Part To Work On](#my-favorite-part-to-work-on)￼
- [Author](#author)

⸻

## 📝 Overview

Pixel Palace is a full-stack project where the backend is built using Java Spring Boot, and it connects to a MySQL database to manage a video game store.

The application allows users to interact with store data, including products, categories, carts, and orders, through a REST API. Testing and interaction with the API is performed using Insomnia, allowing you to send HTTP requests (GET, POST, PUT, DELETE) easily.

This project simulates a functioning e-commerce platform — specifically, a video game store.

⸻

## 🚀 Features

🌐 RESTful API Endpoints
	•	Products (retrieve, add, update, delete)
	•	Categories
	•	🛒 Shopping Cart System
	•	Orders

🗄 MySQL Database Integration
	•	Persistent data storage
	•	Tables for products, categories, users, carts, and orders

🔧 Insomnia for API Testing
	•	Send and verify HTTP requests
	•	Debug backend responses

🌱 Spring Boot Architecture
	•	Controllers
	•	Services
	•	DAO Layer
	•	Transaction Management (@Transactional)

⸻

## ⚙ How-It-Works
	•	The Spring Boot API acts as the backend server.
	•	Each database interaction is handled using DAOs.
	•	@Transactional ensures order processing and stock updates are safely executed.
	•	Insomnia is used to:
	•	simulate adding items to a cart
	•	placing orders
	•	viewing inventory

⸻

## 🗄 Database
	•	The project uses MySQL.
	•	Tables store data such as products, categories, user orders, and carts.
	•	MySQL Workbench or terminal can be used to execute the schema.


⸻

## My-Favorite-Part-To-Work-On

My favorite part of working on this project was learning how controllers, services, and DAOs communicate. I especially enjoyed using @Transactional to ensure order checkout logic updated stock and wrote order details consistently.

⸻

## Author

**Developed by:** Ahmad Kourouma  
**Academy:** Year Up United  
**Capstone Project:** Capstone 1 — Accounting Ledger Application  
**GitHub:** [https://github.com/akgit00](https://github.com/akgit00)
