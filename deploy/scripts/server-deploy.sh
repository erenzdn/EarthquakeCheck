#!/usr/bin/env bash
set -euo pipefail

# Sunucuda calistirin:
#   chmod +x deploy/scripts/server-deploy.sh
#   ./deploy/scripts/server-deploy.sh

ROOT_DIR="/var/opt/docker"
BACKEND_DIR="${ROOT_DIR}/EarthquakeCheck"
FRONTEND_DIR="${ROOT_DIR}/earthquake-check-frontend"
ENV_FILE="${BACKEND_DIR}/.env"
API_BASE="https://earthquakecheck.mehmeterenozden.com"

echo "==> Kod senkronizasyonu"
cd "${BACKEND_DIR}" && git pull --ff-only
cd "${FRONTEND_DIR}" && git pull --ff-only

if [[ ! -f "${ENV_FILE}" ]]; then
  echo "HATA: ${ENV_FILE} bulunamadi. Once .env.example dosyasindan .env olusturun."
  exit 1
fi

required_vars=(POSTGRES_DB POSTGRES_USER POSTGRES_PASSWORD DB_URL DB_USERNAME DB_PASSWORD APP_ADMIN_TOKEN CORS_ALLOWED_ORIGINS)
for var_name in "${required_vars[@]}"; do
  if ! grep -q "^${var_name}=" "${ENV_FILE}"; then
    echo "HATA: ${ENV_FILE} icinde ${var_name} eksik."
    exit 1
  fi
done

if [[ $(grep '^APP_ADMIN_TOKEN=' "${ENV_FILE}" | cut -d= -f2- | wc -c) -lt 33 ]]; then
  echo "HATA: APP_ADMIN_TOKEN en az 32 karakter olmali."
  exit 1
fi

echo "==> Backend build + deploy"
cd "${BACKEND_DIR}"
docker compose --env-file "${ENV_FILE}" build --no-cache app
docker compose --env-file "${ENV_FILE}" up -d

echo "==> Flyway V5 kontrolu"
docker compose --env-file "${ENV_FILE}" logs app | tail -n 80 | grep -i "flyway\|V5" || true

echo "==> Frontend build + deploy"
docker compose --env-file "${ENV_FILE}" build --no-cache frontend
docker compose --env-file "${ENV_FILE}" up -d frontend

echo "==> Container durumu"
docker compose --env-file "${ENV_FILE}" ps

echo "==> Saglik kontrolleri"
curl -fsS "${API_BASE}/api/building/evaluate" -o /dev/null -w "evaluate endpoint HTTP %{http_code}\n" \
  -X POST -H "Content-Type: application/json" \
  -d '{"yearBuilt":2000,"floorCount":5,"address":"Test"}' || true

curl -fsS -o /dev/null -w "swagger HTTP %{http_code}\n" "${API_BASE}/swagger-ui/index.html" || true
curl -fsS -o /dev/null -w "test endpoint HTTP %{http_code}\n" "${API_BASE}/api/test/location?address=test" || true

echo "==> PGA import (manuel)"
echo "Asagidaki komutu APP_ADMIN_TOKEN ile calistirin:"
echo "curl -X POST \"${API_BASE}/api/import/pga\" -H \"X-Admin-Token: <TOKEN>\" -F \"file=@${BACKEND_DIR}/paramPga.xlsx\""

echo "==> PGA kayit sayisi"
docker exec -i earthquakecheck-db psql -U postgres -d earthquake_check -c "SELECT COUNT(*) FROM earthquakecheck.pga_value;"

echo "Deploy tamamlandi."
