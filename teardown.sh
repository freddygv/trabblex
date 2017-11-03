#!/bin/bash

kubectl delete service cmanager; kubectl delete service seedbox

sleep 2m

gcloud compute forwarding-rules list

gcloud container clusters delete trabblex
gcloud compute disks delete postgres-disk
