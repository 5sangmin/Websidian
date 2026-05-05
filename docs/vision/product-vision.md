# Product Vision

## Product Name

Websidian

## Vision Statement

Websidian은 Obsidian Vault를 GitHub Repository처럼 관리하면서, Markdown과 HTML 문서를 웹에서 함께 링크·임베드·탐색할 수 있게 만드는 문서 플랫폼이다.

## Problem

Obsidian은 개인 문서 작성과 지식 관리에 매우 강력하지만, 웹 공유와 HTML 통합 측면에서는 한계가 있다.

특히 다음 문제가 있다.

- Markdown 노트는 내부 링크와 임베드가 잘 동작하지만, HTML 문서는 같은 수준의 내부 문서처럼 다루기 어렵다.
- 문서를 웹에 공개하려면 Obsidian 내부 경험과는 다른 별도 시스템이 필요해지는 경우가 많다.
- 개인 지식 저장소, 기술 문서, 데모 HTML, 첨부파일을 하나의 탐색 가능한 웹 문서 시스템으로 묶기 어렵다.

## Target User

초기 Websidian의 주요 사용자는 다음과 같다.

- Obsidian을 쓰고 있지만 웹 공유 환경이 필요한 개인 사용자
- Markdown 기반 기술 문서를 관리하는 개발자
- 문서와 HTML 데모를 함께 게시하고 싶은 개인 개발자
- 홈랩 환경에서 직접 문서 플랫폼을 운영해보고 싶은 사용자

## Core Value

Websidian이 제공해야 하는 핵심 가치는 다음과 같다.

- Obsidian 스타일 문서 구조를 웹에서 유지할 수 있다.
- Markdown과 HTML을 하나의 문서 그래프 안에서 함께 다룰 수 있다.
- Vault 단위로 문서를 관리하고 대표 문서부터 탐색을 시작할 수 있다.
- 개인 프로젝트와 홈랩 운영에 적합한 구조를 제공한다.

## Product Principles

제품은 다음 원칙을 따른다.

### 1. 문서 중심
화면보다 문서 모델과 탐색 경험이 우선이다.
문서, 링크, 임베드, 그래프가 제품의 핵심이다.

### 2. HTML도 문서다
HTML은 단순 첨부파일이 아니라 내부 문서 노드로 취급한다.

### 3. 작게 시작한다
처음부터 모든 Obsidian 기능을 재현하지 않는다.
읽기 중심 MVP부터 시작해 점진적으로 확장한다.

### 4. 문서와 설계를 함께 관리한다
Docs-as-Code와 ADR을 적극적으로 사용해 제품 방향과 구현 근거를 함께 축적한다.

## Differentiation

Websidian의 차별점은 다음과 같다.

- 단순 Markdown 블로그가 아니다.
- 단순 파일 브라우저도 아니다.
- Obsidian 스타일 문서 구조와 웹 문서 공유를 연결한다.
- Markdown과 HTML을 같은 탐색 체계 안에 포함시킨다.
- 개인 홈랩 기반 운영을 전제로 한 현실적인 구조를 지향한다.

## Success Criteria

초기 성공 기준은 다음과 같다.

- 사용자가 Vault를 생성하고 대표 문서를 지정할 수 있다.
- Markdown 문서를 읽기 좋게 웹에 렌더링할 수 있다.
- 내부 링크를 따라 문서를 탐색할 수 있다.
- HTML 문서를 내부 문서처럼 열고 연결할 수 있다.
- Markdown 안에 HTML 문서를 임베드할 수 있다.
- 최소한의 운영 구조(Docker, DB, 스토리지, 문서 구조)가 안정적으로 잡힌다.

## Long-Term Direction

장기적으로 Websidian은 아래 방향으로 확장할 수 있다.

- 백링크와 그래프 탐색 강화
- 문서 전문 검색 및 태그 탐색
- 버전 관리와 변경 이력
- 사용자/권한 관리
- HTML 임베드의 상호작용 강화
- Windows VM의 레거시 시스템과의 연동 가능성

## Summary

Websidian은 “Obsidian 문서를 웹에 게시한다”를 넘어서, “Markdown과 HTML을 함께 다루는 웹 기반 문서 플랫폼”을 지향한다.
핵심은 UI 장식이 아니라 문서 모델, 링크 구조, 임베드, 탐색 경험, 그리고 이를 뒷받침하는 현실적인 아키텍처다.