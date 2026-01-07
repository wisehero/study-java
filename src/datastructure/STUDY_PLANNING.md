# Java 자료구조 밑바닥 학습 계획

> **목표**: 단순 사용법이 아닌 **내부 구현 원리**와 **시간/공간 복잡도** 중심의 깊이 있는 학습

---

## 📚 학습 로드맵 개요

```
Phase 1: 기초 자료구조 (Array, List)
    ↓
Phase 2: 스택 & 큐
    ↓
Phase 3: 해시 자료구조 ⭐
    ↓
Phase 4: 트리 자료구조
    ↓
Phase 5: 동시성 자료구조 ⭐
    ↓
Phase 6: 심화 주제
```

---

## Phase 1: 기초 자료구조

### 학습 목표

배열 기반 자료구조의 내부 동작 원리 이해

### 학습 내용

#### 1.1 Array (배열)

- 메모리 연속 할당 구조
- 인덱스 접근이 O(1)인 이유 (시작 주소 + offset 계산)
- 배열 복사 비용과 System.arraycopy()
- 다차원 배열의 메모리 레이아웃

#### 1.2 ArrayList

- 동적 배열의 개념
- 내부 배열 `elementData`와 `size` 필드
- **grow() 메커니즘**: 용량 초과 시 1.5배 확장
- `DEFAULT_CAPACITY = 10`의 의미
- 삽입/삭제 시 요소 이동 비용

```java
// 핵심 메서드 분석 대상
private void grow(int minCapacity)

public boolean add(E e)

public E remove(int index)
```

#### 1.3 LinkedList

- Node 클래스 구조 (item, next, prev)
- 이중 연결 리스트 (Doubly Linked List)
- 삽입/삭제 O(1) vs 탐색 O(n) 트레이드오프
- ArrayList vs LinkedList 선택 기준

#### 1.4 CPU 캐시 지역성 (Cache Locality) 🔥

시간 복잡도만으로는 실제 성능을 예측할 수 없다. **CPU 캐시 히트율** 관점에서 ArrayList와 LinkedList를 비교해야 한다.

**ArrayList의 캐시 친화성**

```
메모리 배치 (연속적)
┌───┬───┬───┬───┬───┬───┬───┬───┐
│ 0 │ 1 │ 2 │ 3 │ 4 │ 5 │ 6 │ 7 │  ← 캐시 라인에 한 번에 로드
└───┴───┴───┴───┴───┴───┴───┴───┘
```

- 배열은 메모리에 **연속적으로 배치**
- CPU가 데이터를 읽을 때 인접 데이터도 캐시 라인에 함께 로드
- **공간 지역성(Spatial Locality)** 활용 → 캐시 히트율 높음
- 순차 접근 시 실제 성능이 Big-O 수치보다 **훨씬 빠름**

**LinkedList의 캐시 비친화성**

```
메모리 배치 (흩어짐)
┌───┐     ┌───┐           ┌───┐
│ A │ ──→ │ B │ ────────→ │ C │  ← 포인터 추적 필요
└───┘     └───┘           └───┘
0x100     0x500           0x900
```

- 노드들이 **힙 메모리 곳곳에 흩어짐**
- 다음 노드 접근 시 **포인터 추적(Pointer Chasing)** 발생
- 캐시 미스가 빈번 → CPU 파이프라인 스톨
- 이론적으로 O(1) 삽입도 실제로는 느릴 수 있음

**결론**: 대부분의 경우 ArrayList가 더 빠르다. LinkedList는 **빈번한 중간 삽입/삭제가 확실할 때만** 고려.

### 시간 복잡도 정리

| 연산            | ArrayList | LinkedList |
|---------------|-----------|------------|
| get(index)    | O(1)      | O(n)       |
| add(끝)        | O(1) 평균   | O(1)       |
| add(중간)       | O(n)      | O(1)*      |
| remove(index) | O(n)      | O(n)       |
| contains      | O(n)      | O(n)       |

*탐색 시간 제외

### 실습 과제

#### 직접 구현 (원리 이해)

- [ ] MyArrayList 직접 구현 (add, get, remove, resize, ensureCapacity)
- [ ] MyLinkedList 직접 구현 (Node 클래스, addFirst, addLast, remove)

#### API 활용 실습 (실무 적용)

- [ ] ArrayList 초기 용량 설정과 `ensureCapacity()` 활용
- [ ] `List.of()` vs `Arrays.asList()` vs `new ArrayList<>()` 차이 체험
- [ ] `subList()`의 뷰(View) 특성 이해 (원본 변경 시 동작)
- [ ] `removeIf()`, `replaceAll()` 람다 활용
- [ ] `Collections.sort()` vs `List.sort()` 비교

```java
// 실무 패턴 예시
List<String> names = new ArrayList<>(expectedSize);  // 초기 용량 설정
names.

removeIf(name ->name.

startsWith("test_"));    // 조건부 삭제
	names.

replaceAll(String::toUpperCase);               // 일괄 변환
```

#### 성능 측정

- [ ] JMH 벤치마크: 데이터 1,000개 vs 1,000,000개에서 ArrayList/LinkedList 삽입 성능 역전 지점 찾기

---

## Phase 2: 스택 & 큐

### 학습 목표

LIFO/FIFO 원리와 내부 구현 방식 이해

### 학습 내용

#### 2.1 Stack

- LIFO (Last In First Out) 원리
- Java Stack 클래스의 문제점 (Vector 상속)
- **Deque를 스택으로 사용하는 것이 권장되는 이유**

```java
// 권장 방식
Deque<Integer> stack = new ArrayDeque<>();
stack.

push(1);
stack.

pop();
```

#### 2.2 Queue 인터페이스

- FIFO (First In First Out) 원리
- 예외 발생 메서드 vs 특수값 반환 메서드

| 동작 | 예외 발생     | 특수값 반환   |
|----|-----------|----------|
| 삽입 | add(e)    | offer(e) |
| 삭제 | remove()  | poll()   |
| 조회 | element() | peek()   |

#### 2.3 ArrayDeque

- **원형 배열 (Circular Array)** 구조
- head, tail 포인터의 동작
- null 요소를 허용하지 않는 이유
- Stack, Queue 양쪽으로 사용 가능

```
[  ][  ][  ][A ][B ][C ][  ][  ]
            ↑           ↑
           head        tail
```

#### 2.4 PriorityQueue

- **힙 (Heap)** 자료구조 기반
- 완전 이진 트리의 배열 표현
- heapify (siftUp, siftDown) 과정
- 자연 순서 vs Comparator

### 시간 복잡도 정리

| 연산          | ArrayDeque | PriorityQueue |
|-------------|------------|---------------|
| offer/add   | O(1) 평균    | O(log n)      |
| poll/remove | O(1)       | O(log n)      |
| peek        | O(1)       | O(1)          |

### 실습 과제

#### 직접 구현 (원리 이해)

- [ ] 원형 큐 직접 구현 (배열 기반)
- [ ] Min-Heap 직접 구현

#### API 활용 실습 (실무 적용)

- [ ] `ArrayDeque`를 Stack으로 활용 (`push`, `pop`, `peek`)
- [ ] `ArrayDeque`를 Queue로 활용 (`offer`, `poll`, `peek`)
- [ ] `PriorityQueue`에 커스텀 Comparator 적용
- [ ] `Collections.reverseOrder()`로 Max-Heap 구현
- [ ] `Deque`의 양방향 연산 활용 (`offerFirst`, `offerLast`, `pollFirst`, `pollLast`)

```java
// 실무 패턴 예시
Deque<String> stack = new ArrayDeque<>();           // Stack 대체
Queue<Task> taskQueue = new ArrayDeque<>();         // Queue로 사용

// Top K 문제: Min-Heap으로 상위 K개 유지
PriorityQueue<Integer> topK = new PriorityQueue<>(k);
for(
int num :numbers){
	topK.

offer(num);
    if(topK.

size() >k)topK.

poll();
}
```

#### 알고리즘 응용

- [ ] 괄호 검증 알고리즘 (스택 활용)
- [ ] 후위 표기식 계산기 구현

---

## Phase 3: 해시 자료구조 ⭐ 핵심

### 학습 목표

해시 충돌 해결 방식과 성능 특성 깊이 이해

### 학습 내용

#### 3.1 해시 기초

- **hashCode() 계약**
    - equals()가 true면 hashCode()도 같아야 함
    - hashCode()가 같아도 equals()는 다를 수 있음
- Objects.hash() 사용법
- 좋은 해시 함수의 조건

```java

@Override
public int hashCode() {
	return Objects.hash(name, age);
}
```

#### 3.2 HashMap 내부 구조

```
버킷 배열 (Node<K,V>[] table)
┌───┬───┬───┬───┬───┬───┬───┬───┐
│ 0 │ 1 │ 2 │ 3 │ 4 │ 5 │ 6 │ 7 │
└─┬─┴───┴─┬─┴───┴───┴─┬─┴───┴───┘
  │       │           │
  ▼       ▼           ▼
 [A]     [B]         [D]
  │       │
  ▼       ▼
 [C]     [E]  ← 체이닝
```

- **버킷 인덱스 계산**: `(n - 1) & hash`
- **Separate Chaining**: 연결 리스트로 충돌 해결
- **트리화 (Treeification)**
    - `TREEIFY_THRESHOLD = 8`: 체인 길이 8 이상이면 Red-Black Tree로 변환
    - `UNTREEIFY_THRESHOLD = 6`: 6 이하면 다시 리스트로
    - `MIN_TREEIFY_CAPACITY = 64`: 테이블 크기가 64 미만이면 트리화 대신 리사이징

#### 3.2.1 Hash DoS 공격과 트리화의 배경 🔥

**왜 Java 8에서 트리화를 도입했는가?**

트리화는 단순한 성능 최적화가 아니라 **보안 대응**이다.

**Hash DoS (Hash Collision Denial of Service) 공격**

```
공격자가 의도적으로 동일한 해시값을 갖는 키를 대량 전송
    ↓
모든 키가 같은 버킷에 체이닝
    ↓
O(1) → O(n) 성능 저하
    ↓
서버 마비 (DoS)
```

**실제 사례**: 2011년 PHP, Java, Ruby 등 다수 언어에서 Hash DoS 취약점 발견

**Java의 대응**

- Java 7: 해시 함수에 랜덤 시드 추가 시도
- Java 8: **체인 길이 8 이상 시 Red-Black Tree로 변환**
    - 최악의 경우에도 O(n) → O(log n) 보장
    - 공격자가 수백만 개 충돌을 일으켜도 로그 시간 유지

#### 3.3 리사이징 (Rehashing)

- **Load Factor**: 기본값 0.75
- threshold = capacity × loadFactor
- 리사이징 시 모든 요소 재배치 (비용 큰 작업)
- 초기 용량 설정의 중요성

```java
// 예상 요소 수가 100개라면
Map<K, V> map = new HashMap<>(134); // 100 / 0.75 ≈ 134
```

#### 3.4 LinkedHashMap

- HashMap + 이중 연결 리스트
- **삽입 순서 유지** (기본)
- **접근 순서 유지** (`accessOrder = true`)
- LRU 캐시 구현에 활용

```java
// LRU 캐시 구현
Map<K, V> lruCache = new LinkedHashMap<>(16, 0.75f, true) {
		@Override
		protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
			return size() > MAX_ENTRIES;
		}
	};
```

#### 3.5 HashSet

- **HashMap을 내부적으로 사용**
- 값은 더미 객체 `PRESENT`로 고정
- 중복 체크 = HashMap의 키 존재 여부 확인

### 시간 복잡도 정리

| 연산          | HashMap (평균) | HashMap (최악)     |
|-------------|--------------|------------------|
| put         | O(1)         | O(n) / O(log n)* |
| get         | O(1)         | O(n) / O(log n)* |
| remove      | O(1)         | O(n) / O(log n)* |
| containsKey | O(1)         | O(n) / O(log n)* |

*Java 8+ 트리화 시

### 실습 과제

#### 직접 구현 (원리 이해)

- [ ] 간단한 HashMap 직접 구현 (체이닝 방식)
- [ ] 커스텀 객체의 hashCode/equals 올바르게 구현

#### API 활용 실습 (실무 적용)

- [ ] `getOrDefault()`, `putIfAbsent()` 활용
- [ ] `compute()`, `computeIfAbsent()`, `computeIfPresent()` 차이 이해
- [ ] `merge()`로 카운팅, 누적 연산 구현
- [ ] `Map.of()`, `Map.ofEntries()`로 불변 맵 생성
- [ ] `forEach()`, `replaceAll()` 람다 활용
- [ ] LinkedHashMap으로 LRU 캐시 구현

```java
// 실무 패턴 예시

// 단어 빈도 카운팅 (merge 활용)
Map<String, Integer> wordCount = new HashMap<>();
for(
String word :words){
	wordCount.

merge(word, 1,Integer::sum);
}

// 그룹핑 (computeIfAbsent 활용)
Map<String, List<User>> usersByCity = new HashMap<>();
for(
User user :users){
	usersByCity.

computeIfAbsent(user.getCity(),k ->new ArrayList<>())
	.

add(user);
}

// Null-safe 조회
String value = map.getOrDefault(key, "default");
```

#### 심화 실습

- [ ] 해시 충돌 시나리오 테스트 (동일 hashCode 객체 대량 삽입)

---

## Phase 4: 트리 자료구조

### 학습 목표

정렬된 데이터 관리와 효율적인 탐색

### 학습 내용

#### 4.1 이진 탐색 트리 (BST)

- 왼쪽 < 부모 < 오른쪽 규칙
- 삽입, 삭제, 탐색 알고리즘
- **불균형 문제**: 편향 트리 시 O(n)

```
균형 트리          편향 트리
    4                 1
   / \                 \
  2   6                 2
 / \ / \                 \
1  3 5  7                 3
                           \
                            4
```

#### 4.2 Red-Black Tree (개념 수준)

- 자가 균형 이진 탐색 트리
- **5가지 속성**
    1. 모든 노드는 빨간색 또는 검은색
    2. 루트는 검은색
    3. 모든 리프(NIL)는 검은색
    4. 빨간 노드의 자식은 검은색
    5. 루트에서 리프까지 검은 노드 수 동일
- 회전 연산 (Left Rotation, Right Rotation)
- 삽입/삭제 후 재균형

#### 4.3 TreeMap / TreeSet

- Red-Black Tree 기반
- **NavigableMap 인터페이스** 메서드
    - `floorKey()`, `ceilingKey()`
    - `lowerKey()`, `higherKey()`
    - `subMap()`, `headMap()`, `tailMap()`
- null 키 허용하지 않음

```java
TreeMap<Integer, String> map = new TreeMap<>();
map.

put(1,"one");
map.

put(5,"five");
map.

put(3,"three");

map.

floorKey(4);    // 3 (4 이하 중 최대)
map.

ceilingKey(4);  // 5 (4 이상 중 최소)
map.

subMap(1,5);   // {1=one, 3=three}
```

#### 4.4 HashMap vs TreeMap 선택 기준

| 기준     | HashMap | TreeMap  |
|--------|---------|----------|
| 시간 복잡도 | O(1)    | O(log n) |
| 순서     | 없음      | 정렬됨      |
| null 키 | 허용      | 불허       |
| 범위 검색  | 불가      | 가능       |
| 메모리    | 상대적 적음  | 상대적 많음   |

### 시간 복잡도 정리

| 연산           | TreeMap  | TreeSet  |
|--------------|----------|----------|
| put/add      | O(log n) | O(log n) |
| get/contains | O(log n) | O(log n) |
| remove       | O(log n) | O(log n) |
| first/last   | O(log n) | O(log n) |

### 실습 과제

#### 직접 구현 (원리 이해)

- [ ] 이진 탐색 트리 직접 구현 (삽입, 삭제, 탐색, 순회)

#### API 활용 실습 (실무 적용)

- [ ] `NavigableMap` 범위 검색 메서드 활용
    - `floorKey()`, `ceilingKey()`, `lowerKey()`, `higherKey()`
    - `subMap()`, `headMap()`, `tailMap()`
- [ ] `NavigableSet` 메서드 활용 (`floor()`, `ceiling()`, `lower()`, `higher()`)
- [ ] `descendingMap()`, `descendingKeySet()`으로 역순 조회
- [ ] 커스텀 Comparator로 정렬 기준 변경

```java
// 실무 패턴 예시

// 시간 범위 데이터 조회
TreeMap<LocalDateTime, Event> events = new TreeMap<>();
// 특정 시간 이후의 이벤트들
SortedMap<LocalDateTime, Event> futureEvents = events.tailMap(LocalDateTime.now());

// 가장 가까운 값 찾기
TreeMap<Integer, String> priceMap = new TreeMap<>();
Integer nearestPrice = priceMap.ceilingKey(targetPrice);  // 이상 중 최소
Integer floorPrice = priceMap.floorKey(targetPrice);      // 이하 중 최대

// 구간 조회 (from 이상, to 미만)
SortedMap<Integer, String> range = priceMap.subMap(fromPrice, toPrice);
```

#### 응용 실습

- [ ] 시간 범위 데이터 조회 시스템 구현 (예: 특정 기간 로그 조회)

---

## Phase 5: 동시성 자료구조 ⭐ 실무 필수

### 학습 목표

멀티스레드 환경에서 안전한 자료구조 선택

### 학습 내용

#### 5.1 동기화 문제

- Race Condition
- 복합 연산의 원자성 문제
- `Collections.synchronizedXxx()`의 한계

```java
// 이 코드는 thread-safe하지 않음!
if(!map.containsKey(key)){
	map.

put(key, value);
}
```

#### 5.2 ConcurrentHashMap

- **Java 7**: Segment 기반 락 (16개 세그먼트)
- **Java 8+**: Node 단위 락 + CAS 연산
- `computeIfAbsent()`, `merge()` 등 원자적 복합 연산
- null 키/값 허용하지 않음

```java
// 원자적 복합 연산
map.computeIfAbsent(key, k ->

expensiveComputation(k));
	map.

merge(key, 1,Integer::sum);
```

#### 5.2.1 가시성(Visibility) vs 원자성(Atomicity) 🔥

동시성의 두 가지 핵심 개념을 구분해야 ConcurrentHashMap이 **왜 성능 저하 없이 안전한지** 이해할 수 있다.

**가시성 (Visibility) 문제**

```java
// Thread A
flag =true;

	// Thread B
	while(!flag){} // 영원히 루프할 수 있음!
```

- 한 스레드의 변경이 다른 스레드에 **보이지 않는** 문제
- CPU 캐시, 컴파일러 최적화, 메모리 재배치가 원인
- **해결**: `volatile` 키워드 → 메인 메모리에 즉시 반영

**원자성 (Atomicity) 문제**

```java
// count++는 사실 3단계 연산
// 1. 읽기: temp = count
// 2. 증가: temp = temp + 1
// 3. 쓰기: count = temp
// → 중간에 다른 스레드가 끼어들 수 있음!
```

- 복합 연산이 **중간에 끊기는** 문제
- **해결**: `synchronized`, Lock, 또는 CAS 연산

**ConcurrentHashMap의 해결책**

```
┌─────────────────────────────────────────────────┐
│ volatile Node<K,V>[] table                       │
│ → 배열 참조의 가시성 보장                          │
├─────────────────────────────────────────────────┤
│ CAS (Compare And Swap)                          │
│ → 락 없이 원자적 업데이트                          │
│ → 실패 시 재시도 (낙관적 락)                       │
├─────────────────────────────────────────────────┤
│ synchronized (특정 버킷)                         │
│ → 충돌 시에만 해당 버킷만 락                       │
│ → 다른 버킷은 동시 접근 가능                       │
└─────────────────────────────────────────────────┘
```

**핵심**: 전체 맵을 잠그지 않고, 필요한 부분만 최소한으로 동기화

#### 5.3 CopyOnWriteArrayList

- 쓰기 시 전체 배열 복사
- 읽기는 락 없이 진행
- **읽기 많고 쓰기 적은 시나리오**에 적합
- Iterator는 스냅샷 기반 (ConcurrentModificationException 없음)

#### 5.4 BlockingQueue 계열

- **ArrayBlockingQueue**: 고정 크기 배열 기반
- **LinkedBlockingQueue**: 가변 크기 연결 리스트 기반
- **PriorityBlockingQueue**: 우선순위 기반
- 생산자-소비자 패턴의 핵심

| 메서드                  | 블로킹 | 타임아웃 |
|----------------------|-----|------|
| put(e)               | O   | -    |
| take()               | O   | -    |
| offer(e, time, unit) | -   | O    |
| poll(time, unit)     | -   | O    |

#### 5.5 동시성 컬렉션 선택 가이드

| 상황          | 권장 컬렉션                |
|-------------|-----------------------|
| 읽기 많은 Map   | ConcurrentHashMap     |
| 읽기 많은 List  | CopyOnWriteArrayList  |
| 생산자-소비자     | BlockingQueue         |
| 정렬된 동시성 Map | ConcurrentSkipListMap |

### 실습 과제

#### API 활용 실습 (실무 적용)

- [ ] `ConcurrentHashMap` 원자적 연산 활용
    - `computeIfAbsent()`, `computeIfPresent()`, `compute()`
    - `merge()`, `putIfAbsent()`
- [ ] `ConcurrentHashMap.newKeySet()`으로 동시성 Set 생성
- [ ] `BlockingQueue` 인터페이스 메서드 활용
    - 블로킹: `put()`, `take()`
    - 타임아웃: `offer(e, time, unit)`, `poll(time, unit)`
- [ ] `CopyOnWriteArrayList` 읽기 중심 시나리오 활용

```java
// 실무 패턴 예시

// 동시성 캐시 (computeIfAbsent - 원자적 lazy 초기화)
ConcurrentHashMap<String, ExpensiveObject> cache = new ConcurrentHashMap<>();
ExpensiveObject obj = cache.computeIfAbsent(key, k -> createExpensiveObject(k));

// 동시성 카운터 (merge)
ConcurrentHashMap<String, Long> counters = new ConcurrentHashMap<>();
counters.

merge(eventType, 1L,Long::sum);

// 동시성 Set
Set<String> concurrentSet = ConcurrentHashMap.newKeySet();

// 생산자-소비자 패턴
BlockingQueue<Task> queue = new LinkedBlockingQueue<>(100);
// Producer
queue.

put(task);  // 큐가 가득 차면 블로킹

// Consumer
Task task = queue.take();  // 큐가 비어있으면 블로킹
```

#### 직접 구현 & 비교 실습

- [ ] 생산자-소비자 패턴 구현 (BlockingQueue 활용)
- [ ] ConcurrentHashMap vs synchronized HashMap 성능 비교
- [ ] 동시성 카운터 여러 방식 비교
    - `synchronized`
    - `AtomicLong`
    - `LongAdder`
    - `ConcurrentHashMap.merge()`

---

## Phase 6: 심화 주제

### 학습 목표

특수 상황에서 활용되는 자료구조 이해

### 학습 내용

#### 6.1 WeakHashMap

- **약한 참조 (Weak Reference)** 기반
- 키가 더 이상 참조되지 않으면 자동 제거
- 캐시 구현에 활용
- 주의: 값이 키를 참조하면 제거되지 않음

#### 6.2 EnumMap / EnumSet

- **비트 벡터** 기반 최적화
- Enum 상수를 키로 사용
- HashMap보다 빠르고 메모리 효율적

```java
EnumMap<DayOfWeek, String> schedule = new EnumMap<>(DayOfWeek.class);
EnumSet<DayOfWeek> weekdays = EnumSet.range(MONDAY, FRIDAY);
```

#### 6.3 IdentityHashMap

- `==` 비교 사용 (equals() 아님)
- 참조 동일성 기반
- 객체 그래프 탐색, 직렬화에 활용

#### 6.4 Immutable Collections (Java 9+)

- `List.of()`, `Set.of()`, `Map.of()`
- 불변 보장, null 허용하지 않음
- 방어적 복사 불필요

```java
List<String> immutable = List.of("a", "b", "c");
// immutable.add("d"); // UnsupportedOperationException
```

#### 6.5 기타 유용한 클래스

- **BitSet**: 비트 연산 최적화
- **Collections.nCopies()**: 불변 리스트 생성
- **Arrays.asList()**: 고정 크기 리스트 (주의사항)

### 실습 과제

#### API 활용 실습 (실무 적용)

- [ ] `EnumMap`, `EnumSet` 활용 (상태 관리, 플래그 조합)
- [ ] `List.of()`, `Set.of()`, `Map.of()` 불변 컬렉션 생성
- [ ] `Collections.unmodifiableXxx()` vs `List.of()` 차이 이해
- [ ] `Arrays.asList()` 주의사항 (고정 크기, 원본 배열 연결)

```java
// 실무 패턴 예시

// EnumSet으로 권한 관리
EnumSet<Permission> adminPerms = EnumSet.of(READ, WRITE, DELETE);
EnumSet<Permission> userPerms = EnumSet.of(READ);
if(userPerms.

contains(WRITE)){...}

// EnumMap으로 상태별 핸들러
EnumMap<OrderStatus, Consumer<Order>> handlers = new EnumMap<>(OrderStatus.class);
handlers.

put(PENDING, this::processPending);
handlers.

put(CONFIRMED, this::processConfirmed);
handlers.

get(order.getStatus()).

accept(order);

// 불변 컬렉션 (방어적 복사 불필요)
public List<String> getItems() {
	return List.copyOf(items);  // 불변 복사본 반환
}
```

#### 심화 실습

- [ ] WeakHashMap 기반 캐시 구현 (GC 연동 확인)
- [ ] BitSet 활용 (대량 플래그 처리, 에라토스테네스의 체)

---

## 📊 전체 시간 복잡도 요약표

| 자료구조          | 삽입        | 삭제       | 조회       | 탐색       | 특징        |
|---------------|-----------|----------|----------|----------|-----------|
| ArrayList     | O(1)/O(n) | O(n)     | O(1)     | O(n)     | 인덱스 접근 빠름 |
| LinkedList    | O(1)      | O(1)     | O(n)     | O(n)     | 삽입/삭제 빠름  |
| HashMap       | O(1)      | O(1)     | O(1)     | -        | 해시 기반     |
| TreeMap       | O(log n)  | O(log n) | O(log n) | -        | 정렬 유지     |
| HashSet       | O(1)      | O(1)     | -        | O(1)     | 중복 제거     |
| TreeSet       | O(log n)  | O(log n) | -        | O(log n) | 정렬된 Set   |
| PriorityQueue | O(log n)  | O(log n) | O(1)     | O(n)     | 힙 기반      |
| ArrayDeque    | O(1)      | O(1)     | O(1)     | O(n)     | 양방향 큐     |

---

## ✅ 체크리스트

### Phase 1

- [ ] ArrayList 내부 구현 이해
- [ ] LinkedList 내부 구현 이해
- [ ] CPU 캐시 지역성과 실제 성능 차이 이해
- [ ] 직접 구현 완료
- [ ] List API 활용 (`removeIf`, `replaceAll`, `subList` 등)
- [ ] JMH 벤치마크로 성능 역전 지점 확인

### Phase 2

- [ ] ArrayDeque 원형 배열 이해
- [ ] PriorityQueue 힙 구조 이해
- [ ] Deque를 Stack/Queue로 활용
- [ ] PriorityQueue + Comparator 활용
- [ ] 실습 과제 완료

### Phase 3

- [ ] HashMap 버킷/체이닝/트리화 이해
- [ ] Hash DoS 공격과 트리화의 보안적 의미 이해
- [ ] hashCode/equals 계약 이해
- [ ] Map API 활용 (`compute`, `merge`, `getOrDefault` 등)
- [ ] LinkedHashMap LRU 캐시 구현

### Phase 4

- [ ] BST 직접 구현
- [ ] Red-Black Tree 개념 이해
- [ ] NavigableMap/NavigableSet API 활용
- [ ] TreeMap 범위 검색 활용

### Phase 5

- [ ] 가시성(Visibility) vs 원자성(Atomicity) 구분
- [ ] volatile과 CAS 연산 이해
- [ ] ConcurrentHashMap 동작 원리 이해
- [ ] ConcurrentHashMap 원자적 연산 API 활용
- [ ] BlockingQueue 생산자-소비자 패턴
- [ ] 동시성 컬렉션 선택 기준 정립

### Phase 6

- [ ] EnumMap/EnumSet 활용
- [ ] 불변 컬렉션 API 활용 (`List.of`, `Map.of` 등)
- [ ] 특수 목적 컬렉션 이해

---

*마지막 업데이트: 2025년 1월*