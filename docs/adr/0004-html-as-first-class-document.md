# ADR-0004: HTML을 내부 문서 노드로 취급한다

## Status
Accepted

## Context
Websidian의 주요 목표 중 하나는 “Markdown뿐 아니라 HTML도 Obsidian 노트처럼 연결·임베드·탐색 가능하게 만드는 것”이다.

- Obsidian은 Markdown 내부 링크와 임베드에는 강하지만, HTML 파일은 같은 수준의 내부 문서로 다루기 어렵다.
- 사용자는 HTML을 단지 첨부하는 것이 아니라, Markdown 문서와 동일한 문서 그래프 안에서 링크하고, 임베드하고, 백링크/그래프에 포함되기를 원한다.

한편 Markdown도 결국 HTML로 렌더링되어 사용자에게 보인다.
이 점을 고려하면, Markdown과 HTML은 저장 형식은 다르지만, 서비스 입장에서는 모두 “렌더 가능한 문서 노드”라고 볼 수 있다.

## Decision
HTML 파일을 단순 첨부파일이 아니라 **문서 그래프의 1급 노드**로 취급한다.

구체적으로:

- documents 계층에서 Markdown 문서와 HTML 문서를 모두 “문서”로 취급한다.
- 링크는 파일 경로/확장자 기준이 아니라 문서 식별자(canonical URL) 기준으로 관리한다.
- Markdown 문서에서 HTML 문서로 내부 링크 및 임베드가 가능해야 한다.
- HTML 문서도 백링크, 그래프, 관련 문서 계산 대상에 포함한다.
- 렌더링 시 문서 타입이 md면 Markdown 렌더러, html이면 iframe/HTML 렌더러를 사용한다.

## Consequences
장점:

- Markdown과 HTML을 같은 문서 그래프 안에서 자연스럽게 섞어 쓸 수 있다.
- HTML 기반 데모/예제를 문서 시스템의 일부로 포함시키기 쉬워진다.

단점:

- HTML 렌더링에 대한 보안 고려(iframe sandbox, CSP 등)가 필요하다.
- 문서 타입별 렌더러 분기, 임베드 시 postMessage 연동 같은 추가 설계가 필요하다.

Websidian의 차별점이 여기서 나오므로, 이 복잡도를 받아들이고 HTML을 1급 문서 노드로 취급하기로 한다.