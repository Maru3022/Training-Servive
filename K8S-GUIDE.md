# Kubernetes + k6 + Grafana — Руководство запуска

## Структура

```
k8s/
├── namespace.yaml
├── postgres/
│   ├── pvc.yaml
│   ├── deployment.yaml
│   └── service.yaml
├── redis/
│   ├── deployment.yaml
│   └── service.yaml
├── kafka/
│   ├── deployment.yaml
│   └── service.yaml
├── app/
│   ├── deployment.yaml          ← Training Service (imagePullPolicy: Never)
│   └── service.yaml             ← NodePort 30085
├── monitoring/
│   ├── prometheus/
│   │   ├── rbac.yaml            ← ServiceAccount + ClusterRole для pod discovery
│   │   ├── configmap.yaml       ← scrape config (pod annotations)
│   │   ├── deployment.yaml      ← remote-write receiver включён
│   │   └── service.yaml         ← NodePort 30090
│   └── grafana/
│       ├── configmap-datasource.yaml       ← Prometheus datasource
│       ├── configmap-dashboard-provider.yaml
│       ├── configmap-dashboard-k6.yaml     ← k6 + Spring Boot дашборд
│       ├── deployment.yaml
│       └── service.yaml         ← NodePort 30030
└── k6/
    ├── configmap.yaml           ← k6 скрипт
    └── job.yaml                 ← запускать вручную
scripts/
├── deploy.ps1   ← Windows PowerShell
└── deploy.sh    ← Linux / macOS / Git Bash
```

---

## Требования

- [minikube](https://minikube.sigs.k8s.io/docs/start/) ≥ 1.32
- kubectl
- Docker Desktop (Windows)
- Java 17 + Maven (для сборки JAR)

---

## Шаг 1 — Запустить minikube

```powershell
minikube start --memory=4096 --cpus=4 --driver=docker
```

Минимум 4 ГБ RAM — нужен для PostgreSQL + Kafka + приложения + мониторинга.

---

## Шаг 2 — Задеплоить всё

```powershell
# Из корня проекта
.\scripts\deploy.ps1
```

Скрипт автоматически:
1. Собирает JAR через `mvnw.cmd`
2. Строит Docker-образ **внутри** minikube daemon (не нужен registry)
3. Применяет все манифесты в правильном порядке
4. Ждёт готовности каждого компонента
5. Выводит URL-адреса

---

## Шаг 3 — Проверить что всё работает

```powershell
kubectl get pods -n training
```

Ожидаемый вывод:
```
NAME                                READY   STATUS    RESTARTS
grafana-xxx                         1/1     Running   0
kafka-xxx                           1/1     Running   0
postgres-xxx                        1/1     Running   0
prometheus-xxx                      1/1     Running   0
redis-xxx                           1/1     Running   0
training-service-xxx (x2)           1/1     Running   0
```

### Адреса (узнать IP minikube: `minikube ip`)

| Сервис           | URL                              | Логин     |
|------------------|----------------------------------|-----------|
| Training Service | `http://<minikube-ip>:30085`     | —         |
| Swagger UI       | `http://<minikube-ip>:30085/swagger-ui.html` | — |
| Prometheus       | `http://<minikube-ip>:30090`     | —         |
| Grafana          | `http://<minikube-ip>:30030`     | admin/admin |

---

## Шаг 4 — Запустить k6 нагрузочный тест

```powershell
# Удалить предыдущий запуск (если был)
kubectl delete job k6-load-test -n training --ignore-not-found=true

# Запустить новый
kubectl apply -f k8s/k6/job.yaml

# Следить за логами в реальном времени
kubectl logs -f job/k6-load-test -n training
```

Тест занимает ~5 минут (ramp-up → steady → spike → ramp-down).

---

## Шаг 5 — Смотреть в Grafana

1. Открыть `http://<minikube-ip>:30030`
2. Войти: `admin` / `admin`
3. Перейти в **Dashboards → Training Service → k6 Load Testing Results**

### Что показывает дашборд

| Панель | Метрика |
|--------|---------|
| Virtual Users | `k6_vus` — текущее число виртуальных пользователей |
| Requests/s | `rate(k6_http_reqs_total[1m])` |
| HTTP Failure Rate | процент ошибочных запросов |
| p95 Response Time | 95-й перцентиль времени ответа |
| Response Time Percentiles | p50 / p90 / p95 / p99 |
| HTTP Request Duration by Phase | connect / TLS / send / wait / receive |
| Spring Boot HTTP Rate | метрики с `/actuator/prometheus` |
| Spring Boot p95 | серверная сторона по URI |
| JVM Heap / CPU / DB Connections | инфраструктурные метрики приложения |

---

## Запустить k6 локально (без Job в кластере)

Если k6 установлен локально:

```powershell
$ip = minikube ip
k6 run `
  --out experimental-prometheus-rw `
  -e K6_PROMETHEUS_RW_SERVER_URL="http://${ip}:30090/api/v1/write" `
  -e BASE_URL="http://${ip}:30085" `
  k8s/k6/script.js
```

---

## Перезапуск только приложения (после изменений кода)

```powershell
# Пересобрать образ внутри minikube daemon
eval $(minikube docker-env)        # или в PowerShell: minikube docker-env --shell powershell | Invoke-Expression
docker build -t training-service:latest .

# Рестартовать поды
kubectl rollout restart deployment/training-service -n training
kubectl rollout status  deployment/training-service -n training
```

---

## Удалить всё

```powershell
.\scripts\teardown.ps1
# или
kubectl delete namespace training
```

---

## Troubleshooting

### Под приложения в `CrashLoopBackOff`
```powershell
kubectl logs deployment/training-service -n training --previous
```
Чаще всего — Kafka или Postgres ещё не готовы. Подождать 1-2 минуты, рестартовать:
```powershell
kubectl rollout restart deployment/training-service -n training
```

### k6 не видит приложение
```powershell
# Проверить что сервис резолвится внутри кластера
kubectl run test --rm -it --image=curlimages/curl -n training -- \
  curl http://training-service:8085/actuator/health
```

### Prometheus не скрейпит поды
```powershell
# Проверить targets в Prometheus UI
# http://<minikube-ip>:30090/targets
# Убедиться что у подов есть аннотации:
kubectl get pods -n training -o jsonpath='{range .items[*]}{.metadata.name}{" "}{.metadata.annotations}{"\n"}{end}'
```

### Недостаточно памяти
```powershell
minikube stop
minikube start --memory=6144 --cpus=4 --driver=docker
```
