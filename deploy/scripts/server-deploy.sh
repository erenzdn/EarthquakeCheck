#!/usr/bin/env bash
set -euo pipefail

# Sunucuda calistirin (root veya docker yetkili kullanici):
#   cd /var/opt/docker/EarthquakeCheck
#   git pull --ff-only
#   chmod +x deploy/scripts/server-deploy.sh
#   ./deploy/scripts/server-deploy.sh

ROOT_DIR="/var/opt/docker"
BACKEND_DIR="${ROOT_DIR}/EarthquakeCheck"
FRONTEND_DIR="${ROOT_DIR}/earthquake-check-frontend"
NGINX_CONF="${ROOT_DIR}/ngnix_proxy/conf.d/earthquakecheck.conf"
NGINX_GATEWAY="nginx-gateway"
ENV_FILE="${BACKEND_DIR}/.env"
API_BASE="https://earthquakecheck.mehmeterenozden.com"
PGA_FILE="${BACKEND_DIR}/paramPga.xlsx"

log() { printf '==> %s\n' "$*"; }
fail() { printf 'HATA: %s\n' "$*" >&2; exit 1; }

require_cmd() {
  command -v "$1" >/dev/null 2>&1 || fail "$1 bulunamadi."
}

require_cmd docker
require_cmd openssl
require_cmd curl

# --- 1) .env hazirligi ---
log ".env kontrolu"
cd "${BACKEND_DIR}"

if [[ ! -f "${ENV_FILE}" ]]; then
  log ".env yok; .env.example sablonundan olusturuluyor"
  cp .env.example "${ENV_FILE}"
  DB_PASS="$(openssl rand -base64 32 | tr -d '/+=' | head -c 32)"
  ADMIN_TOKEN="$(openssl rand -base64 48 | tr -d '\n')"
  sed -i "s|<guclu-rastgele-sifre>|${DB_PASS}|g" "${ENV_FILE}"
  sed -i "s|<guclu-rastgele-token-min-32-karakter>|${ADMIN_TOKEN}|g" "${ENV_FILE}"
  if ! grep -q '^RATE_LIMIT_TRUSTED_PROXY_IPS=' "${ENV_FILE}"; then
    echo 'RATE_LIMIT_TRUSTED_PROXY_IPS=127.0.0.1,::1,10.0.0.0/8,172.16.0.0/12,192.168.0.0/16' >> "${ENV_FILE}"
  fi
  chmod 600 "${ENV_FILE}"
  log ".env olusturuldu (chmod 600). APP_ADMIN_TOKEN kaydedildi."
else
  chmod 600 "${ENV_FILE}"
fi

required_vars=(POSTGRES_DB POSTGRES_USER POSTGRES_PASSWORD DB_URL DB_USERNAME DB_PASSWORD APP_ADMIN_TOKEN CORS_ALLOWED_ORIGINS RATE_LIMIT_TRUSTED_PROXY_IPS)
for var_name in "${required_vars[@]}"; do
  if ! grep -q "^${var_name}=" "${ENV_FILE}"; then
    fail "${ENV_FILE} icinde ${var_name} eksik."
  fi
done

token_len="$(grep '^APP_ADMIN_TOKEN=' "${ENV_FILE}" | cut -d= -f2- | wc -c)"
if [[ "${token_len}" -lt 33 ]]; then
  fail "APP_ADMIN_TOKEN en az 32 karakter olmali."
fi

ADMIN_TOKEN="$(grep '^APP_ADMIN_TOKEN=' "${ENV_FILE}" | cut -d= -f2-)"

# --- 2) docker-compose dogrulama ---
log "docker compose config"
docker compose --env-file "${ENV_FILE}" config >/dev/null

# --- 3) nginx-gateway entegrasyonu ---
log "nginx-gateway config"
if [[ -f "${NGINX_CONF}" ]]; then
  if ! grep -q 'CF-Connecting-IP' "${NGINX_CONF}"; then
    sed -i '/location \/api\//,/}/ s/proxy_set_header X-Forwarded-Proto \$scheme;/proxy_set_header X-Forwarded-Proto \$scheme;\n        proxy_set_header CF-Connecting-IP \$http_cf_connecting_ip;/' "${NGINX_CONF}"
    log "CF-Connecting-IP eklendi"
  fi
  if ! grep -q 'earthquakecheck-frontend:80' "${NGINX_CONF}"; then
    fail "nginx conf frontend upstream kontrol edin: earthquakecheck-frontend:80"
  fi
  if ! grep -q 'earthquakecheck-app:8081' "${NGINX_CONF}"; then
    fail "nginx conf API upstream kontrol edin: earthquakecheck-app:8081"
  fi
  docker exec "${NGINX_GATEWAY}" nginx -t
  docker exec "${NGINX_GATEWAY}" nginx -s reload
else
  log "UYARI: ${NGINX_CONF} bulunamadi; deploy/nginx/earthquakecheck-gateway.conf dosyasini kopyalayin"
  if [[ -f "${BACKEND_DIR}/deploy/nginx/earthquakecheck-gateway.conf" ]]; then
    cp "${BACKEND_DIR}/deploy/nginx/earthquakecheck-gateway.conf" "${NGINX_CONF}"
    docker exec "${NGINX_GATEWAY}" nginx -t
    docker exec "${NGINX_GATEWAY}" nginx -s reload
  fi
fi

# --- 4) Build ve deploy (db + app) ---
log "Backend build (app)"
docker compose --env-file "${ENV_FILE}" build --no-cache app
docker compose --env-file "${ENV_FILE}" up -d db app

log "App loglari (ProductionSecurityValidator, Flyway V5)"
sleep 15
docker compose --env-file "${ENV_FILE}" logs app --tail 120 | grep -iE 'ProductionSecurityValidator|flyway|V5|Started EarthquakeCheck' || true

if ! docker compose --env-file "${ENV_FILE}" ps app | grep -q 'Up'; then
  docker compose --env-file "${ENV_FILE}" logs app --tail 80
  fail "earthquakecheck-app ayaga kalkmadi."
fi

# --- 5) Guvenlik dogrulama checklist ---
log "Guvenlik checklist (curl)"
check_http() {
  local label="$1"
  local expected="$2"
  shift 2
  local code
  code="$(curl -sS -o /dev/null -w '%{http_code}' "$@" || echo '000')"
  if [[ "${code}" == "${expected}" ]]; then
    printf '  [OK] %s -> %s\n' "${label}" "${code}"
  else
    printf '  [FAIL] %s -> %s (beklenen %s)\n' "${label}" "${code}" "${expected}"
    CHECK_FAILED=1
  fi
}

CHECK_FAILED=0
check_http "POST /api/building/evaluate" "200" \
  -X POST "${API_BASE}/api/building/evaluate" \
  -H "Content-Type: application/json" \
  -d '{"yearBuilt":2000,"floorCount":5,"address":"Test"}'

check_http "POST /api/import/pga (token yok)" "403" \
  -X POST "${API_BASE}/api/import/pga"

check_http "GET /swagger-ui/index.html" "404" \
  "${API_BASE}/swagger-ui/index.html"

check_http "GET /api/test/location" "404" \
  "${API_BASE}/api/test/location?address=test"

check_http "POST /api/contact/messages" "201" \
  -X POST "${API_BASE}/api/contact/messages" \
  -H "Content-Type: application/json" \
  -d '{"fullName":"Deploy Test","email":"deploy@test.com","subject":"Test","message":"Guvenlik checklist mesaji"}'

check_http "GET /api/contact/admin/messages (token yok)" "403" \
  "${API_BASE}/api/contact/admin/messages"

check_http "GET /api/contact/admin/messages (token var)" "200" \
  -H "X-Admin-Token: ${ADMIN_TOKEN}" \
  "${API_BASE}/api/contact/admin/messages"

if [[ "${CHECK_FAILED}" -ne 0 ]]; then
  log "UYARI: Bazi guvenlik kontrolleri basarisiz (frontend/nginx henuz hazir olmayabilir)"
fi

# --- 6) PGA verisi import ---
if [[ -f "${PGA_FILE}" ]]; then
  log "PGA import"
  curl -sS -X POST "${API_BASE}/api/import/pga" \
    -H "X-Admin-Token: ${ADMIN_TOKEN}" \
    -F "file=@${PGA_FILE}" \
    -w "\nPGA import HTTP %{http_code}\n" -o /tmp/pga-import-response.json || true
  PGA_COUNT="$(docker exec earthquakecheck-db psql -U postgres -d earthquake_check -tAc "SELECT COUNT(*) FROM earthquakecheck.pga_value;" 2>/dev/null || echo 0)"
  log "PGA kayit sayisi: ${PGA_COUNT}"
  if [[ "${PGA_COUNT}" -eq 0 ]]; then
    log "UYARI: PGA tablosu bos; import loglarini kontrol edin"
  fi
else
  log "UYARI: ${PGA_FILE} bulunamadi; PGA import atlandi"
fi

# --- 7) Frontend container ---
log "Frontend build + deploy"
if [[ -d "${FRONTEND_DIR}" ]]; then
  docker compose --env-file "${ENV_FILE}" build --no-cache frontend
  docker compose --env-file "${ENV_FILE}" up -d frontend
else
  fail "Frontend dizini bulunamadi: ${FRONTEND_DIR}"
fi

log "Ag dogrulama"
docker inspect earthquakecheck-app --format '{{range $k,$v := .NetworkSettings.Networks}}{{$k}} {{end}}'
docker inspect earthquakecheck-frontend --format '{{range $k,$v := .NetworkSettings.Networks}}{{$k}} {{end}}'

log "Container durumu"
docker compose --env-file "${ENV_FILE}" ps

log "Deploy tamamlandi."
