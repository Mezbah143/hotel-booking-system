# Hotel Booking System

Spring Boot MVC final project for a simple hotel room booking website.

## Features

- Register, login, and logout
- User and admin roles
- Browse and search hotel rooms
- View room details with images
- Book rooms with check-in and check-out dates
- View and cancel personal bookings
- Admin add, edit, delete, and hide rooms
- Admin image upload for room photos
- MySQL database with Spring Data JPA

## Local Setup

Requirements:

- Java 17
- Maven
- MySQL

Create a local MySQL database:

```sql
CREATE DATABASE hotel_booking;
```

Run the app:

```bash
mvn spring-boot:run
```

The default local database settings are:

- `DB_URL=jdbc:mysql://localhost:3306/hotel_booking?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC`
- `DB_USERNAME=root`
- `DB_PASSWORD=`

You can override them with environment variables.

## Demo Accounts

Seed data is created automatically when the app starts.

- Admin: `admin@hotel.com` / `admin123`
- User: `user@hotel.com` / `user123`

## Render Deployment

Set these environment variables in Render:

- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `JPA_DDL_AUTO=update`
- `SEED_DATA=true`

Use an external MySQL database provider for the deployed database. The app uses local uploaded files for room images; on Render, uploaded files may not persist after redeploys, so seeded image URLs remain available as a fallback.

## Final Report Checklist

Include:

1. Project title
2. Team members
3. Project overview
4. Main features
5. Technologies used
6. Database design
7. Screenshots
8. Development process
9. Problems and solutions
10. Conclusion
11. GitHub Repository URL
12. Deployed Website URL
