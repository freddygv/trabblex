#!/bin/bash

COUNT=$(kubectl get pods | grep 'ContainerCreating' | wc -l)
while [ $COUNT -gt 0 ]; do
  ERROR=kubectl get pods | grep 'Error' | wc -l
  if [ $ERROR -gt 0 ]; then
  	echo "Errors present"
    kubectl get pods | grep 'Error'
  fi

	echo Number of containers pending creation: $COUNT
  sleep 30
done

echo "All pods running."
