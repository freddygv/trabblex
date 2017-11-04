#!/bin/bash

gcloud container clusters create trabblex --machine-type=f1-micro --num-nodes=4 --disk-size=30
gcloud compute disks create postgres-disk --size 200GB
gcloud compute disks create seedbox-disk --size 200GB

gcloud container clusters get-credentials "trabblex"

kubectl create -f k8s-postgres-server.yaml
kubectl create -f k8s-portal.yaml
kubectl create -f k8s-seedbox.yaml
kubectl create -f k8s-cmanager.yaml

COUNT=$(kubectl get pods | grep 'ContainerCreating' | wc -l)
while [ $COUNT -gt 0 ]; do
  sleep 30
  ERROR=$(kubectl get pods | grep -E 'Error|CrashLoopBackOff' | wc -l)
  if [ $ERROR -gt 0 ]; then
  	echo "Errors present"
    kubectl get pods | grep 'Error'
    break
  fi

  COUNT=$(kubectl get pods | grep 'ContainerCreating' | wc -l)
	echo Number of containers pending creation: $COUNT
done

kubectl get pods
