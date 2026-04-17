# 🍽️ Visualize AI

**Turn Raw Restaurant Data into Strategic Profit.**

Visualize AI is a professional-grade restaurant analytics platform designed to convert complex datasets into clear, actionable business intelligence. By leveraging internal heuristic engines and a premium "Stitch" design system, it provides restaurant owners with deep insights into menu performance, profitability trends, and growth opportunities.

---

## ✨ Key Features

### 📈 Sales Intelligence Dashboard
- **Dynamic KPIs**: Real-time tracking of Total Revenue, Net Profit, Average Order Value (AOV), and Monthly Growth.
- **Deep Learning Trends**: Advanced bar and line visualizations that identify non-obvious correlations in sales cycles.
- **High-End Visualization**: Beautiful, accessible charts powered by `Recharts` and customized for readability.

### 🍱 AI-Driven Menu Optimization
- **Context-Aware Recommendations**: The system doesn't just flag low-performing items; it understands context.
- **Sensitivity Hard-Filters**: Prevents the removal of low-cost staple items (≤ ₹50), instead suggesting bundling or cost-optimization strategies.
- **Volume & Revenue Check**: Automated alerts for items with <100 units sold or underperforming revenue per unit.

### 📂 Universal Data Ingestion
- **One-Click Upload**: Native support for **CSV** and **Excel (.xlsx)** datasets.
- **Fault-Tolerant Parsing**: High-performance ingestion engine that handles messy datasets with grace.

---

## 🎨 Design Philosophy: "Ethereal Intelligence"
Visualize AI uses a custom iteration of the **Stitch Design System**, prioritizing:
- **Negative Space**: Reduced clutter for maximum focus on data.
- **Tonal Layering**: Smooth glassmorphic surfaces with no hard solid borders.
- **Editorial Typography**: Using *Plus Jakarta Sans* for headlines to maintain a premium, data-led aesthetic.

---

## 🛠️ Technical Architecture

### Frontend (Modern React)
- **Framework**: Vite + React
- **Styling**: Tailwind CSS + Vanilla CSS Modules
- **Charts**: Recharts
- **State Management**: Context API

### Backend (Robust Spring Boot)
- **Language**: Java 17
- **Framework**: Spring Boot 3.2.5
- **Database**: H2 (In-memory for fast local deployment)
- **Parsers**: Apache POI (Excel) & OpenCSV (CSV)

---

## ⚙️ Quick Start

### 1. Prerequisites
- **Java 17+**
- **Node.js 18+**
- **Maven**

### 2. Backend Setup
```bash
# Navigate to root
mvn clean install
mvn spring-boot:run
```
The backend will start on `http://localhost:8080`.

### 3. Frontend Setup
```bash
# Navigate to frontend folder
cd frontend
npm install
npm run dev
```
The application will be accessible at `http://localhost:5173`.

---

## 🎯 Target Markets
Built specifically for **Hospitality**, **Cloud Kitchens**, and **Retail Chains** requiring immediate clarity on their inventory and menu ROI without the overhead of enterprise-level BIs.

---
*Built with ❤️ by the Visualize AI team.*
