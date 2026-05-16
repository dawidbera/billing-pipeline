#!/usr/bin/env bash

# Purpose: Local CI/CD deployment script for Java S3 Application on K3s.
# Performs Maven build, Docker packaging, local cluster image loading, and applying K8s manifests.

set -euo pipefail

# Configuration
NAMESPACE="java-app-demo"
IMAGE_NAME="java-app"
IMAGE_TAG="latest"
FULL_IMAGE="${IMAGE_NAME}:${IMAGE_TAG}"
KUBECONFIG_PATH="${HOME}/.kube/config"

export KUBECONFIG="${KUBECONFIG_PATH}"

echo "========================================================="
echo " Starting Local CI/CD Deployment to K3s Cluster "
echo "========================================================="

# Step 1: Verify pre-requisites
echo "Checking requirements..."
for cmd in docker kubectl mvn; do
    if ! command -v "$cmd" &> /dev/null; then
        echo "Error: $cmd is not installed or not in PATH." >&2
        exit 1
    fi
done

# Step 2: Build the Java application
echo "Building Java project with Maven..."
mvn -f java-app/pom.xml clean package -DskipTests

# Step 3: Build the Docker image
echo "Building Docker image ${FULL_IMAGE}..."
docker build -t "${FULL_IMAGE}" .

# Step 4: Import the image into all k3d cluster nodes (excluding load balancer)
echo "Importing Docker image into k3d cluster nodes..."
NODES=$(docker ps --filter "name=k3d-obs-platform-" --format "{{.Names}}")
for node in $NODES; do
    if [[ "$node" != *"-serverlb" ]]; then
        echo "--> Loading image into node: $node"
        docker save "${FULL_IMAGE}" | docker exec -i "$node" ctr --namespace k8s.io images import -
    fi
done

# Step 5: Apply Kubernetes manifests
echo "Applying Kubernetes manifests..."
kubectl apply -f k8s/namespace.yaml
kubectl apply -f k8s/localstack.yaml
kubectl apply -f k8s/app.yaml

# Step 6: Wait for services to be ready
echo "Waiting for LocalStack deployment to be ready..."
kubectl rollout status deployment/localstack -n "${NAMESPACE}" --timeout=120s

echo "Waiting for S3 Bucket initialization..."
# Give LocalStack's init script a moment to execute
sleep 5

echo "Waiting for Java Application deployment to be ready..."
kubectl rollout status deployment/java-app -n "${NAMESPACE}" --timeout=120s

echo "========================================================="
echo " Deployment Completed Successfully! "
echo "========================================================="
echo "You can access the application at:"
echo "  URL: http://java-app.localhost/api/files"
echo "  (Note: Ensure 'java-app.localhost' resolves to 127.0.0.1 or"
echo "   override host header: curl -H 'Host: java-app.localhost' http://localhost/api/files)"
echo ""
echo "Monitoring metric scrape URL:"
echo "  http://java-app.localhost/actuator/prometheus"
echo "========================================================="
