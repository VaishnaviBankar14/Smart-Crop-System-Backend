🌾 Smart Crop System – Backend

A Spring Boot–based backend for the Smart Crop System, a data-driven agricultural decision support platform that provides crop recommendations, yield prediction, weather-aware insights, authentication, and farmer history tracking.

🚀 Features

🔐 JWT-based Authentication
User registration & login
Role-based access (Farmer/Admin)

🌦 Weather Integration
Real-time weather data via OpenWeather API
Temperature & rainfall extraction

🌱 Crop Recommendation Engine

Auto crop recommendation based on:
Location (city / latitude & longitude)
Soil pH
Weather conditions

📊 Yield Prediction
Predict expected yield for recommended crops

🕒 History Tracking
Farmer recommendation history
Admin-level history access

🗄 Cloud-Ready Database
MySQL (Railway cloud DB)

🐳 Dockerized Deployment
Multi-stage Docker build
Deployed on Render

🛠 Tech Stack

Backend Framework: Spring Boot 
Security: Spring Security + JWT
ORM: Spring Data JPA (Hibernate)
Database: MySQL (Railway Cloud)
Weather API: OpenWeatherMap
Build Tool: Maven
Containerization: Docker
Deployment: Render
