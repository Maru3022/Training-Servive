# =============================================================================
# deploy.ps1 — Build Docker image, load into minikube, apply all k8s manifests
# Run from project root:  .\scripts\deploy.ps1
# =============================================================================
$ErrorActionPreference = "Stop"

$NAMESPACE  = "training"
$IMAGE_NAME = "training-service"
$IMAGE_TAG  = "latest"

function Info  { param($msg) Write-Host "[INFO]  $msg" -ForegroundColor Green  }
function Warn  { param($msg) Write-Host "[WARN]  $msg" -ForegroundColor Yellow }
function Err   { param($msg) Write-Host "[ERROR] $msg" -ForegroundColor Red; exit 1 }

$ProjectDir = Split-Path -Parent $PSScriptRoot
$K8S        = Join-Path $ProjectDir "k8s"

# ---------------------------------------------------------------------------
# 1. Pre-checks
# ---------------------------------------------------------------------------
Info "Checking prerequisites..."
foreach ($cmd in @("minikube","kubectl","docker")) {
    if (-not (Get-Command $cmd -ErrorAction SilentlyContinue)) {
        Err "$cmd not found in PATH"
    }
}

$status = minikube status 2>&1
if ($LASTEXITCODE -ne 0) {
    Err "minikube is not running. Start it with:`n  minikube start --memory=4096 --cpus=4"
}
Info "minikube is running."

# ---------------------------------------------------------------------------
# 2. Build JAR
# ---------------------------------------------------------------------------
Info "Building JAR (skipping tests)..."
Push-Location $ProjectDir
if (Test-Path ".\mvnw.cmd") {
    & .\mvnw.cmd clean package -DskipTests -q
} else {
    & mvn clean package -DskipTests -q
}
if ($LASTEXITCODE -ne 0) { Err "Maven build failed" }
Pop-Location
Info "JAR built."

# ---------------------------------------------------------------------------
# 3. Build Docker image inside minikube daemon
# ---------------------------------------------------------------------------
Info "Configuring Docker to use minikube daemon..."
& minikube docker-env --shell powershell | Invoke-Expression

Info "Building Docker image ${IMAGE_NAME}:${IMAGE_TAG}..."
docker build -t "${IMAGE_NAME}:${IMAGE_TAG}" $ProjectDir
if ($LASTEXITCODE -ne 0) { Err "Docker build failed" }
Info "Docker image built."

# ---------------------------------------------------------------------------
# 4. Apply manifests
# ---------------------------------------------------------------------------
Info "Creating namespace..."
kubectl apply -f "$K8S\namespace.yaml"

Info "Deploying PostgreSQL..."
kubectl apply -f "$K8S\postgres\pvc.yaml"
kubectl apply -f "$K8S\postgres\deployment.yaml"
kubectl apply -f "$K8S\postgres\service.yaml"

Info "Deploying Redis..."
kubectl apply -f "$K8S\redis\deployment.yaml"
kubectl apply -f "$K8S\redis\service.yaml"

Info "Deploying Kafka..."
kubectl apply -f "$K8S\kafka\deployment.yaml"
kubectl apply -f "$K8S\kafka\service.yaml"

Info "Waiting for infra..."
kubectl rollout status deployment/postgres -n $NAMESPACE --timeout=120s
kubectl rollout status deployment/redis    -n $NAMESPACE --timeout=60s
kubectl rollout status deployment/kafka    -n $NAMESPACE --timeout=120s

Info "Deploying Training Service..."
kubectl apply -f "$K8S\app\deployment.yaml"
kubectl apply -f "$K8S\app\service.yaml"

Info "Deploying Prometheus..."
kubectl apply -f "$K8S\monitoring\prometheus\rbac.yaml"
kubectl apply -f "$K8S\monitoring\prometheus\configmap.yaml"
kubectl apply -f "$K8S\monitoring\prometheus\deployment.yaml"
kubectl apply -f "$K8S\monitoring\prometheus\service.yaml"

Info "Deploying Grafana..."
kubectl apply -f "$K8S\monitoring\grafana\configmap-datasource.yaml"
kubectl apply -f "$K8S\monitoring\grafana\configmap-dashboard-provider.yaml"
kubectl apply -f "$K8S\monitoring\grafana\configmap-dashboard-k6.yaml"
kubectl apply -f "$K8S\monitoring\grafana\deployment.yaml"
kubectl apply -f "$K8S\monitoring\grafana\service.yaml"

Info "Applying k6 script ConfigMap..."
kubectl apply -f "$K8S\k6\configmap.yaml"

Info "Waiting for training-service..."
kubectl rollout status deployment/training-service -n $NAMESPACE --timeout=180s

# ---------------------------------------------------------------------------
# 5. URLs
# ---------------------------------------------------------------------------
$ip = minikube ip
Write-Host ""
Write-Host "================================================================" -ForegroundColor Green
Write-Host "  DEPLOYMENT COMPLETE" -ForegroundColor Green
Write-Host "================================================================" -ForegroundColor Green
Write-Host ""
Write-Host "  Training Service API : http://${ip}:30085"
Write-Host "  Swagger UI           : http://${ip}:30085/swagger-ui.html"
Write-Host "  Prometheus           : http://${ip}:30090"
Write-Host "  Grafana              : http://${ip}:30030  (admin/admin)"
Write-Host ""
Write-Host "  To run k6 load test:" -ForegroundColor Cyan
Write-Host "    kubectl delete job k6-load-test -n training --ignore-not-found=true"
Write-Host "    kubectl apply -f k8s\k6\job.yaml"
Write-Host "    kubectl logs -f job/k6-load-test -n training"
Write-Host ""
Write-Host "================================================================" -ForegroundColor Green
