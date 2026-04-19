# it3030-paf-2026-smart-campus-group261

This backend uses MySQL in normal application runs.

Set your MySQL connection details before starting the app:

```powershell
$env:MYSQL_URL="jdbc:mysql://localhost:3306/campus_nexus_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true"
$env:MYSQL_USERNAME="root"
$env:MYSQL_PASSWORD="your-mysql-password"
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

- The app now uses `spring-boot-starter-data-jpa` with MySQL.
- Hibernate creates or updates the required tables automatically with `ddl-auto=update`.
- Test runs use an in-memory H2 database so `mvn test` does not require your local MySQL server to be running.
