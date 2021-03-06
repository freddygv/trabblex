#!/bin/bash

kubectl delete service cmanager
kubectl delete service seedbox

# Wait for static IP to un-bind
sleep 240

gcloud compute forwarding-rules list

yes | gcloud container clusters delete trabblex
yes | gcloud compute disks delete postgres-disk
yes | gcloud compute disks delete seedbox-disk
