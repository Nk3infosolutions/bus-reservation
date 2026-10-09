# Deploying to Kubernetes

These manifests deploy the application and a single MySQL instance. MySQL data is stored in a persistent volume claim. The application Service is a `LoadBalancer` on port 80.

## Prerequisites

- A running Kubernetes cluster, with `kubectl` configured for that cluster.
- Docker and access to a container registry the cluster can pull from.
- A cluster that provides external load balancers to assign a public URL/IP. Local clusters may require a tunnel or port forwarding.

## Build the application image

From the repository root:

```powershell
docker build -t bus-reservation:1.0 .
```

For Minikube, load the image into the cluster before applying the manifests:

```powershell
minikube image load bus-reservation:1.0
```

For a remote cluster, push the image to a registry the cluster can access and change the image in `k8s/app.yaml` to that registry path. For a private registry, configure an image pull secret on the cluster.

## Create database credentials

Create the Kubernetes Secret before applying the manifests. Replace both values with strong passwords; do not commit credentials to the repository.

```powershell
kubectl create secret generic bus-reservation-db `
  --from-literal=MYSQL_ROOT_PASSWORD='REPLACE_WITH_A_STRONG_ROOT_PASSWORD' `
  --from-literal=MYSQL_PASSWORD='REPLACE_WITH_A_STRONG_APP_PASSWORD'
```

The app password is used by both MySQL and the application.

## Deploy and get the URL

From the repository root:

```powershell
kubectl apply -f .\k8s\mysql.yaml
kubectl apply -f .\k8s\app.yaml
kubectl get pods
kubectl get services bus-reservation --watch
```

Wait until `EXTERNAL-IP` shows an address or hostname instead of `<pending>`. Open `http://<EXTERNAL-IP>/`; Swagger UI is at `http://<EXTERNAL-IP>/swagger-ui/index.html`.

For Minikube, the `LoadBalancer` external IP remains `<pending>`. Run the following in a separate terminal and keep it running; it opens a local tunnel and prints the app URL:

```powershell
minikube service bus-reservation --url
```

Open the printed URL; Swagger UI is at the same URL followed by `/swagger-ui/index.html`.

To remove the application and database resources:

```powershell
kubectl delete -f .\k8s\app.yaml
kubectl delete -f .\k8s\mysql.yaml
kubectl delete secret bus-reservation-db
```

Deleting `mysql.yaml` also deletes the persistent volume claim. Depending on the cluster's storage reclaim policy, the underlying database volume may also be deleted.
