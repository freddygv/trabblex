#!/bin/bash

gcloud container clusters create trabblex --machine-type=g1-small --num-nodes=4 --disk-size=30
gcloud compute disks create postgres-disk --size 200GB

gcloud container clusters get-credentials "trabblex"

kubectl create -f k8s-postgres-server.yaml
kubectl create -f k8s-portal.yaml
kubectl create -f k8s-seedbox.yaml
kubectl create -f k8s-cmanager.yaml

kubectl get pods
