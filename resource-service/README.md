# Resource Service

The Resource Service is a microservice responsible for managing MP3 file uploads and processing their metadata. This service interacts with a PostgreSQL database to store audio files as BLOBs and utilizes a separate Song Service for managing song metadata.

## Table of Contents

- [Technologies Used](#technologies-used)
- [API Endpoints](#api-endpoints)
- [Environment Variables](#environment-variables)
- [Setup Instructions](#setup-instructions)
- [Running the Service](#running-the-service)
- [Connecting to PostgreSQL](#connecting-to-postgresql)

## Technologies Used

- Java 17
- Spring Boot
- Spring Data JPA
- Lombok
- RestTemplate
- PostgreSQL
- Docker

## API Endpoints

### 1. Upload MP3 File

- **Endpoint**: `POST /resources`
- **Description**: Upload a new MP3 resource.
- **Request Body**: Binary MP3 data (Content-Type: audio/mpeg)
- **Response**:
    - **200 OK**:
      ```json
      {
          "id": 1123
      }
      ```
    - **400 Bad Request**: Validation failed or request body is invalid MP3.
    - **500 Internal Server Error**: An internal server error has occurred.

### 2. Get MP3 File

- **Endpoint**: `GET /resources/{id}`
- **Description**: Get the binary audio data of a resource.
- **Response**:
    - **200 OK**: Binary MP3 data.
    - **404 Not Found**: The resource with the specified id does not exist.
    - **500 Internal Server Error**: An internal server error has occurred.

### 3. Delete Resources

- **Endpoint**: `DELETE /resources?id=1,2`
- **Description**: Delete resource(s) by IDs. If there is no resource for ID, do nothing.
- **Response**:
    - **200 OK**:
      ```json
      {
          "ids": [1, 2]
      }
      ```
    - **500 Internal Server Error**: An internal server error has occurred.

## Environment Variables

Create a `.env` file in the root directory with the following variables:
```
POSTGRES_USER=resource_user 
POSTGRES_PASSWORD=resource_password 
POSTGRES_DB=resource_db
```

## Setup Instructions

1. Ensure Docker and Docker Compose are installed on your machine.
2. Create a `.env` file in the root directory as specified above.

## Running the Service

To start the Resource Service along with the PostgreSQL database, run the following command:

```bash
docker-compose -f docker-compose.yml up --build
```

## Connecting to PostgreSQL

To connect to the PostgreSQL database, you can use the following command:

```bash
docker exec -it resource-service-db psql -U resource_user -d resource_db
```

You can also connect from your host machine:

```bash
psql -h localhost -U resource_user -d resource_db -p 5433
```