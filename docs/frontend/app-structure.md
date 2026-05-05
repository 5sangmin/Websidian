# Frontend App Structure

## Purpose

이 문서는 Websidian 프론트엔드 애플리케이션의 구조 원칙과 폴더 구성을 정의한다.
목적은 Vue 프로젝트를 단순히 컴포넌트 모음으로 키우는 것이 아니라, 기능 단위와 화면 단위를 분리해 점진적으로 확장 가능한 구조를 만드는 것이다.

Websidian 프론트엔드는 단순 블로그 UI가 아니다.
문서 뷰어, Vault 탐색, Markdown/HTML 렌더링, 내부 링크 이동, 임베드, 관리자 기능, 장기적으로 그래프 뷰까지 포함하는 애플리케이션이다.
따라서 초기부터 “어디에 무엇을 둘지” 기준을 정하는 것이 중요하다.

## Structure Principles

### 1. 화면보다 기능을 우선한다
페이지는 사용자 진입점이지만, 실제 복잡도는 기능에서 나온다.
따라서 문서, Vault, graph, auth 같은 기능 단위를 별도 경계로 본다.

### 2. 전역 공용 코드와 도메인 코드를 구분한다
버튼, 레이아웃, 포맷터, 공통 API 클라이언트 같은 것은 공용 영역에 두고, 특정 비즈니스 개념에 강하게 묶인 코드는 feature 영역에 둔다.

### 3. 얕고 명확한 폴더 구조를 유지한다
너무 많은 중첩과 추상화는 초기 프로젝트에서 오히려 부담이 된다.
초기에는 단순하게 두되, 기능 경계만 명확히 한다.

### 4. 라우팅과 렌더링 책임을 분리한다
페이지 컴포넌트는 라우트 진입과 화면 조합을 담당하고, 실제 문서 렌더링 로직은 feature 내부 컴포넌트가 맡는다.

## Recommended Top-Level Structure

```text
frontend/
├── public/
├── src/
│   ├── app/
│   ├── pages/
│   ├── features/
│   ├── components/
│   ├── layouts/
│   ├── router/
│   ├── stores/
│   ├── api/
│   ├── types/
│   ├── composables/
│   ├── utils/
│   ├── assets/
│   └── styles/
├── package.json
└── vite.config.ts
```

## Directory Responsibilities

### `src/app/`
앱 초기화와 전역 설정을 둔다.

포함 대상:
- 앱 엔트리 설정
- 플러그인 등록
- 전역 에러 핸들링
- 테마/설정 초기화
- 환경별 bootstrap 코드

예시:
- `main.ts`
- `providers.ts`
- `config.ts`

### `src/pages/`
라우트 단위 페이지 컴포넌트를 둔다.

역할:
- URL에 대응되는 최상위 화면 제공
- 여러 feature를 조합
- 페이지 수준 로딩/에러 처리

예시:
- `VaultHomePage.vue`
- `DocumentPage.vue`
- `VaultSettingsPage.vue`
- `NotFoundPage.vue`

규칙:
- 페이지는 비즈니스 로직을 과도하게 가지지 않는다.
- 데이터 로딩은 feature composable 또는 store에 위임한다.

### `src/features/`
Websidian의 핵심 기능 단위를 둔다.

초기 후보:
- `vault/`
- `document/`
- `file/`
- `graph/`
- `tag/`
- `auth/`

예시 구조:
```text
features/
├── vault/
│   ├── api/
│   ├── components/
│   ├── composables/
│   ├── model/
│   └── views/
├── document/
├── file/
└── graph/
```

설명:
- `api/`: 해당 기능의 API 호출
- `components/`: 해당 기능 전용 UI 컴포넌트
- `composables/`: 상태 및 로직 재사용 단위
- `model/`: feature 전용 타입 및 변환 로직
- `views/`: 페이지가 아닌 feature 조합 뷰

### `src/components/`
전역 재사용 UI 컴포넌트를 둔다.

예시:
- `AppButton.vue`
- `AppModal.vue`
- `AppBreadcrumb.vue`
- `AppEmptyState.vue`

규칙:
- 특정 도메인 개념에 강하게 묶인 컴포넌트는 여기에 두지 않는다.
- 문서 전용 렌더러나 Vault 카드 같은 것은 feature로 보낸다.

### `src/layouts/`
레이아웃 컴포넌트를 둔다.

예시:
- `DefaultLayout.vue`
- `AdminLayout.vue`
- `ReaderLayout.vue`

역할:
- 헤더, 사이드바, 본문 배치
- 읽기 화면과 관리 화면의 공통 골격 분리

### `src/router/`
Vue Router 설정을 둔다.

포함 대상:
- route definitions
- route guards
- lazy loading 정책
- 라우트 메타 정보

예시:
- `index.ts`
- `guards.ts`

초기 라우트 예시:
- `/`
- `/v/:vaultSlug`
- `/v/:vaultSlug/docs/:documentSlug`
- `/admin/vaults/:vaultId`

## Feature Focus: Document

Websidian 프론트엔드에서 가장 중요한 feature는 document다.

`features/document/` 안에 들어갈 가능성이 큰 요소:
- 문서 메타데이터 조회
- Markdown 렌더링
- HTML 문서 표시
- 내부 링크 처리
- 임베드 블록 처리
- 백링크/관련 문서 요약
- 문서 상태 배지

예시 구조:
```text
features/document/
├── api/
│   └── documentApi.ts
├── components/
│   ├── DocumentHeader.vue
│   ├── MarkdownRenderer.vue
│   ├── HtmlDocumentFrame.vue
│   ├── DocumentEmbed.vue
│   └── DocumentTagList.vue
├── composables/
│   ├── useDocument.ts
│   ├── useDocumentLinks.ts
│   └── useDocumentEmbeds.ts
├── model/
│   ├── document.types.ts
│   └── document.mapper.ts
└── views/
    └── DocumentView.vue
```

## Feature Focus: Vault

`features/vault/`는 저장소 단위 컨텍스트를 담당한다.

포함 대상:
- Vault 요약 카드
- Vault 메타데이터
- 대표 문서 정보
- Vault 단위 문서 목록
- 공개 범위 표시

이 feature는 document feature와 밀접하지만, 책임은 분리한다.

## State Management

초기 상태 관리는 Pinia 중심으로 구성하는 것이 적절하다.

권장 원칙:
- 서버 상태와 UI 상태를 혼동하지 않는다.
- API에서 가져온 데이터 캐시는 기능 단위 composable 또는 store로 관리한다.
- 전역 store는 최소화한다.
- 문서 상세 같은 화면 전용 상태는 페이지나 feature composable에 남긴다.

초기 store 후보:
- `useSessionStore`
- `useUiStore`
- `useVaultContextStore`

주의:
- 모든 것을 store에 넣지 않는다.
- 문서 단건 조회 결과는 필요 시 composable로 충분하다.

## API Layer

`src/api/`는 공통 API 기반 코드를 둔다.

예시:
- `httpClient.ts`
- `apiError.ts`
- `request.ts`

feature별 실제 API 함수는 각 feature 내부 `api/` 폴더에 둔다.

예:
- 공통: base URL, auth header, error normalization
- feature: `getDocumentById`, `listVaultDocuments`

## Types and Models

`src/types/`에는 전역 공유 타입을 둔다.
feature 내부 전용 타입은 feature 폴더 안에 둔다.

전역 공유 타입 예시:
- pagination
- api response envelope
- common id/value types

문서 전용 타입은 `features/document/model/`로 보낸다.

## Styling

스타일링 방식은 후속 결정이 필요하지만, 구조 관점에서는 아래 원칙을 따른다.

- 전역 스타일은 `src/styles/`
- feature 전용 스타일은 feature 내부에 가깝게 둔다
- 디자인 시스템이 생기면 공용 컴포넌트와 토큰을 분리한다

## What Not To Do

초기 단계에서 피할 구조:

- 모든 컴포넌트를 `components/`에 몰아넣기
- `helpers`, `common`, `misc` 같은 모호한 폴더 남발
- 너무 이른 shared abstraction 도입
- 페이지 파일에 API 호출, 상태 관리, 렌더링, 변환 로직을 모두 몰아넣기

## Recommended First Cut

MVP 기준으로는 아래 정도만 먼저 만들면 충분하다.

- `app/`
- `pages/`
- `features/document/`
- `features/vault/`
- `components/`
- `router/`
- `api/`
- `styles/`

graph, auth, tag feature는 실제 필요가 생길 때 추가한다.

## Summary

Websidian 프론트엔드는 페이지 중심이 아니라 기능 중심 구조를 기본으로 삼는다.
핵심은 `document`와 `vault` feature를 중심으로 앱을 조직하고, 공용 UI와 전역 설정은 분리하며, 페이지는 조합자 역할만 하도록 유지하는 것이다.