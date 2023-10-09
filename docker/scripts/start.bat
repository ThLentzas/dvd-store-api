docker network create dvd-net

docker run --name postgres -d ^
--env-file ..\env\psql.env ^
--network dvd-net ^
-v pgdata:/var/lib/postgresql/data ^
postgres:15.2-alpine

docker run -d ^
--network dvd-net ^
--name redis redis:7.0.10-alpine

docker run -p 8080:8080 -d ^
--env-file ..\env\dvd-store.env ^
--network dvd-net ^
--name dvd-store dvd-store