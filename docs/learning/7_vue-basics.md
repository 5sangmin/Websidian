# Vue Basics

## Purpose

이 문서는 Websidian을 구현하기 전에 알아두면 좋은 Vue의 기본 개념을 정리한다.

설명 방식은 가능한 한 다음 흐름을 따른다.

- 기존에는 무엇이 문제였는가
- 그 문제를 해결하기 위해 어떤 개념이 등장했는가
- 그 개념은 실제로 무엇을 해결하는가
- Websidian에서는 이 개념이 어디에 연결되는가

이 문서는 학습 문서다. 실제 Websidian 프론트엔드 구조 방향은 `docs/frontend/app-structure.md`, `docs/adr/0002-use-vue-spring-boot.md`, `docs/operations/development-guide.md` 문서를 기준으로 본다. 

## 1. 왜 Vue를 먼저 이해해야 하는가

웹 프론트엔드를 처음 접하면 “결국 HTML, CSS, JavaScript로 화면을 만들면 되는 것 아닌가?”라고 생각하기 쉽다. 실제로 아주 단순한 페이지는 이 세 가지만으로도 충분히 만들 수 있다. 

하지만 Websidian처럼:

- Vault 리스트와 상세 화면을 오가고
- 문서 본문을 여러 타입으로 렌더링하고
- 내부 링크를 따라 이동하고
- 로딩/에러/빈 상태를 모두 처리해야 하는

프론트엔드 앱이 되면, 단순히 “스크립트 몇 줄로 DOM을 건드리는” 수준으로는 유지보수가 점점 어려워진다. Vue는 이런 상황에서 프론트엔드 코드를 덜 엉키게 만들기 위한 프레임워크다. 

## 2. DOM은 무엇이고, 왜 중요한가

### 기존 문제

Vue를 이해하기 전에 먼저 “브라우저에서 화면이 어떻게 그려지는가”를 알아야 한다. 그 중심 개념이 **DOM(Document Object Model)** 이다.

많은 초보자가 “HTML을 쓰면 브라우저가 알아서 화면을 그려주겠지” 정도로만 이해하지만, 실제로는 이렇게 변환된다.

- HTML 문서는 텍스트일 뿐이다.
- 브라우저는 HTML을 읽고, 문서 구조를 트리(tree) 형태의 **DOM** 으로 만든다.
- DOM 트리의 각 노드가 화면에 보이는 요소(엘리먼트)가 된다.

쉽게 말하면:

- HTML: 설계도(텍스트)
- DOM: 브라우저 안에 만들어진 실제 구조물(객체 트리)
- 화면: DOM을 그려낸 최종 결과

JavaScript가 직접 다루는 대상은 HTML 텍스트가 아니라 이 **DOM 트리**다.

### DOM을 직접 다룰 때의 패턴

전통적인 방식에서는 보통 이렇게 코드를 쓴다.

- `document.getElementById(...)` 로 특정 요소를 찾고
- `element.textContent` 를 수정하고
- `element.classList.add/remove` 로 스타일 상태를 바꾸고
- `element.appendChild` 나 `innerHTML` 로 DOM 조각을 넣고 뺀다

즉, 상태가 바뀔 때마다 개발자가 “어떤 DOM을 어떻게 수정해야 하는지”를 하나하나 적어 줘야 한다.

### 왜 문제가 되는가

이 방식은 화면이 단순할 때는 괜찮지만, 상태와 UI가 많아질수록 문제가 생긴다.

- 문서 제목을 바꿀 때, 화면 상단 제목·탭 제목·브레드크럼 등 여러 곳을 함께 바꿔야 할 수 있다.
- 로딩 상태 → 성공 상태 → 에러 상태로 바뀔 때, 보여줄 요소와 숨길 요소를 일일이 관리해야 한다.
- 문서 타입이 markdown인지 html인지에 따라 다른 렌더러를 써야 한다.

DOM 직접 조작 방식에서는 이런 상태 전환을 모두 사람이 기억해서 코드를 써야 한다. 화면이 복잡해질수록 “어디를 빠뜨렸는지” 찾기 어려워진다.

## 3. Vue는 무엇을 해결하려고 등장했는가

### 기존 문제

정리하면, 전통적인 DOM 중심 방식은 다음 문제를 반복해서 만들었다.

- 화면과 데이터(상태)가 분리되어 있지 않다.
- 특정 상태에서 화면이 어떻게 되어야 하는지 코드를 읽어도 한눈에 보이지 않는다.
- UI 조각을 재사용하기 어렵다.
- DOM 조작 코드가 여러 곳에 흩어져 버그가 생기기 쉽다.

### 등장한 개념

Vue 같은 프론트엔드 프레임워크는 이런 문제를 줄이기 위해 크게 세 가지를 제공한다.

1. **컴포넌트(Component)**: 화면을 작은 단위로 나누는 방법
2. **반응성(Reactivity)**: 상태가 바뀌면 자동으로 UI가 갱신되는 구조
3. **선언적 UI(Declarative UI)**: “어떤 상태에서 어떤 화면을 보여줄지”를 템플릿으로 표현하는 방식

즉, “DOM을 어떻게 고칠까”에서 “상태를 정의하고, 그에 따른 UI를 선언하자”로 관점을 바꾸는 것이다.

### Websidian에 대입

Websidian에서는 문서 상세 화면 하나만 봐도:

- 문서 헤더
- 태그 목록
- 본문 렌더러(markdown/html)
- 링크/임베드 영역
- 에러 및 빈 상태

같은 UI 조각이 있다. Vue를 쓰면 이것들을 각각 컴포넌트로 나누고, 상태에 따라 어떤 컴포넌트를 어떻게 보여줄지 선언적으로 관리할 수 있다. 

## 4. 컴포넌트는 왜 필요한가

### 기존 문제

하나의 HTML 파일이나 하나의 큰 JS 파일 안에 모든 DOM 조작과 이벤트 처리가 들어가면, 어느 순간부터는 “이 파일이 무슨 일을 하는지” 읽기 힘들어진다.

특히 Websidian처럼:

- 여러 문서 화면
- Vault 홈 화면
- 설정 화면
- 나중에는 그래프 화면

까지 생기면, 화면을 어떤 기준으로 나눌지 고민이 필요하다.

### 등장한 개념

**컴포넌트(Component)** 는 화면을 “기능적으로 의미 있는 조각”으로 쪼개는 단위다.

컴포넌트 하나는 보통 다음을 함께 가진다.

- 화면 구조(템플릿)
- 동작 로직(스크립트)
- 스타일(선택적으로)

예:

- `DocumentHeader.vue`: 제목, 태그, 상태 배지 등
- `MarkdownRenderer.vue`: markdown 본문을 HTML로 렌더링
- `VaultCard.vue`: Vault 요약 정보와 진입 링크

### Websidian에 대입

`app-structure.md` 는 전역 `components/` 와 feature 전용 `features/.../components/` 를 구분하라고 한다. Websidian에서는:

- `AppButton.vue`, `AppModal.vue` 같은 것은 전역 컴포넌트
- `DocumentHeader.vue`, `DocumentTagList.vue` 같은 것은 `document` feature 전용 컴포넌트

로 두는 것이 자연스럽다.  

이렇게 나누면 “문서 관련 코드가 어디에 모여 있는지”를 찾기 쉬워진다.

## 5. 상태(State)를 중심으로 생각해야 하는 이유

### 기존 문제

DOM을 직접 다루는 방식의 가장 큰 문제는, 실제로는 “화면 모양”보다 “지금 상태”가 더 중요함에도 불구하고, 코드가 상태보다 DOM 조작에 집중하게 된다는 점이다.

예를 들어 문서 페이지에는 다음과 같은 상태가 있다.

- `isLoading`: API 호출 중인가
- `error`: 에러가 났는가
- `document`: 문서 데이터가 있는가
- `document.type`: markdown인가 html인가
- `selectedVault`: 현재 vault 컨텍스트는 무엇인가

그런데 DOM 중심 코드에서는:

- 로딩이면 이 div를 보여주고 저 div를 숨기고…
- 에러면 다른 div를 보여주고…
- 데이터를 가져오면 제목/본문/태그 요소 텍스트를 각각 바꾸고…

같은 식으로 흩어진 조건문을 보게 된다.

### 등장한 개념

Vue에서는 **상태(State)** 를 명시적으로 두고, 상태에 따라 UI가 자동으로 정해지게 하는 방식을 택한다.

- 상태는 “지금 화면이 알고 있는 값”
- UI는 “이 값들이 이런 조합일 때 이렇게 보인다”를 정의한 것

즉,

- 로딩이면 로딩 컴포넌트를 렌더링
- 에러가 있으면 에러 컴포넌트를 렌더링
- 데이터가 있으면 실제 문서 뷰를 렌더링

하는 식이다.

### Websidian에 대입

문서 상세 페이지를 예로 들면 다음 상태가 필요할 수 있다.

- `isLoadingDocument`
- `documentError`
- `documentData`
- `isMarkdown` / `isHtml`

Vue에서는 이 값을 반응형 상태로 두고, 템플릿에서 `v-if`, `v-else`, `v-for` 같은 구문으로 화면을 선언한다. 이렇게 하면 상태 변경에 따라 DOM을 직접 건드리지 않아도 된다.

`app-structure.md` 도 서버 상태와 UI 상태를 혼동하지 말고, 화면별 상태는 composable이나 페이지에 두되, 전역 상태는 store로 구분하라고 권장한다. 

## 6. 반응성(Reactivity)은 어떻게 DOM 갱신 문제를 줄여주는가

### 기존 문제

DOM을 직접 다루는 코드에서 가장 자주 발생하는 버그는 “값은 바뀌었는데 화면이 안 바뀌는 경우”와 “한 곳만 바뀌고 나머지는 그대로인 경우”다.

- 내부 데이터 구조를 수정했지만
- 그에 따라 DOM을 갱신하는 코드를 빠뜨렸거나
- 두세 군데 중 한 군데만 고쳤을 때

문제가 생긴다.

### 등장한 개념

Vue의 **반응성(Reactivity)** 은 이 문제를 줄이기 위한 핵심 메커니즘이다.

개념적으로는 다음과 같다.

- 반응형 상태를 선언한다.
- 그 상태를 사용하는 템플릿을 선언한다.
- 상태가 바뀌면, Vue가 어떤 DOM을 다시 그려야 하는지 계산해서 알아서 갱신한다.

개발자는 “이 상태를 이렇게 바꾼다”까지만 직접 하고, 나머지 DOM 변경은 프레임워크에 맡긴다.

### Websidian에 대입

예를 들어 `useDocument()` composable이 다음 상태를 제공한다고 해 보자.

- `document`
- `isLoading`
- `error`

문서 페이지 템플릿에서는 단지:

- `v-if="isLoading"` → 로딩 컴포넌트
- `v-else-if="error"` → 에러 컴포넌트
- `v-else` → 실제 문서 뷰

를 선언해 두면 된다.

문서가 새로 로드되거나 에러가 생길 때, Vue는 DOM을 직접 건드릴 필요 없이 템플릿에 맞게 화면을 다시 그려 준다. 상태를 바꾸는 쪽과 화면 갱신을 담당하는 쪽이 분리되는 셈이다.

## 7. 선언적 UI와 템플릿은 왜 중요한가

### 기존 문제

전통적인 방식에서는 화면 업데이트를 이렇게 코딩한다.

- “이벤트가 발생하면 이 DOM을 찾아서 이 클래스를 붙이고…”
- “데이터 배열을 for문으로 돌면서 HTML 문자열을 조립하고…”
- “조건문에 따라 다른 HTML 조각을 붙인다…”

즉, “어떻게 그릴지” 과정을 하나하나 써야 한다.

### 등장한 개념

Vue는 템플릿 기반 **선언적 UI**를 제공한다. 선언적이라는 말은 “어떻게”보다 “무엇이 되어야 하는지”를 적는다는 뜻이다.

예를 들어:

- `v-if="isLoading"`: 로딩 상태면 이 블록을 보여준다.
- `v-for="tag in document.tags"`: 태그 목록을 반복해서 보여준다.
- `:class="{ active: isActive }"`: `isActive`가 true면 active 클래스를 붙인다.

개발자는 “이 조건에서 이 UI”를 선언하고, 중간에 DOM을 조립하는 세부 과정은 Vue에 맡긴다.

### Websidian에 대입

문서 상세 템플릿에는:

- 문서가 없을 때 보여줄 empty state
- 문서가 있을 때 헤더·본문·첨부 파일 영역
- 문서 타입에 따라 다른 렌더러

등을 선언적으로 표현하게 된다.

이 접근은 Websidian처럼 상태와 분기가 많은 화면을 “읽기 쉬운 코드”로 유지하는 데 큰 도움이 된다. 

## 8. Single File Component(SFC)는 왜 편리한가

### 기존 문제

HTML, CSS, JavaScript를 각기 다른 파일에 완전히 분리해 두면, 특정 화면 하나를 이해하기 위해 여러 파일을 계속 오가야 한다.

- 이 화면의 구조는 어디에 있지?
- 이 화면의 로직은 어디에 있지?
- 이 화면 전용 스타일은 어디에 있지?

라는 질문에 매번 답해야 한다.

### 등장한 개념

Vue의 **Single File Component(SFC)** 는 `.vue` 파일 하나 안에 템플릿, 스크립트, 스타일을 모아 두는 방식이다.

이 방식의 장점은:

- 관련 코드가 한 파일에 모인다.
- 컴포넌트 단위로 쉽게 이동·삭제·수정할 수 있다.
- 빌드 도구(vite 등)와 결합이 잘 된다.

### Websidian에 대입

Websidian에서 예를 들면:

- `DocumentPage.vue`: 라우트 단위 페이지
- `DocumentHeader.vue`: 문서 헤더 UI
- `MarkdownRenderer.vue`: markdown 렌더링
- `VaultHomePage.vue`: Vault 목록/진입점

같은 파일들이 각각 SFC로 존재하게 된다. 

이렇게 되면 “이 화면이 무엇을 하는지”를 알고 싶을 때 해당 `.vue` 파일 하나를 열어보면 되기 때문에, 혼자 개발하는 프로젝트에서도 맥락 복구가 훨씬 쉽다.

## 9. 라우터와 페이지는 왜 분리해야 하는가

### 기존 문제

웹 앱에는 URL 기반으로 여러 화면이 있다. 그런데 URL 매핑 로직, 레이아웃 선택, 데이터 로딩, 렌더링 로직이 모두 한 파일에 섞이면 읽기 어렵고 테스트도 힘들다.

### 등장한 개념

Vue에서는 보통 **Vue Router**를 사용해 라우팅을 담당하고, 각 라우트에 대응하는 **페이지 컴포넌트**를 둔다.

- 라우터: URL → 어느 페이지를 열지
- 페이지: 여러 feature를 조합해 최상위 화면을 만든다

### Websidian에 대입

`app-structure.md`의 권장 구조를 그대로 옮기면: 

- `src/router/`: 라우트 정의, 가드, lazy loading 정책
- `src/pages/`: 라우트 단위 페이지 컴포넌트

예시 라우트:

- `/` – 홈 또는 Vault 선택 화면
- `/v/:vaultSlug` – Vault 홈
- `/v/:vaultSlug/docs/:documentSlug` – 문서 상세
- `/admin/vaults/:vaultId` – Vault 설정/관리

각 페이지는 주로:

- URL 파라미터 파싱
- 페이지 수준 로딩/에러 처리
- 어떤 feature view를 조합할지 결정

을 담당하고, 세부 문서 렌더링은 `features/document/views/DocumentView.vue` 같은 곳에 위임한다. 

## 10. Composable은 왜 필요해졌는가

### 기존 문제

컴포넌트 기반으로 나누더라도, 여러 컴포넌트에서 공통으로 쓰이는 로직이 생긴다.

예:

- 문서 데이터를 API로 불러오는 로직
- Vault 컨텍스트를 추적하는 로직
- 내부 링크를 파싱하는 로직
- API의 로딩/에러 상태를 관리하는 패턴

이걸 컴포넌트마다 복붙하면 유지보수가 어려워진다.

### 등장한 개념

Vue 3에서는 이런 문제를 해결하기 위해 **Composable** 패턴이 널리 쓰인다. composable은 “재사용 가능한 상태 + 로직 묶음”이라고 보면 된다.

예:

- `useDocument(documentId)`
- `useVaultContext()`
- `useDocumentLinks(document)`
- `useFetch(apiCall)`

### Websidian에 대입

`app-structure.md`도 feature 내부에 `composables/` 디렉터리를 두는 것을 권장한다. 

예를 들어:

```text
features/document/
  composables/
    useDocument.ts
    useDocumentLinks.ts
    useDocumentEmbeds.ts
```

같은 구조가 자연스럽다. 이렇게 하면 문서 조회나 링크 처리 로직을 페이지/컴포넌트에서 적당히 분리하면서도, document feature 안에 모아 둘 수 있다.

## 11. Store는 언제, 왜 쓰는가

### 기존 문제

몇몇 상태는 특정 페이지나 컴포넌트에만 해당되지만, 어떤 상태는 앱 전체에서 공유해야 한다.

예:

- 로그인/세션 정보
- 현재 선택된 Vault 컨텍스트
- 전역 다크 모드, 토스트 메시지 등 UI 상태

이런 것을 props와 이벤트만으로 주고받으면 구조가 복잡해질 수 있다.

### 등장한 개념

그래서 Vue 생태계에서는 보통 **Pinia** 같은 전역 상태 관리(store)를 사용한다. store는:

- 여러 페이지/컴포넌트에서 공유되는 상태
- 앱 전체 수명 동안 유지되는 컨텍스트

를 관리하는 데 적합하다. 

### Websidian에 대입

Websidian에서 store 후보는 대략 다음과 같다. 

- `useSessionStore`: 로그인/사용자 정보
- `useUiStore`: 전역 UI 설정, 테마, 토스트 등
- `useVaultContextStore`: 현재 선택된 Vault, 최근 사용 Vault 등

반면, 특정 문서 한 건의 상세 데이터처럼 “한 화면에서만 필요한 값”은 굳이 store에 올리지 않고 composable이나 페이지 로컬 상태로 두는 편이 낫다. `app-structure.md`도 전역 store는 최소화하라고 명시한다. 

## 12. 왜 feature 중심 구조가 중요한가

### 기존 문제

Vue 프로젝트를 처음 만들면 다음과 같은 구조를 많이 본다.

```text
components/
views/
store/
services/
utils/
```

겉으로 보기에는 단순하지만, 시간이 지나면 `document`, `vault`, `file` 관련 코드가 전역 폴더 곳곳에 흩어진다. 그러면 “문서 기능 전체를 보고 싶을 때” 한 곳에서 보기 어렵다.

### 등장한 개념

Websidian은 이 문제를 피해 가기 위해 프론트엔드도 **feature 중심 구조**를 택한다. 

즉:

- 먼저 `document`, `vault`, `file`, `graph`, `auth` 같은 도메인/기능 단위를 나누고
- 그 안에 `api`, `components`, `composables`, `model`, `views` 를 둔다.

### 권장 구조

`app-structure.md`의 예시를 조금 단순화하면 다음과 같다. 

```text
src/
  app/
  pages/
  features/
    document/
      api/
      components/
      composables/
      model/
      views/
    vault/
      api/
      components/
      composables/
      model/
      views/
  components/
  layouts/
  router/
  stores/
  api/
  styles/
```

이렇게 하면 “문서 관련 프론트 코드”는 대부분 `features/document` 아래에서 찾을 수 있다. 이는 백엔드의 도메인 중심 모듈 구조와도 잘 대응된다. 

## 13. Vue는 Websidian에서 어떤 역할을 맡는가

ADR-0002는 Websidian의 기본 스택을 “Frontend: Vue.js, Backend: Spring Boot”로 고정하고 각자의 역할을 다음처럼 나눈다. 

- Vue: 라우팅, 문서 뷰어, 그래프 UI, HTML 임베드, 관리자 화면
- Spring Boot: REST API, 인증/권한, Markdown 처리, DB/MinIO 연동, 검색 기초 로직

즉, Vue는 단순 HTML 템플릿 도구가 아니라:

- URL과 화면 관계를 관리하고
- 문서 데이터를 받아 렌더링하고
- Vault와 문서 컨텍스트를 유지하며
- 앞으로 추가될 그래프/검색 UI를 연결하는

**문서 경험 전체의 프론트엔드 실행 환경**이다. 

## 14. Vue를 처음 배울 때 자주 생기는 오해

1. **Vue는 DOM 조작을 쉽게 해 주는 도구다?**  
   → Vue의 핵심은 DOM을 직접 덜 만지게 하는 것이다. 상태와 템플릿을 선언하면 DOM 갱신은 프레임워크가 맡는다.

2. **모든 상태를 전역 store에 넣어야 한다?**  
   → Websidian에서는 전역 store는 최소화하고, feature와 페이지 수준에서 해결 가능한 상태는 그 안에 두는 것을 권장한다. 

3. **공유 가능한 것은 다 `components/`에 넣어야 한다?**  
   → 도메인에 강하게 묶인 UI는 feature 내부에 두는 편이 더 낫다. 재사용성보다 “어떤 기능에 속하느냐”가 우선이다. 

4. **페이지에 API 호출, 상태 관리, 렌더링을 다 넣어도 된다?**  
   → 페이지는 라우트 진입과 조합 역할에 집중하고, API와 상태, 렌더링은 composable과 feature view로 나누는 편이 Websidian이 원하는 구조에 더 가깝다. 

## 15. Websidian에서 Vue 흐름은 어떻게 보이는가

문서 상세 화면을 예로 들어, Vue의 역할을 정리하면 다음과 같다. 

1. 사용자가 `/v/:vaultSlug/docs/:documentSlug` URL로 들어온다.
2. Vue Router가 이 URL을 `DocumentPage.vue` 같은 페이지에 매핑한다. 
3. 페이지 컴포넌트는 `useDocument()` 같은 composable을 호출해 문서 데이터를 로드한다. 
4. composable 내부 API 함수가 Spring Boot 백엔드의 REST API를 호출한다. 
5. 반응형 상태(`isLoading`, `error`, `document`)가 업데이트된다.
6. 템플릿은 상태에 따라 로딩/에러/실제 문서 뷰를 자동으로 전환한다.
7. 문서 타입에 따라 `MarkdownRenderer` 또는 `HtmlDocumentFrame` 컴포넌트를 선택해 렌더링한다. 
8. 내부 링크 클릭 시 라우터가 URL을 바꾸고, 같은 흐름이 반복된다.

이 한 줄기가 “DOM을 직접 조작하는 코드” 없이도 복잡한 문서 UI를 유지보수 가능한 형태로 만드는 Vue의 역할을 보여 준다.

## 16. 지금 단계에서 꼭 잡아야 할 핵심 문장

- DOM은 브라우저가 HTML을 읽어 만든 문서 구조 트리이고, 전통적인 방식에서는 JavaScript가 이 DOM을 직접 조작했다.
- Vue는 DOM 조작 자체보다 상태와 컴포넌트, 템플릿에 집중하게 만들어 프론트엔드 구조를 단순하게 한다.
- 컴포넌트는 화면을 기능적으로 나누는 기본 단위이고, Websidian에서는 도메인별 feature 안에 모인 컴포넌트가 특히 중요하다. 
- 반응성과 선언적 템플릿 덕분에 상태가 바뀌면 DOM 변경을 직접 코딩하지 않아도 된다.
- 페이지와 라우터는 URL 진입과 조합을 담당하고, 실제 기능 로직은 feature와 composable이 담당한다. 
- Websidian은 Vue를 “문서 뷰어 UI” 수준이 아니라, Vault/Document/Graph를 아우르는 프론트엔드 애플리케이션의 기반으로 선택했다. 

## 17. 다음 문서로 이어지는 연결

이 문서를 이해한 다음에는 다음 주제가 자연스럽게 이어진다.

- `spring-basics.md`: Vue가 호출하는 API를 백엔드에서 어떻게 처리하는지, controller/service/repository 관점에서 연결해 보면 좋다.
- `http-and-rest.md`: Vue가 어떤 규칙(HTTP 메서드, 상태 코드, REST 자원)으로 Spring Boot와 통신하는지 더 구체적으로 이해할 수 있다. 
- `layered-architecture.md`: 프론트엔드/백엔드 모두에서 “DOM/HTTP/도메인/저장소”를 어떻게 층으로 나눌지 함께 보는 데 도움이 된다. 
- `docker-basics.md`: Vue 개발 서버와 Spring Boot, PostgreSQL, MinIO를 로컬과 Compose로 함께 띄우는 흐름을 이해할 수 있다.
- `app-structure.md`: 여기서 설명한 개념들이 실제 폴더 구조와 파일 이름으로 어떻게 구체화되어 있는지 확인할 수 있다. 