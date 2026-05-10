

작업	명령어
전체 시작	docker compose -f infra/compose/compose.dev.yml --env-file infra/compose/.env up -d --build
컨테이너 상태	docker compose -f infra/compose/compose.dev.yml --env-file infra/compose/.env ps
전체 중지	docker compose -f infra/compose/compose.dev.yml --env-file infra/compose/.env down
전체 중지 + 볼륨 삭제	docker compose -f infra/compose/compose.dev.yml --env-file infra/compose/.env down -v
Backend 재빌드	docker compose -f infra/compose/compose.dev.yml --env-file infra/compose/.env build backend
Backend 재시작	docker compose -f infra/compose/compose.dev.yml --env-file infra/compose/.env restart backend