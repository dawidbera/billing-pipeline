# GitOps Deployment with Argo CD

This guide describes how to configure the billing-pipeline project for GitOps deployment using **Argo CD**, now that the repository is stored on **GitHub.com**.

---

## 1. Prerequisites
- A Kubernetes cluster (e.g., K3s, K3d, or Minikube) running locally or in the cloud.
- Git repository pushed to GitHub.
- `kubectl` CLI installed and configured.

---

## 2. Installing Argo CD
If Argo CD is not already installed on your cluster, install it by running:

```bash
# Create the argocd namespace
kubectl create namespace argocd

# Apply the official Argo CD installation manifests
kubectl apply -n argocd -f https://raw.githubusercontent.com/argoproj/argo-cd/stable/manifests/install.yaml
```

Wait for all Argo CD components to be ready:
```bash
kubectl rollout status deployment/argocd-server -n argocd --timeout=120s
```

---

## 3. Configuring the Argo CD Application

1. Open the Argo CD Application manifest: [argocd/application.yaml](file:///home/d/project-name/billing-pipeline/argocd/application.yaml).
2. Change the `repoURL` value to point to your GitHub repository:
   ```yaml
   repoURL: 'https://github.com/dawidbera/billing-pipeline.git'
   ```
3. Apply the Application resource to your cluster:
   ```bash
   kubectl apply -f argocd/application.yaml
   ```

Argo CD will automatically create the `java-app-demo` namespace and deploy:
- LocalStack (mocked S3)
- Java Spring Boot Application
- Service & Ingress rules

---

## 4. Docker Image Management (CI/CD)

### Option A: Local Development (K3d / K3s)
Since the manifest uses `image: java-app:latest` with `imagePullPolicy: IfNotPresent`, the cluster needs access to this image. If you are developing locally:
1. Run the local build script to compile the application and build the Docker image:
   ```bash
   # Build app and Docker image
   mvn -f java-app/pom.xml clean package -DskipTests
   docker build -t java-app:latest .
   ```
2. Import the image into your local k3d nodes:
   ```bash
   NODES=$(docker ps --filter "name=k3d-obs-platform-" --format "{{.Names}}")
   for node in $NODES; do
       if [[ "$node" != *"-serverlb" ]]; then
           docker save java-app:latest | docker exec -i "$node" ctr --namespace k8s.io images import -
       fi
   done
   ```

### Option B: Cloud Deployment (GitHub Actions + Container Registry)
For a fully automated pipeline:
1. Push your Docker image to a registry (e.g., GitHub Container Registry `ghcr.io` or Docker Hub):
   ```bash
   docker tag java-app:latest ghcr.io/<your-username>/java-app:latest
   docker push ghcr.io/<your-username>/java-app:latest
   ```
2. Update the image reference in [k8s/app.yaml](file:///home/d/project-name/billing-pipeline/k8s/app.yaml):
   ```yaml
   spec:
     containers:
       - name: java-app
         image: ghcr.io/<your-username>/java-app:latest
         imagePullPolicy: Always
   ```
3. Commit and push the changes to GitHub. Argo CD will automatically detect the change and sync the deployment.
