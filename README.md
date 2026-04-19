# it3030-paf-2026-smart-campus-group261

This backend has been migrated from MySQL to MongoDB.

Set your MongoDB Atlas connection string before starting the app:

```powershell
$env:MONGODB_URI="mongodb+srv://<username>:<password>@<cluster-url>/campus_nexus_db?retryWrites=true&w=majority&appName=CampusNexus"
```

Then run:

```powershell
mvn spring-boot:run
```

If port `8080` is already in use, start the app on another port:

```powershell
$env:SERVER_PORT="8081"
mvn spring-boot:run
```

Notes:

- The app now uses `spring-boot-starter-data-mongodb`.
- Numeric `Long` IDs are still preserved using a MongoDB sequence collection named `database_sequences`.
- Collections created by the app include `users`, `resources`, `bookings`, `maintenance_tickets`, `notifications`, and `audit_logs`.
