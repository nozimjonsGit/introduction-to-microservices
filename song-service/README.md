# Song Service

The Song Service is a microservice dedicated to managing metadata related to songs, including artist names, album titles, and song lengths. This service operates alongside the Resource Service, which handles the actual MP3 files.

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
- PostgreSQL
- Mapstruct
- Docker

## API Endpoints

### 1. Create Song Metadata

- **Endpoint**: `POST /songs`
- **Description**: Create a new song metadata record in the database.
- **Request Body**: 
  ```json
  {
      "id": 1,
      "name": "We are the champions",
      "artist": "Queen",
      "album": "News of the world",
      "length": "2:59",
      "year": "1977"
  }
  ```
- **Response**:
    - **200 OK**:
      ```json
      {
          "id": 1123
      }
      ```
    - **400 Bad Request**: Song metadata missing validation error.
    - **500 Internal Server Error**: An internal server error has occurred.

### 2. Get Song Metadata

- **Endpoint**: `GET /songs/{id}`
- **Description**: Get song metadata by ID.

- **Response**:
    - **200 OK**:
      ```json
      {
        "name": "We are the champions",
        "artist": "Queen",
        "album": "News of the world",
        "length": "2:59",
        "resourceId": 123,
        "year": "1977"
      }
      ```
  - **404 Not Found:** The song metadata with the specified id does not exist.
  - **500 Internal Server Error:** An internal server error has occurred.


### 3. Delete Resources

- **Endpoint**: `DELETE /songs?id=1,2`
- **Description**: Delete song(s) metadata by IDs. If there is no song metadata for ID, do nothing.
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
POSTGRES_USER=song_user 
POSTGRES_PASSWORD=song_password 
POSTGRES_DB=song_db
```

## Setup Instructions

1. Ensure Docker and Docker Compose are installed on your machine.
2. Create a `.env` file in the root directory as specified above.

## Running the Service

To start the Song Service along with the PostgreSQL database, run the following command:

```bash
docker-compose -f docker-compose.yml up --build
```

## Connecting to PostgreSQL

To connect to the PostgreSQL database, you can use the following command:

```bash
docker exec -it song-service-db psql -U song_user -d song_db
```

You can also connect from your host machine:

```bash
psql -h localhost -U song_user -d song_db -p 5434
```