# IT Asset & Equipment Tracker

A full-stack IT asset management system.

## Technologies

### Backend
- Java 21
- Spring Boot
- Spring Security and JWT
- PostgreSQL
- Maven
- Docker

## Frontend
- HTML
- CSS
- JavaScript
- React
- Vite
## Requirements

- Java 21
- Docker Desktop
- Node.js and npm

## Run the Backend

From the repository root:

```powershell
docker compose up -d --build
cd frontend
npm install
npm run dev
## Application URLs

- **Frontend:** http://localhost:5174/
- **Backend:** http://localhost:8081
- **Swagger UI:** http://localhost:8081/swagger-ui/index.html

## Database

- **Database:** PostgreSQL 17
- **Database name:** `it_asset_tracker`
- **Database user:** `asset_user`
- **Docker container:** PostgreSQL

## Screenshots

Project screenshots are stored in the `screenshots/` folder.

## Stop the Application

From the project root:

```powershell
docker compose stop
```

To stop the frontend, press `Ctrl + C` in its terminal.

## Project Structure

```text
IT-Asset-Equipment-Tracker/
├── src/
├── frontend/
├── screenshots/
├── Dockerfile
├── docker-compose.yml
├── pom.xml
├── .env
└── README.md
```

## Security

- Passwords are encrypted using BCrypt.
- Authentication uses JWT.
- Database credentials and JWT secrets are configured through environment variables.
- Do not commit the `.env` file to GitHub.