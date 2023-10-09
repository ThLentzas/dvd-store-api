@echo off

rem Deploying PostgreSQL resources
kubectl apply -f ..\psql\psql-pv.yaml
kubectl apply -f ..\psql\psql-claim.yaml
kubectl apply -f ..\psql\psql-config.yaml
kubectl apply -f ..\psql\psql-secret.yaml
kubectl apply -f ..\psql\psql.yaml
kubectl apply -f ..\psql\psql-service.yaml

rem Deploying Redis resources
kubectl apply -f ..\redis\redis.yaml
kubectl apply -f ..\redis\redis-service.yaml

rem Deploying our Dvd-store backend service
kubectl apply -f ..\dvd-store\dvd-store-config.yaml
kubectl apply -f ..\dvd-store\dvd-store-secret.yaml
kubectl apply -f ..\dvd-store\dvd-store.yaml
kubectl apply -f ..\dvd-store\dvd-store-service.yaml

rem Open the DVD Store application in the default web browser
minikube service dvd-store