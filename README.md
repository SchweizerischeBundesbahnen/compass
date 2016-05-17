# compass - Backend for managing Short-Links

## How to run

Use this docker-compose.yml

compass:
  image: imasen/compass
  ports:
    - "8080:8080"
  environment:
    REDIS: redis
  links:
    - redis
  restart: always

redis:
  image: redis
  restart: always