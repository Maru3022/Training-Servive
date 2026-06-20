# =============================================================================
# teardown.ps1 — Remove all k8s resources from the training namespace
# =============================================================================
$ErrorActionPreference = "Stop"
$NAMESPACE = "training"

Write-Host "Deleting k6 job..." -ForegroundColor Yellow
kubectl delete job k6-load-test -n $NAMESPACE --ignore-not-found=true

Write-Host "Deleting all deployments in namespace $NAMESPACE..." -ForegroundColor Yellow
kubectl delete namespace $NAMESPACE --ignore-not-found=true

Write-Host "Done. Namespace '$NAMESPACE' removed." -ForegroundColor Green
