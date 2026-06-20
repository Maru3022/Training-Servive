#!/usr/bin/env bash
# =============================================================================
# deploy.sh — Build Docker image, load into minikube, apply all k8s manifests
# =============================================================================
set -euo pipefail

NAMESPACE="training"
IMAGE_NAME="training-service"
IMAGE_TAG="latest"

# Colour helpers
GREEN='\033[0;32m'; YELLOW='\033[1;33m'; RED='\033[0;31m'; NC='\033[0m'
info()    { echo -e "${GREEN}[INFO]${NC}  $*"; }
warn()    { echo -e "${YELLOW}[WARN]${NC}  $*"; }
error()   { echo -e "${RED}[ERROR]${NC} $*"; exit 1; }

# ---------------------------------------------------------------------------
# 1. Pre-checks
# ---------------------------------------------------------------------------
command -v minikube >/dev/null 2>&1 || error "minikube not found"
command -v kubectl  >/dev/null 2>&1 || error "kubectl not found"
command -v docker   >/dev/null 2>&1 || error "docker not found"
command -v mvn      >/dev/null 2>&1 || command -v ./mvnw >/dev/null 2>&1 || error "maven not found"

info "minikube status:"
minikube status || error "minikube is not running. Start it with: minikube start --memory=4096 --cpus=4"

# ---------------------------------------------------------------------------
# 2. Build JAR
# ---------------------------------------------------------------------------
info "Building JAR (skipping tests)..."
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"

cd "$PROJECT_DIR"
if [ -f "./mvnw" ]; then
  ./mvnw clean package -DskipTests -q
else
  mvn clean package -DskipTests -q
fi
info "JAR built successfully."

# ---------------------------------------------------------------------------
# 3. Build Docker image INSIDE minikube's Docker daemon
# ---------------------------------------------------------------------------
info "Pointing Docker to minikube daemon..."
eval "$(minikube docker-env)"

info "Building Docker image ${IMAGE_NAME}:${IMAGE_TAG}..."
docker build -t "${IMAGE_NAME}:${IMAGE_TAG}" "$PROJECT_DIR"
info "Docker image built."

# ---------------------------------------------------------------------------
# 4. Apply k8s manifests (ordered)
# ---------------------------------------------------------------------------
K8S="$PROJECT_DIR/k8s"

info "Creating namespace..."
kubectl apply -f "$K8S/namespace.yaml"

info "Deploying PostgreSQL..."
kubectl apply -f "$K8S/postgres/pvc.yaml"
kubectl apply -f "$K8S/postgres/deployment.yaml"
kubectl apply -f "$K8S/postgres/service.yaml"

info "Deploying Redis..."
kubectl apply -f "$K8S/redis/deployment.yaml"
kubectl apply -f "$K8S/redis/service.yaml"

info "Deploying Kafka..."
kubectl apply -f "$K8S/kafka/deployment.yaml"
kubectl apply -f "$K8S/kafka/service.yaml"

info "Waiting for infrastructure to be ready..."
kubectl rollout status deployment/postgres -n "$NAMESPACE" --timeout=120s
kubectl rollout status deployment/redis    -n "$NAMESPACE" --timeout=60s
kubectl rollout status deployment/kafka    -n "$NAMESPACE" --timeout=120s

info "Deploying Training Service..."
kubectl apply -f "$K8S/app/deployment.yaml"
kubectl apply -f "$K8S/app/service.yaml"

info "Deploying Prometheus..."
kubectl apply -f "$K8S/monitoring/prometheus/rbac.yaml"
kubectl apply -f "$K8S/monitoring/prometheus/configmap.yaml"
kubectl apply -f "$K8S/monitoring/prometheus/deployment.yaml"
kubectl apply -f "$K8S/monitoring/prometheus/service.yaml"

info "Deploying Grafana..."
kubectl apply -f "$K8S/monitoring/grafana/configmap-datasource.yaml"
kubectl apply -f "$K8S/monitoring/grafana/configmap-dashboard-provider.yaml"
kubectl apply -f "$K8S/monitoring/grafana/configmap-dashboard-k6.yaml"
kubectl apply -f "$K8S/monitoring/grafana/deployment.yaml"
kubectl apply -f "$K8S/monitoring/grafana/service.yaml"

info "Applying k6 ConfigMap (script)..."
kubectl apply -f "$K8S/k6/configmap.yaml"

# ---------------------------------------------------------------------------
# 5. Wait for app
# ---------------------------------------------------------------------------
info "Waiting for training-service to be ready..."
kubectl rollout status deployment/training-service -n "$NAMESPACE" --timeout=180s

# ---------------------------------------------------------------------------
# 6. Print access URLs
# ---------------------------------------------------------------------------
MINIKUBE_IP=$(minikube ip)
echo ""
echo -e "${GREEN}================================================================${NC}"
echo -e "${GREEN}  DEPLOYMENT COMPLETE${NC}"
echo -e "${GREEN}================================================================${NC}"
echo ""
echo -e "  Training Service API : http://${MINIKUBE_IP}:30085"
echo -e "  Swagger UI           : http://${MINIKUBE_IP}:30085/swagger-ui.html"
echo -e "  Prometheus           : http://${MINIKUBE_IP}:30090"
echo -e "  Grafana              : http://${MINIKUBE_IP}:30030  (admin/admin)"
echo ""
echo -e "  To run k6 load test:"
echo -e "    kubectl delete job k6-load-test -n training --ignore-not-found"
echo -e "    kubectl apply -f k8s/k6/job.yaml"
echo -e "    kubectl logs -f job/k6-load-test -n training"
echo ""
echo -e "${GREEN}================================================================${NC}"
