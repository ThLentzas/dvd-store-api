docker stop postgres redis dvd-store

docker rm postgres redis dvd-store

docker volume rm pgdata

docker system prune -f