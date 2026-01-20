# 4단계: String vs StringBuilder vs StringBuffer

## 학습 목표

이 문서를 학습하고 나면 String, StringBuilder, StringBuffer 세 클래스의 내부 구현 차이를 명확히 설명할 수 있어야 한다. 가변(mutable)과 불변(immutable)의 차이가 성능에 미치는 영향을 이해하고, 상황에 따라 적절한 클래스를 선택할 수 있어야 한다. 또한 컴파일러가 문자열 연결(`+`)을 어떻게 최적화하는지 바이트코드 레벨에서 이해해야 한다.

---

## 1. 핵심 질문: 왜 세 가지나 필요한가?

Java에서 문자열을 다루는 클래스가 세 가지나 있는 이유는 무엇일까? 간단히 말하면, 각각 다른 사용 시나리오에 최적화되어 있기 때문이다.

| 클래스 | 가변성 | 스레드 안전성 | 주요 용도 |
|--------|--------|---------------|-----------|
| String | 불변 | 안전 (불변이므로) | 문자열 저장, 전달, 비교 |
| StringBuilder | 가변 | 안전하지 않음 | 단일 스레드에서 문자열 조작 |
| StringBuffer | 가변 | 안전 (동기화) | 멀티 스레드에서 문자열 조작 |

String은 1단계에서 배웠듯이 불변 객체다. 문자열을 변경하려면 매번 새 객체를 생성해야 한다. 이는 안전하지만, 빈번한 수정이 필요한 경우 비효율적이다.

StringBuilder와 StringBuffer는 이 문제를 해결하기 위해 등장했다. 내부 버퍼를 직접 수정할 수 있어 새 객체 생성 없이 문자열을 조작할 수 있다.

---

## 2. 내부 구현 분석

### 2.1 공통 부모: AbstractStringBuilder

StringBuilder와 StringBuffer는 모두 `AbstractStringBuilder`를 상속받는다.

```java
// OpenJDK 소스 코드 기반 (jdk/src/java.base/share/classes/java/lang/AbstractStringBuilder.java)
abstract class AbstractStringBuilder implements Appendable, CharSequence {
    /**
     * 문자 데이터를 저장하는 버퍼
     */
    byte[] value;
    
    /**
     * 인코딩 식별자 (LATIN1 = 0, UTF16 = 1)
     */
    byte coder;
    
    /**
     * 실제 사용 중인 문자 수
     */
    int count;
    
    /**
     * 버퍼의 최대 크기
     */
    private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;
}
```

> **출처**: OpenJDK 17 소스 코드, `java.lang.AbstractStringBuilder`
> https://github.com/openjdk/jdk/blob/jdk-17%2B35/src/java.base/share/classes/java/lang/AbstractStringBuilder.java

String과의 핵심 차이점을 보자.

첫째, `value` 배열이 `final`이 아니다. 크기가 부족하면 더 큰 배열로 교체할 수 있다.

둘째, `count` 필드가 있다. 배열 크기(capacity)와 실제 사용량(count)이 다를 수 있다. 이 덕분에 버퍼를 미리 크게 잡아두고 필요한 만큼만 사용할 수 있다.

### 2.2 StringBuilder

```java
// OpenJDK 소스 코드 기반
public final class StringBuilder
    extends AbstractStringBuilder
    implements java.io.Serializable, Comparable<StringBuilder>, CharSequence
{
    public StringBuilder() {
        super(16);  // 기본 용량 16
    }
    
    public StringBuilder(int capacity) {
        super(capacity);
    }
    
    public StringBuilder(String str) {
        super(str.length() + 16);  // 문자열 길이 + 16
        append(str);
    }
    
    @Override
    public StringBuilder append(String str) {
        super.append(str);
        return this;  // 메서드 체이닝 지원
    }
    
    // 다른 메서드들...
}
```

> **출처**: OpenJDK 17 소스 코드, `java.lang.StringBuilder`
> https://github.com/openjdk/jdk/blob/jdk-17%2B35/src/java.base/share/classes/java/lang/StringBuilder.java

### 2.3 StringBuffer

```java
// OpenJDK 소스 코드 기반
public final class StringBuffer
    extends AbstractStringBuilder
    implements java.io.Serializable, Comparable<StringBuffer>, CharSequence
{
    // StringBuilder와 거의 동일하지만, 모든 public 메서드에 synchronized 키워드가 붙어있다
    
    @Override
    public synchronized StringBuffer append(String str) {
        toStringCache = null;  // 캐시 무효화
        super.append(str);
        return this;
    }
    
    @Override
    public synchronized String toString() {
        if (toStringCache == null) {
            return toStringCache = new String(value, 0, count);
        }
        return toStringCache;
    }
    
    // toStringCache: toString() 결과를 캐싱하여 반복 호출 시 성능 향상
    private transient String toStringCache;
}
```

> **출처**: OpenJDK 17 소스 코드, `java.lang.StringBuffer`
> https://github.com/openjdk/jdk/blob/jdk-17%2B35/src/java.base/share/classes/java/lang/StringBuffer.java

StringBuffer는 모든 public 메서드에 `synchronized` 키워드가 붙어있어 스레드 안전하다. 대신 동기화 오버헤드가 발생한다.

---

## 3. 버퍼 확장 메커니즘

### 3.1 확장이 필요한 시점

`append()`를 호출했는데 버퍼 공간이 부족하면 확장이 일어난다.

```java
// AbstractStringBuilder.append(String str) 내부 로직 (단순화)
public AbstractStringBuilder append(String str) {
    if (str == null) {
        return appendNull();
    }
    int len = str.length();
    ensureCapacityInternal(count + len);  // 공간 확보
    putStringAt(count, str);               // 데이터 복사
    count += len;
    return this;
}
```

### 3.2 확장 정책

```java
// OpenJDK 소스 코드 기반
private int newCapacity(int minCapacity) {
    // 기존 용량의 2배 + 2
    int oldCapacity = value.length >> coder;
    int newCapacity = (oldCapacity << 1) + 2;
    
    if (newCapacity - minCapacity < 0) {
        newCapacity = minCapacity;
    }
    
    // 최대 크기 체크
    int SAFE_BOUND = MAX_ARRAY_SIZE >> coder;
    return (newCapacity <= 0 || SAFE_BOUND - newCapacity < 0)
        ? hugeCapacity(minCapacity)
        : newCapacity;
}
```

> **출처**: OpenJDK 17 소스 코드, `java.lang.AbstractStringBuilder.newCapacity()`

확장 정책을 정리하면 다음과 같다.

1. 기존 용량의 **2배 + 2**로 확장
2. 그래도 부족하면 필요한 최소 용량으로 설정
3. 최대 크기는 `Integer.MAX_VALUE - 8`

### 3.3 확장 비용

버퍼 확장 시 내부적으로 `Arrays.copyOf()`가 호출된다. 이는 O(n) 연산이다.

```java
// 확장 과정
value = Arrays.copyOf(value, newCapacity << coder);
```

이 때문에 최종 크기를 예측할 수 있다면 초기 용량을 지정하는 것이 좋다.

```java
// 비효율적: 여러 번 확장 발생 가능
StringBuilder sb = new StringBuilder();  // 기본 용량 16
for (int i = 0; i < 1000; i++) {
    sb.append("hello");  // 16 → 34 → 70 → 142 → ... 확장 반복
}

// 효율적: 확장 없음
StringBuilder sb = new StringBuilder(5000);  // 미리 충분한 용량 확보
for (int i = 0; i < 1000; i++) {
    sb.append("hello");
}
```

### 3.4 확장 횟수 계산

초기 용량 16에서 시작하여 5000자를 채우려면 몇 번의 확장이 필요할까?

```
16 → 34 → 70 → 142 → 286 → 574 → 1150 → 2302 → 4606 → 9214
```

9번의 확장이 필요하다. 각 확장마다 배열 복사가 일어나므로, 초기 용량 설정이 중요하다.

---

## 4. 성능 비교: 문자열 연결

### 4.1 String으로 연결하는 경우

```java
String result = "";
for (int i = 0; i < n; i++) {
    result = result + i;  // 매번 새 String 객체 생성
}
```

이 코드의 시간 복잡도는 **O(n²)**이다. 왜냐하면 매 반복마다 새 String 객체를 생성하고, 기존 문자열 전체를 복사해야 하기 때문이다.

```
반복 1: 길이 1 복사
반복 2: 길이 2 복사  
반복 3: 길이 3 복사
...
반복 n: 길이 n 복사

총 복사량: 1 + 2 + 3 + ... + n = n(n+1)/2 = O(n²)
```

### 4.2 StringBuilder로 연결하는 경우

```java
StringBuilder sb = new StringBuilder();
for (int i = 0; i < n; i++) {
    sb.append(i);  // 버퍼에 추가
}
String result = sb.toString();
```

이 코드의 시간 복잡도는 **O(n)**이다. 버퍼 확장이 발생해도 amortized O(1)이므로 전체적으로 O(n)이다.

### 4.3 실제 벤치마크

```java
public class StringConcatBenchmark {
    public static void main(String[] args) {
        int[] sizes = {1000, 5000, 10000, 50000};
        
        for (int n : sizes) {
            // String 연결
            long start1 = System.currentTimeMillis();
            String s = "";
            for (int i = 0; i < n; i++) {
                s = s + "a";
            }
            long time1 = System.currentTimeMillis() - start1;
            
            // StringBuilder 연결
            long start2 = System.currentTimeMillis();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < n; i++) {
                sb.append("a");
            }
            String result = sb.toString();
            long time2 = System.currentTimeMillis() - start2;
            
            System.out.println("n=" + n + " | String: " + time1 + "ms, StringBuilder: " + time2 + "ms");
        }
    }
}
```

**예상 출력** (환경에 따라 다름):
```
n=1000 | String: 5ms, StringBuilder: 0ms
n=5000 | String: 45ms, StringBuilder: 0ms
n=10000 | String: 150ms, StringBuilder: 1ms
n=50000 | String: 3500ms, StringBuilder: 2ms
```

n이 커질수록 차이가 급격하게 벌어진다. 이것이 O(n²) vs O(n)의 차이다.

---

## 5. 컴파일러의 문자열 연결 최적화

### 5.1 Java 8 이전: StringBuilder로 변환

```java
String s = a + b + c;

// 컴파일러가 아래와 같이 변환
String s = new StringBuilder().append(a).append(b).append(c).toString();
```

하지만 반복문 안에서는 최적화가 제대로 안 된다.

```java
String result = "";
for (int i = 0; i < n; i++) {
    result = result + i;
}

// 컴파일러 변환 (문제!)
String result = "";
for (int i = 0; i < n; i++) {
    result = new StringBuilder().append(result).append(i).toString();
    // 매 반복마다 새 StringBuilder 생성!
}
```

### 5.2 Java 9+: invokedynamic 기반 최적화

Java 9부터 문자열 연결이 `invokedynamic`을 사용하도록 변경되었다(JEP 280).

> **출처**: JEP 280: Indify String Concatenation
> https://openjdk.org/jeps/280

```java
String s = a + b + c;

// Java 9+ 바이트코드 (개념적)
invokedynamic makeConcatWithConstants(a, b, c)
```

이 방식의 장점은 다음과 같다.

1. JVM이 런타임에 최적의 연결 전략을 선택할 수 있다
2. 바이트코드 크기가 줄어든다
3. 향후 JVM 개선 사항을 자동으로 활용할 수 있다

하지만 **반복문 안의 연결은 여전히 비효율적**이다. 명시적으로 StringBuilder를 사용해야 한다.

### 5.3 바이트코드 확인 방법

```bash
# 컴파일
javac StringConcat.java

# 바이트코드 확인
javap -c StringConcat.class
```

---

## 6. StringBuilder vs StringBuffer

### 6.1 동기화 오버헤드

StringBuffer의 모든 public 메서드는 `synchronized`로 보호된다. 단일 스레드 환경에서는 불필요한 오버헤드다.

```java
// StringBuffer.append() - 동기화됨
public synchronized StringBuffer append(String str) {
    toStringCache = null;
    super.append(str);
    return this;
}

// StringBuilder.append() - 동기화 없음
public StringBuilder append(String str) {
    super.append(str);
    return this;
}
```

### 6.2 성능 차이 측정

```java
public class SyncOverheadTest {
    public static void main(String[] args) {
        int iterations = 10_000_000;
        
        // StringBuilder
        StringBuilder sb = new StringBuilder();
        long start1 = System.currentTimeMillis();
        for (int i = 0; i < iterations; i++) {
            sb.append("a");
            sb.setLength(0);  // 초기화
        }
        System.out.println("StringBuilder: " + (System.currentTimeMillis() - start1) + "ms");
        
        // StringBuffer
        StringBuffer sbuf = new StringBuffer();
        long start2 = System.currentTimeMillis();
        for (int i = 0; i < iterations; i++) {
            sbuf.append("a");
            sbuf.setLength(0);  // 초기화
        }
        System.out.println("StringBuffer: " + (System.currentTimeMillis() - start2) + "ms");
    }
}
```

일반적으로 StringBuilder가 10~20% 정도 빠르다. 하지만 JIT 컴파일러가 단일 스레드 환경에서 StringBuffer의 락을 제거(lock elision)하기도 하므로, 실제 차이는 환경에 따라 다를 수 있다.

### 6.3 StringBuffer의 toString() 캐싱

StringBuffer는 `toString()` 결과를 캐싱한다.

```java
// StringBuffer 내부
private transient String toStringCache;

@Override
public synchronized String toString() {
    if (toStringCache == null) {
        toStringCache = new String(value, 0, count);
    }
    return toStringCache;
}

// 수정 시 캐시 무효화
@Override
public synchronized StringBuffer append(String str) {
    toStringCache = null;  // 캐시 무효화
    super.append(str);
    return this;
}
```

> **출처**: OpenJDK 17 소스 코드, `java.lang.StringBuffer`

이 덕분에 StringBuffer에서 연속으로 `toString()`을 호출하면 두 번째부터는 캐싱된 값을 반환한다. StringBuilder는 이 기능이 없다.

---

## 7. 실무 선택 가이드

### 7.1 언제 무엇을 사용할까?

**String을 사용해야 하는 경우**:
- 문자열이 변경되지 않는 경우
- 메서드 파라미터, 반환값으로 전달하는 경우
- Map의 키로 사용하는 경우
- 여러 스레드에서 공유해야 하는 경우

**StringBuilder를 사용해야 하는 경우**:
- 반복문에서 문자열을 조립하는 경우
- 단일 스레드 환경에서 문자열을 자주 수정하는 경우
- 동적으로 SQL 쿼리나 로그 메시지를 생성하는 경우

**StringBuffer를 사용해야 하는 경우**:
- 여러 스레드가 동시에 같은 문자열 버퍼를 수정하는 경우
- (사실 이런 경우는 드물다. 대부분 스레드별로 StringBuilder를 사용하거나, 다른 동기화 메커니즘을 사용한다)

### 7.2 초기 용량 설정

예상 크기를 알면 초기 용량을 설정하는 것이 좋다.

```java
// 좋은 예: 예상 크기 지정
StringBuilder sb = new StringBuilder(expectedSize);

// 실무 예시: CSV 한 줄 생성
public String toCsv(List<String> values) {
    // 각 값 평균 10자 + 구분자 가정
    StringBuilder sb = new StringBuilder(values.size() * 11);
    for (int i = 0; i < values.size(); i++) {
        if (i > 0) sb.append(',');
        sb.append(values.get(i));
    }
    return sb.toString();
}
```

### 7.3 재사용 패턴

StringBuilder 객체를 재사용하면 GC 부담을 줄일 수 있다.

```java
// StringBuilder 재사용
public class LogFormatter {
    private final StringBuilder sb = new StringBuilder(256);
    
    public String format(String level, String message) {
        sb.setLength(0);  // 내용만 초기화, 버퍼는 유지
        sb.append('[').append(level).append("] ").append(message);
        return sb.toString();
    }
}
```

단, 이 패턴은 **단일 스레드에서만** 안전하다. 멀티 스레드 환경에서는 ThreadLocal을 사용하거나 매번 새로 생성해야 한다.

```java
// ThreadLocal을 사용한 스레드 안전한 재사용
public class ThreadSafeFormatter {
    private static final ThreadLocal<StringBuilder> BUFFER = 
        ThreadLocal.withInitial(() -> new StringBuilder(256));
    
    public static String format(String level, String message) {
        StringBuilder sb = BUFFER.get();
        sb.setLength(0);
        sb.append('[').append(level).append("] ").append(message);
        return sb.toString();
    }
}
```

---

## 8. 주요 메서드 정리

### 8.1 공통 메서드

```java
StringBuilder sb = new StringBuilder("Hello");

// 추가
sb.append(" World");        // "Hello World"
sb.append(123);             // "Hello World123" (int, long, double 등 지원)
sb.append(true);            // "Hello World123true"

// 삽입
sb.insert(0, "Say: ");      // "Say: Hello World123true"

// 삭제
sb.delete(0, 5);            // "Hello World123true"
sb.deleteCharAt(0);         // "ello World123true"

// 교체
sb.replace(0, 4, "H");      // "H World123true"

// 뒤집기
sb.reverse();               // "eurt321dlroW H"

// 길이 조절
sb.setLength(5);            // "eurt3"
sb.setLength(10);           // "eurt3\0\0\0\0\0" (null 문자로 채움)

// 용량 관련
sb.capacity();              // 현재 버퍼 크기
sb.ensureCapacity(100);     // 최소 100 이상 확보
sb.trimToSize();            // 버퍼 크기를 실제 사용량에 맞춤
```

### 8.2 메서드 체이닝

StringBuilder의 대부분의 메서드는 `this`를 반환하므로 체이닝이 가능하다.

```java
String result = new StringBuilder()
    .append("Name: ")
    .append(name)
    .append(", Age: ")
    .append(age)
    .toString();
```

---

## 9. 실습: 내부 동작 확인

### 실습 9.1: 버퍼 확장 관찰

```java
public class BufferExpansionTest {
    public static void main(String[] args) {
        StringBuilder sb = new StringBuilder();
        
        System.out.println("초기 용량: " + sb.capacity());
        
        for (int i = 0; i < 100; i++) {
            int prevCapacity = sb.capacity();
            sb.append("a");
            int newCapacity = sb.capacity();
            
            if (newCapacity != prevCapacity) {
                System.out.println("길이 " + sb.length() + "에서 확장: " 
                    + prevCapacity + " → " + newCapacity);
            }
        }
    }
}
```

**예상 출력**:
```
초기 용량: 16
길이 17에서 확장: 16 → 34
길이 35에서 확장: 34 → 70
길이 71에서 확장: 70 → 142
```

### 실습 9.2: O(n²) vs O(n) 체감

```java
public class ComplexityComparisonTest {
    public static void main(String[] args) {
        System.out.println("=== String 연결 (O(n²)) ===");
        for (int n = 10000; n <= 50000; n += 10000) {
            long start = System.currentTimeMillis();
            String s = "";
            for (int i = 0; i < n; i++) {
                s = s + "a";
            }
            System.out.println("n=" + n + ": " + (System.currentTimeMillis() - start) + "ms");
        }
        
        System.out.println("\n=== StringBuilder 연결 (O(n)) ===");
        for (int n = 10000; n <= 50000; n += 10000) {
            long start = System.currentTimeMillis();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < n; i++) {
                sb.append("a");
            }
            String result = sb.toString();
            System.out.println("n=" + n + ": " + (System.currentTimeMillis() - start) + "ms");
        }
    }
}
```

String 연결은 n이 2배가 되면 시간이 4배가 되는 반면, StringBuilder는 거의 선형으로 증가하는 것을 확인할 수 있다.

---

## 10. 핵심 정리

String은 불변 객체로, 문자열 수정 시 항상 새 객체가 생성된다. 반복문에서 문자열을 연결하면 O(n²) 시간 복잡도가 된다.

StringBuilder와 StringBuffer는 가변 객체로, 내부 버퍼를 직접 수정한다. 반복문에서 문자열을 연결해도 O(n) 시간 복잡도를 유지한다.

StringBuilder는 동기화를 하지 않아 단일 스레드에서 빠르다. StringBuffer는 모든 메서드가 synchronized로 보호되어 스레드 안전하지만 오버헤드가 있다.

버퍼 확장 정책은 "기존 용량 × 2 + 2"다. 예상 크기를 알면 초기 용량을 지정하여 확장 비용을 피할 수 있다.

Java 9부터 문자열 연결(`+`)이 invokedynamic 기반으로 최적화되었지만, 반복문 안에서는 여전히 StringBuilder를 명시적으로 사용해야 한다.

실무에서는 거의 대부분 StringBuilder를 사용한다. StringBuffer가 필요한 경우는 매우 드물다.

---

## 11. 참고 자료

- OpenJDK 17 소스 코드: https://github.com/openjdk/jdk/tree/jdk-17%2B35
- JEP 280: Indify String Concatenation: https://openjdk.org/jeps/280
- Java Language Specification, Chapter 15.18.1 (String Concatenation Operator +): https://docs.oracle.com/javase/specs/jls/se17/html/jls-15.html#jls-15.18.1

---

## 12. 다음 단계 예고

5단계에서는 실무에서 자주 마주치는 String 관련 패턴과 안티패턴을 학습한다. null 안전한 문자열 비교, 빈 문자열 체크의 여러 방식, 문자열 포맷팅 방법들의 성능 비교, 정규식 사용 시 주의점 등을 다룬다.

---

# 5단계: 실무 패턴과 안티패턴

## 학습 목표

이 문서를 학습하고 나면 실무에서 자주 발생하는 String 관련 실수들을 사전에 방지할 수 있어야 한다. null 안전한 문자열 처리 패턴을 익히고, 빈 문자열 체크의 여러 방식과 차이점을 이해해야 한다. 또한 문자열 포맷팅 방법들의 성능 특성을 파악하고 상황에 맞게 선택할 수 있어야 한다.

---

## 1. null 안전한 문자열 비교

### 1.1 흔한 실수: NullPointerException

```java
public void processUserInput(String input) {
    if (input.equals("admin")) {  // input이 null이면 NPE 발생!
        grantAdminAccess();
    }
}
```

이 코드는 `input`이 null일 때 `NullPointerException`을 던진다. 실무에서 매우 흔하게 발생하는 버그다.

### 1.2 패턴 1: 리터럴을 앞에 배치

```java
// 안전한 방식
if ("admin".equals(input)) {
    grantAdminAccess();
}
```

리터럴 `"admin"`은 절대 null이 아니므로 `equals()`를 안전하게 호출할 수 있다. `input`이 null이어도 `equals()` 내부에서 `false`를 반환한다.

```java
// String.equals() 내부 로직
public boolean equals(Object anObject) {
    if (this == anObject) {
        return true;
    }
    if (anObject instanceof String) {  // null은 instanceof 체크에서 false
        // 비교 로직...
    }
    return false;
}
```

이 패턴을 **Yoda Condition**(요다 조건)이라고 부르기도 한다. 스타워즈의 요다가 어순을 뒤집어 말하는 것처럼, 상수를 왼쪽에 두기 때문이다.

### 1.3 패턴 2: Objects.equals() 사용 (Java 7+)

```java
import java.util.Objects;

// 양쪽 모두 null-safe
if (Objects.equals(input, "admin")) {
    grantAdminAccess();
}

// 두 변수 비교에도 안전
if (Objects.equals(str1, str2)) {
    // str1, str2 중 어느 것이 null이어도 안전
}
```

`Objects.equals()`의 내부 구현을 보자.

```java
// java.util.Objects.equals() - OpenJDK 소스
public static boolean equals(Object a, Object b) {
    return (a == b) || (a != null && a.equals(b));
}
```

> **출처**: OpenJDK 17 소스 코드, `java.util.Objects`
> https://github.com/openjdk/jdk/blob/jdk-17%2B35/src/java.base/share/classes/java/util/Objects.java

이 메서드는 다음 경우를 모두 처리한다.
- 둘 다 null → true (같은 참조)
- 하나만 null → false (a != null 체크 실패 또는 equals에서 false)
- 둘 다 non-null → equals() 결과

### 1.4 패턴 3: 명시적 null 체크

```java
// 가장 명확한 방식
if (input != null && input.equals("admin")) {
    grantAdminAccess();
}

// 또는 early return 패턴
public void processUserInput(String input) {
    if (input == null) {
        return;  // 또는 예외 던지기
    }
    
    if (input.equals("admin")) {
        grantAdminAccess();
    }
}
```

코드의 의도가 가장 명확하게 드러나는 방식이다. null이 유효한 입력인지, 오류 상황인지에 따라 처리 방식을 선택하면 된다.

### 1.5 어떤 패턴을 선택할까?

| 상황 | 권장 패턴 |
|------|-----------|
| 리터럴과 비교 | `"literal".equals(variable)` |
| 두 변수 비교 | `Objects.equals(a, b)` |
| null이 명백한 오류인 경우 | 명시적 null 체크 + 예외 |
| null 체크가 비즈니스 로직의 일부인 경우 | 명시적 null 체크 |

---

## 2. 빈 문자열 체크

### 2.1 여러 가지 방식

```java
String s = "";

// 방식 1: length() 체크
if (s.length() == 0) { }

// 방식 2: isEmpty() (Java 6+)
if (s.isEmpty()) { }

// 방식 3: 빈 문자열과 equals
if (s.equals("")) { }

// 방식 4: isBlank() (Java 11+) - 공백만 있는 경우도 포함
if (s.isBlank()) { }
```

### 2.2 isEmpty() vs isBlank()

이 두 메서드의 차이를 정확히 이해해야 한다.

```java
String empty = "";
String spaces = "   ";
String tabs = "\t\n";
String text = "hello";

// isEmpty(): 길이가 0인지 확인
empty.isEmpty();   // true
spaces.isEmpty();  // false - 공백도 문자다
tabs.isEmpty();    // false
text.isEmpty();    // false

// isBlank(): 길이가 0이거나 공백 문자만 있는지 확인
empty.isBlank();   // true
spaces.isBlank();  // true - 공백만 있으면 blank
tabs.isBlank();    // true - 탭, 줄바꿈도 공백 문자
text.isBlank();    // false
```

`isBlank()`는 `Character.isWhitespace()`가 true인 문자만으로 구성되어 있으면 true를 반환한다.

```java
// String.isBlank() 내부 구현 - OpenJDK 소스
public boolean isBlank() {
    return indexOfNonWhitespace() == length();
}
```

> **출처**: OpenJDK 17 소스 코드, `java.lang.String.isBlank()`
> https://github.com/openjdk/jdk/blob/jdk-17%2B35/src/java.base/share/classes/java/lang/String.java

### 2.3 null과 빈 문자열 동시 체크

실무에서는 null과 빈 문자열을 동시에 체크해야 하는 경우가 많다.

```java
// 직접 구현
public static boolean isEmpty(String s) {
    return s == null || s.isEmpty();
}

public static boolean isBlank(String s) {
    return s == null || s.isBlank();
}

// 사용
if (isEmpty(userInput)) {
    throw new IllegalArgumentException("입력값이 비어있습니다");
}
```

### 2.4 Apache Commons Lang 활용

Apache Commons Lang 라이브러리는 문자열 유틸리티를 제공한다.

```java
import org.apache.commons.lang3.StringUtils;

// null-safe 빈 문자열 체크
StringUtils.isEmpty(null);      // true
StringUtils.isEmpty("");        // true
StringUtils.isEmpty(" ");       // false
StringUtils.isEmpty("hello");   // false

// null-safe blank 체크
StringUtils.isBlank(null);      // true
StringUtils.isBlank("");        // true
StringUtils.isBlank(" ");       // true
StringUtils.isBlank("hello");   // false

// 반대 메서드
StringUtils.isNotEmpty(str);
StringUtils.isNotBlank(str);
```

> **출처**: Apache Commons Lang 3.12 Documentation
> https://commons.apache.org/proper/commons-lang/apidocs/org/apache/commons/lang3/StringUtils.html

### 2.5 Spring Framework의 StringUtils

Spring을 사용한다면 Spring의 StringUtils도 있다.

```java
import org.springframework.util.StringUtils;

// hasLength(): null이 아니고 길이 > 0
StringUtils.hasLength(null);    // false
StringUtils.hasLength("");      // false
StringUtils.hasLength(" ");     // true
StringUtils.hasLength("hello"); // true

// hasText(): null이 아니고 공백이 아닌 문자 포함
StringUtils.hasText(null);      // false
StringUtils.hasText("");        // false
StringUtils.hasText(" ");       // false
StringUtils.hasText("hello");   // true
```

> **출처**: Spring Framework 5.3 API Documentation
> https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/util/StringUtils.html

---

## 3. 문자열 포맷팅 방법 비교

### 3.1 주요 방법들

Java에서 문자열을 조립하는 방법은 여러 가지가 있다.

```java
String name = "Alice";
int age = 30;

// 1. + 연산자
String s1 = "Name: " + name + ", Age: " + age;

// 2. StringBuilder
String s2 = new StringBuilder()
    .append("Name: ").append(name)
    .append(", Age: ").append(age)
    .toString();

// 3. String.format()
String s3 = String.format("Name: %s, Age: %d", name, age);

// 4. MessageFormat
String s4 = MessageFormat.format("Name: {0}, Age: {1}", name, age);

// 5. String.formatted() (Java 15+)
String s5 = "Name: %s, Age: %d".formatted(name, age);
```

### 3.2 성능 비교

각 방법의 성능 특성을 이해해야 적절한 선택을 할 수 있다.

```java
public class FormatPerformanceTest {
    public static void main(String[] args) {
        String name = "Alice";
        int age = 30;
        int iterations = 1_000_000;
        
        // Warm-up
        for (int i = 0; i < 10000; i++) {
            String s = "Name: " + name + ", Age: " + age;
        }
        
        // + 연산자
        long start1 = System.currentTimeMillis();
        for (int i = 0; i < iterations; i++) {
            String s = "Name: " + name + ", Age: " + age;
        }
        System.out.println("+ 연산자: " + (System.currentTimeMillis() - start1) + "ms");
        
        // StringBuilder
        long start2 = System.currentTimeMillis();
        for (int i = 0; i < iterations; i++) {
            String s = new StringBuilder()
                .append("Name: ").append(name)
                .append(", Age: ").append(age)
                .toString();
        }
        System.out.println("StringBuilder: " + (System.currentTimeMillis() - start2) + "ms");
        
        // String.format()
        long start3 = System.currentTimeMillis();
        for (int i = 0; i < iterations; i++) {
            String s = String.format("Name: %s, Age: %d", name, age);
        }
        System.out.println("String.format(): " + (System.currentTimeMillis() - start3) + "ms");
    }
}
```

**일반적인 결과** (환경에 따라 다름):
```
+ 연산자: 50ms
StringBuilder: 45ms
String.format(): 400ms
```

`String.format()`은 포맷 문자열을 파싱하고 Formatter 객체를 생성하는 오버헤드가 있어 상당히 느리다.

### 3.3 언제 무엇을 사용할까?

**+ 연산자 / StringBuilder**:
- 성능이 중요한 경우
- 단순한 문자열 조합
- 반복문 내부 (StringBuilder 권장)

**String.format()**:
- 가독성이 중요한 경우
- 복잡한 포맷 (숫자 형식, 날짜 등)
- 성능이 크게 중요하지 않은 경우
- 국제화(i18n)가 필요한 경우

```java
// 복잡한 포맷팅에는 String.format()이 가독성 좋음
String.format("%-10s | %,15d | %8.2f%%", name, count, percentage);

// 단순 조합은 + 연산자가 간결함
String greeting = "Hello, " + name + "!";
```

### 3.4 포맷 문자열 재사용

`String.format()`을 자주 사용한다면 포맷 문자열을 상수로 정의하는 것이 좋다.

```java
// 나쁜 예: 매번 같은 포맷 문자열 사용
for (User user : users) {
    log(String.format("User: %s, Email: %s", user.getName(), user.getEmail()));
}

// 좋은 예: 포맷 문자열 재사용
private static final String USER_FORMAT = "User: %s, Email: %s";

for (User user : users) {
    log(String.format(USER_FORMAT, user.getName(), user.getEmail()));
}
```

---

## 4. 문자열 연결 안티패턴

### 4.1 안티패턴: 반복문 내 + 연산자

이것은 이미 4단계에서 다뤘지만, 다시 한번 강조한다.

```java
// 나쁜 예: O(n²) 성능
String result = "";
for (String item : items) {
    result += item + ",";  // 매번 새 String 객체 생성
}

// 좋은 예: O(n) 성능
StringBuilder sb = new StringBuilder();
for (String item : items) {
    sb.append(item).append(",");
}
String result = sb.toString();

// 더 좋은 예: String.join() 사용 (Java 8+)
String result = String.join(",", items);
```

### 4.2 안티패턴: 불필요한 toString()

```java
// 나쁜 예: 불필요한 toString() 호출
StringBuilder sb = new StringBuilder();
sb.append(user.getName().toString());  // String에 toString() 불필요
sb.append(String.valueOf(count));      // append(int)가 있으므로 불필요

// 좋은 예
StringBuilder sb = new StringBuilder();
sb.append(user.getName());
sb.append(count);  // int를 직접 append
```

### 4.3 안티패턴: + 연산자로 로그 메시지 생성

```java
// 나쁜 예: 로그 레벨과 관계없이 항상 문자열 연결 수행
logger.debug("Processing user: " + user.getName() + " with id: " + user.getId());

// 좋은 예: 파라미터화된 로깅
logger.debug("Processing user: {} with id: {}", user.getName(), user.getId());
```

파라미터화된 로깅을 사용하면, 해당 로그 레벨이 비활성화되어 있을 때 문자열 연결이 수행되지 않는다.

> **출처**: SLF4J FAQ - What is the fastest way of (not) logging?
> https://www.slf4j.org/faq.html#logging_performance

---

## 5. 정규식 관련 주의점

### 5.1 안티패턴: 반복문 내 정규식 컴파일

```java
// 나쁜 예: 매번 정규식 컴파일
for (String line : lines) {
    if (line.matches("\\d{4}-\\d{2}-\\d{2}")) {  // 매번 Pattern.compile() 호출
        processDate(line);
    }
}

// 좋은 예: 패턴 미리 컴파일
private static final Pattern DATE_PATTERN = Pattern.compile("\\d{4}-\\d{2}-\\d{2}");

for (String line : lines) {
    if (DATE_PATTERN.matcher(line).matches()) {
        processDate(line);
    }
}
```

`String.matches()`, `String.replaceAll()`, `String.split()`은 모두 내부적으로 `Pattern.compile()`을 호출한다.

```java
// String.matches() 내부 구현 - OpenJDK 소스
public boolean matches(String regex) {
    return Pattern.matches(regex, this);
}

// Pattern.matches() 내부 구현
public static boolean matches(String regex, CharSequence input) {
    Pattern p = Pattern.compile(regex);  // 매번 컴파일!
    Matcher m = p.matcher(input);
    return m.matches();
}
```

> **출처**: OpenJDK 17 소스 코드, `java.lang.String.matches()`, `java.util.regex.Pattern`
> https://github.com/openjdk/jdk/blob/jdk-17%2B35/src/java.base/share/classes/java/lang/String.java

### 5.2 정규식 vs 단순 문자열 메서드

정규식이 필요 없는 경우에는 단순 문자열 메서드를 사용하는 것이 훨씬 빠르다.

```java
// 나쁜 예: 단순 치환에 정규식 사용
str.replaceAll("\\.", "-");  // 정규식 컴파일 오버헤드

// 좋은 예: 정규식 불필요한 경우 replace() 사용
str.replace(".", "-");  // 단순 문자열 치환

// 나쁜 예: 단순 분할에 정규식 사용
str.split("\\|");  // 정규식

// 좋은 예: 단일 문자 분할 (최적화 경로 사용)
str.split("\\|");  // 사실 이 경우는 JDK가 최적화함
// 하지만 더 명확하게:
StringTokenizer st = new StringTokenizer(str, "|");
```

### 5.3 split()의 빠른 경로

`split()`은 단일 문자이고 정규식 메타 문자가 아니면 최적화된 경로를 탄다.

```java
// 빠른 경로 (정규식 컴파일 안 함)
"a,b,c".split(",");     // 단일 문자, 메타 문자 아님

// 느린 경로 (정규식 컴파일)
"a.b.c".split("\\.");   // 이스케이프된 메타 문자
"a|b|c".split("\\|");   // 이스케이프된 메타 문자
"a  b".split("\\s+");   // 복잡한 정규식
```

---

## 6. 문자열 비교 시 주의점

### 6.1 == vs equals() 실수

```java
// 흔한 실수
String input = scanner.nextLine();
if (input == "yes") {  // 거의 항상 false!
    proceed();
}

// 올바른 방식
if ("yes".equals(input)) {
    proceed();
}
```

### 6.2 대소문자 무시 비교

```java
// 방법 1: equalsIgnoreCase()
if ("YES".equalsIgnoreCase(input)) {
    proceed();
}

// 방법 2: 변환 후 비교 (비권장 - 새 객체 생성)
if ("yes".equals(input.toLowerCase())) {
    proceed();
}
```

`equalsIgnoreCase()`는 내부적으로 문자 하나씩 대소문자를 무시하고 비교하므로 새 객체를 생성하지 않는다.

### 6.3 로케일 의존적 비교

대소문자 변환이나 비교가 로케일에 따라 다를 수 있다.

```java
// 터키어 로케일에서의 문제
Locale turkishLocale = new Locale("tr", "TR");

String s = "TITLE";
s.toLowerCase();                              // "title" (기본 로케일)
s.toLowerCase(turkishLocale);                 // "tıtle" (터키어: I → ı)

// 로케일 독립적인 비교가 필요하면
s.toLowerCase(Locale.ROOT);                   // 항상 "title"
s.toUpperCase(Locale.ROOT);                   // 항상 "TITLE"
```

> **출처**: Java API Documentation - String.toLowerCase(Locale)
> https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/lang/String.html#toLowerCase(java.util.Locale)

---

## 7. 자주 발생하는 버그 패턴

### 7.1 substring 인덱스 오류

```java
String s = "Hello";

// IndexOutOfBoundsException 발생 케이스
s.substring(10);        // beginIndex > length
s.substring(-1);        // beginIndex < 0
s.substring(3, 2);      // beginIndex > endIndex
s.substring(0, 10);     // endIndex > length

// 안전한 substring
public static String safeSubstring(String s, int begin, int end) {
    if (s == null) return null;
    int len = s.length();
    begin = Math.max(0, begin);
    end = Math.min(len, end);
    if (begin >= end) return "";
    return s.substring(begin, end);
}
```

### 7.2 인코딩 관련 문제

```java
// 문제: 기본 인코딩에 의존
byte[] bytes = str.getBytes();  // 시스템 기본 인코딩 사용

// 해결: 명시적 인코딩 지정
byte[] bytes = str.getBytes(StandardCharsets.UTF_8);

// 반대 방향도 마찬가지
String s = new String(bytes);                           // 위험
String s = new String(bytes, StandardCharsets.UTF_8);   // 안전
```

### 7.3 trim()으로 제거되지 않는 공백

```java
String s = "\u00A0Hello\u00A0";  // non-breaking space

s.trim();   // "\u00A0Hello\u00A0" - 제거 안 됨!
s.strip();  // "Hello" - Java 11+에서는 제거됨

// Java 11 이전에서 유니코드 공백 제거
s.replaceAll("^\\s+|\\s+$", "");  // 정규식 사용
```

`trim()`은 `<= '\u0020'`(공백 문자) 이하만 제거한다. 유니코드 공백 문자들은 제거하지 않는다.

---

## 8. Java 버전별 유용한 메서드

### 8.1 Java 8

```java
// String.join()
String.join(", ", "a", "b", "c");           // "a, b, c"
String.join("-", Arrays.asList("x", "y"));  // "x-y"
```

### 8.2 Java 11

```java
// isBlank() - 공백만 있는지 확인
"   ".isBlank();  // true

// strip(), stripLeading(), stripTrailing() - 유니코드 공백 제거
"  hello  ".strip();          // "hello"
"  hello  ".stripLeading();   // "hello  "
"  hello  ".stripTrailing();  // "  hello"

// lines() - 줄 단위로 Stream 생성
"a\nb\nc".lines().forEach(System.out::println);

// repeat() - 문자열 반복
"ab".repeat(3);  // "ababab"
```

### 8.3 Java 12

```java
// indent() - 들여쓰기 추가/제거
"hello".indent(4);   // "    hello\n"
"    hello".indent(-2);  // "  hello\n"

// transform() - 함수 적용
"hello".transform(String::toUpperCase);  // "HELLO"
```

### 8.4 Java 15

```java
// formatted() - String.format()의 인스턴스 메서드 버전
"Name: %s".formatted("Alice");  // "Name: Alice"

// stripIndent() - 공통 들여쓰기 제거
// 주로 텍스트 블록과 함께 사용
```

### 8.5 Java 15+ 텍스트 블록

```java
// 여러 줄 문자열을 깔끔하게 작성
String json = """
    {
        "name": "Alice",
        "age": 30
    }
    """;

String sql = """
    SELECT *
    FROM users
    WHERE status = 'active'
    """;
```

> **출처**: JEP 378: Text Blocks
> https://openjdk.org/jeps/378

---

## 9. 실습: 안티패턴 찾기

다음 코드에서 문제점을 찾아보자.

```java
public class UserService {
    
    public String formatUserInfo(String name, String email, int age) {
        String result = "";
        result = result + "Name: " + name + "\n";
        result = result + "Email: " + email + "\n";
        result = result + "Age: " + age + "\n";
        return result;
    }
    
    public boolean isValidEmail(String email) {
        if (email == null) {
            return false;
        }
        return email.matches("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}");
    }
    
    public void processUsers(List<User> users) {
        for (User user : users) {
            String log = "Processing: " + user.getName() + " (" + user.getId() + ")";
            logger.debug(log);
            
            if (user.getStatus() == "ACTIVE") {
                // 처리 로직
            }
        }
    }
    
    public String sanitizeInput(String input) {
        if (input == "") {
            return input;
        }
        return input.trim().toLowerCase();
    }
}
```

### 문제점과 개선안

**formatUserInfo()**: `+` 연산자로 여러 번 연결하고 있다. StringBuilder 또는 String.format() 사용 권장.

```java
public String formatUserInfo(String name, String email, int age) {
    return String.format("Name: %s%nEmail: %s%nAge: %d%n", name, email, age);
    // 또는
    return new StringBuilder()
        .append("Name: ").append(name).append("\n")
        .append("Email: ").append(email).append("\n")
        .append("Age: ").append(age).append("\n")
        .toString();
}
```

**isValidEmail()**: 매번 정규식을 컴파일한다. Pattern을 미리 컴파일해야 한다.

```java
private static final Pattern EMAIL_PATTERN = 
    Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}");

public boolean isValidEmail(String email) {
    if (email == null) {
        return false;
    }
    return EMAIL_PATTERN.matcher(email).matches();
}
```

**processUsers()**: `==`로 문자열 비교하고 있고, 로그 메시지를 미리 생성하고 있다.

```java
public void processUsers(List<User> users) {
    for (User user : users) {
        logger.debug("Processing: {} ({})", user.getName(), user.getId());
        
        if ("ACTIVE".equals(user.getStatus())) {
            // 처리 로직
        }
    }
}
```

**sanitizeInput()**: `==`로 빈 문자열 비교하고 있고, null 체크가 없다.

```java
public String sanitizeInput(String input) {
    if (input == null || input.isEmpty()) {
        return input;
    }
    return input.trim().toLowerCase();
}
```

---

## 10. 핵심 정리

null 안전한 문자열 비교를 위해 리터럴을 앞에 두거나(`"literal".equals(var)`), `Objects.equals()`를 사용하는 것이 좋다.

빈 문자열 체크에서 `isEmpty()`는 길이가 0인지 확인하고, `isBlank()`(Java 11+)는 공백만 있는 경우도 포함한다. null도 함께 체크해야 하는 경우가 대부분이다.

문자열 포맷팅에서 `String.format()`은 가독성이 좋지만 성능이 느리다. 성능이 중요하면 StringBuilder나 `+` 연산자를 사용한다.

정규식 사용 시 반복문 내에서 `matches()`, `replaceAll()`, `split()` 호출을 피하고, Pattern을 미리 컴파일해서 재사용해야 한다.

문자열 비교 시 `==` 대신 `equals()`를 사용하고, 대소문자 무시 비교는 `equalsIgnoreCase()`를 사용한다. 로케일 의존적인 변환이 문제가 될 수 있으면 `Locale.ROOT`를 명시한다.

`trim()`은 ASCII 공백만 제거하므로, 유니코드 공백까지 제거하려면 `strip()`(Java 11+)을 사용한다.

---

## 11. 참고 자료

- OpenJDK 17 소스 코드: https://github.com/openjdk/jdk/tree/jdk-17%2B35
- Apache Commons Lang 3.12: https://commons.apache.org/proper/commons-lang/
- Spring Framework StringUtils: https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/util/StringUtils.html
- SLF4J FAQ: https://www.slf4j.org/faq.html
- JEP 378: Text Blocks: https://openjdk.org/jeps/378

---

## 12. 다음 단계 예고

6단계에서는 String 관련 성능 최적화와 심화 주제를 학습한다. String deduplication 기능, StringJoiner와 Collectors.joining()의 활용, 대용량 문자열 처리 전략, 그리고 실제 운영 환경에서 String 관련 메모리 이슈를 진단하는 방법을 다룬다.

---

# 6단계: 성능 최적화와 심화 주제

## 학습 목표

이 문서를 학습하고 나면 JVM의 String Deduplication 기능을 이해하고
적절히 활용할 수 있어야 한다.

StringJoiner와 Collectors.joining()의 내부 동작을 파악하고,
대용량 문자열 처리 시 메모리 효율적인 전략을 선택할 수 있어야 한다.

또한 운영 환경에서 String 관련 메모리 이슈를 진단하는 방법을 익혀야 한다.

---

## 1. String Deduplication (G1 GC)

### 1.1 개념

String Deduplication은 G1 가비지 컬렉터가 제공하는 기능으로,
힙에 있는 중복된 String 객체들의 내부 `char[]`(또는 `byte[]`)를 공유하게 만든다.

`intern()`과 다른 점은, String 객체 자체는 별개로 유지하면서
내부 배열만 공유한다는 것이다.
따라서 기존 코드를 수정하지 않고도 메모리를 절약할 수 있다.

```
intern() 적용 전/후:
┌─────────┐     ┌─────────┐
│ String1 │     │ String1 │──┐
└────┬────┘     └─────────┘  │
     │                       ▼
┌────▼────┐     ┌─────────────┐
│ "Hello" │     │   "Hello"   │  (String Pool)
└─────────┘     └─────────────┘
                             ▲
┌─────────┐     ┌─────────┐  │
│ String2 │     │ String2 │──┘
└────┬────┘     └─────────┘
     │          
┌────▼────┐     (String2 객체는 GC 대상이 될 수 있음)
│ "Hello" │     
└─────────┘     


String Deduplication 적용 전/후:
┌─────────┐              ┌─────────┐
│ String1 │              │ String1 │──┐
└────┬────┘              └─────────┘  │
     │                                │
┌────▼────┐              ┌───────────▼───┐
│ byte[]  │              │    byte[]     │
│ "Hello" │              │    "Hello"    │
└─────────┘              └───────────▲───┘
                                     │
┌─────────┐              ┌─────────┐ │
│ String2 │              │ String2 │─┘
└────┬────┘              └─────────┘
     │                   
┌────▼────┐              (두 String 객체는 유지, 배열만 공유)
│ byte[]  │              
│ "Hello" │              
└─────────┘              
```

### 1.2 활성화 방법

```bash
# G1 GC와 String Deduplication 활성화
java -XX:+UseG1GC -XX:+UseStringDeduplication MyApp

# Java 9+에서는 G1이 기본 GC이므로
java -XX:+UseStringDeduplication MyApp
```

### 1.3 동작 조건

String Deduplication이 동작하려면 몇 가지 조건이 필요하다.

첫째, G1 GC를 사용해야 한다.
다른 GC(Parallel, ZGC, Shenandoah 등)에서는 동작하지 않는다.

둘째, Young GC에서 살아남아 Old Generation으로 승격된 String만 대상이 된다.
짧은 수명의 문자열은 대상이 아니다.

셋째, 백그라운드 스레드에서 비동기적으로 수행되므로
애플리케이션 성능에 미치는 영향은 적다.

### 1.4 모니터링

```bash
# Deduplication 통계 출력
java -XX:+UseG1GC \
     -XX:+UseStringDeduplication \
     -XX:+PrintStringDeduplicationStatistics \
     MyApp
```

출력 예시:
```
[GC concurrent-string-deduplication, 1234.5K->234.5K(1000.0K), avg 78.1%, 0.0123456 secs]
   [Last Coverage: 23456 (100.0% of young gen)]
   [Last Deduplication: 12345 (52.6% of candidates)]
   [Total Coverage: 234567 (100.0% of young gen)]
   [Total Deduplication: 123456 (52.6% of candidates)]
```

> **출처**: Oracle JDK Documentation - G1 Garbage Collector
> https://docs.oracle.com/en/java/javase/17/gctuning/garbage-first-g1-garbage-collector1.html

### 1.5 언제 유용한가?

String Deduplication이 효과적인 시나리오는 다음과 같다.

- 대용량 데이터를 메모리에 캐싱하는 애플리케이션
- 동일한 문자열이 많이 생성되는 웹 애플리케이션 (세션 데이터, 사용자 정보 등)
- CSV, JSON 등을 파싱하여 메모리에 보관하는 경우

반면, 짧은 수명의 문자열이 대부분인 경우에는 효과가 적다.

---

## 2. StringJoiner와 Collectors.joining()

### 2.1 StringJoiner 기본 사용법

Java 8에서 도입된 `StringJoiner`는 구분자, 접두사, 접미사를
지정하여 문자열을 조합할 수 있는 클래스다.

```java
// 기본 사용
StringJoiner sj = new StringJoiner(", ");
sj.add("Apple");
sj.add("Banana");
sj.add("Cherry");
String result = sj.toString();  // "Apple, Banana, Cherry"

// 접두사, 접미사 포함
StringJoiner sjWithBrackets = new StringJoiner(", ", "[", "]");
sjWithBrackets.add("A");
sjWithBrackets.add("B");
sjWithBrackets.add("C");
String result2 = sjWithBrackets.toString();  // "[A, B, C]"

// 빈 경우 기본값 설정
StringJoiner sjEmpty = new StringJoiner(", ", "[", "]");
sjEmpty.setEmptyValue("EMPTY");
String result3 = sjEmpty.toString();  // "EMPTY" (빈 경우)
```

### 2.2 내부 구현

```java
// OpenJDK 소스 기반 (단순화)
public final class StringJoiner {
    private final String prefix;
    private final String delimiter;
    private final String suffix;
    
    private StringBuilder value;  // 내부적으로 StringBuilder 사용
    private String emptyValue;
    
    public StringJoiner add(CharSequence newElement) {
        final String elt = String.valueOf(newElement);
        if (value == null) {
            value = new StringBuilder();
            value.append(prefix);
        } else {
            value.append(delimiter);
        }
        value.append(elt);
        return this;
    }
    
    @Override
    public String toString() {
        if (value == null) {
            return emptyValue != null 
                ? emptyValue 
                : prefix + suffix;
        }
        if (suffix.isEmpty()) {
            return value.toString();
        }
        // suffix 추가 로직...
    }
}
```

> **출처**: OpenJDK 17 소스 코드, `java.util.StringJoiner`
> https://github.com/openjdk/jdk/blob/jdk-17%2B35/src/java.base/share/classes/java/util/StringJoiner.java

내부적으로 `StringBuilder`를 사용하므로 성능이 좋다.
첫 번째 요소 추가 시에만 `StringBuilder`를 생성하여
빈 StringJoiner의 메모리 사용을 최소화한다.

### 2.3 String.join()과의 관계

`String.join()`은 내부적으로 `StringJoiner`를 사용한다.

```java
// String.join() 내부 구현 - OpenJDK 소스
public static String join(CharSequence delimiter, 
                          CharSequence... elements) {
    var delim = delimiter.toString();
    var elems = new String[elements.length];
    for (int i = 0; i < elements.length; i++) {
        elems[i] = String.valueOf(elements[i]);
    }
    return join("", "", delim, elems, elements.length);
}

// Iterable 버전
public static String join(CharSequence delimiter,
                          Iterable<? extends CharSequence> elements) {
    Objects.requireNonNull(delimiter);
    Objects.requireNonNull(elements);
    StringJoiner joiner = new StringJoiner(delimiter);
    for (CharSequence cs : elements) {
        joiner.add(cs);
    }
    return joiner.toString();
}
```

> **출처**: OpenJDK 17 소스 코드, `java.lang.String.join()`

### 2.4 Collectors.joining()

Stream API와 함께 사용할 때는 `Collectors.joining()`이 편리하다.

```java
List<String> names = Arrays.asList("Alice", "Bob", "Charlie");

// 기본 사용
String result1 = names.stream()
    .collect(Collectors.joining());
// "AliceBobCharlie"

// 구분자 지정
String result2 = names.stream()
    .collect(Collectors.joining(", "));
// "Alice, Bob, Charlie"

// 구분자 + 접두사 + 접미사
String result3 = names.stream()
    .collect(Collectors.joining(", ", "[", "]"));
// "[Alice, Bob, Charlie]"
```

### 2.5 성능 비교

```java
public class JoiningPerformanceTest {
    public static void main(String[] args) {
        List<String> items = new ArrayList<>();
        for (int i = 0; i < 10000; i++) {
            items.add("item" + i);
        }
        
        int iterations = 1000;
        
        // StringBuilder
        long start1 = System.currentTimeMillis();
        for (int i = 0; i < iterations; i++) {
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < items.size(); j++) {
                if (j > 0) sb.append(", ");
                sb.append(items.get(j));
            }
            String result = sb.toString();
        }
        System.out.println("StringBuilder: " + 
            (System.currentTimeMillis() - start1) + "ms");
        
        // String.join()
        long start2 = System.currentTimeMillis();
        for (int i = 0; i < iterations; i++) {
            String result = String.join(", ", items);
        }
        System.out.println("String.join(): " + 
            (System.currentTimeMillis() - start2) + "ms");
        
        // Collectors.joining()
        long start3 = System.currentTimeMillis();
        for (int i = 0; i < iterations; i++) {
            String result = items.stream()
                .collect(Collectors.joining(", "));
        }
        System.out.println("Collectors.joining(): " + 
            (System.currentTimeMillis() - start3) + "ms");
    }
}
```

일반적으로 `StringBuilder` ≈ `String.join()` < `Collectors.joining()` 순이다.
Stream의 오버헤드가 있기 때문이다.

하지만 대부분의 실무 상황에서 이 차이는 무시할 수 있는 수준이므로,
가독성을 우선으로 선택해도 된다.

---

## 3. 대용량 문자열 처리 전략

### 3.1 문제 상황

수 MB ~ 수 GB 크기의 텍스트 파일을 처리해야 할 때,
전체를 메모리에 올리면 OutOfMemoryError가 발생할 수 있다.

```java
// 위험한 방식: 전체 파일을 메모리에 로드
String content = Files.readString(Path.of("huge_file.txt"));
// OutOfMemoryError 위험!
```

### 3.2 스트리밍 방식으로 처리

```java
// 줄 단위로 스트리밍 처리
try (Stream<String> lines = Files.lines(Path.of("huge_file.txt"))) {
    lines.filter(line -> line.contains("ERROR"))
         .forEach(this::processErrorLine);
}

// BufferedReader 사용
try (BufferedReader reader = Files.newBufferedReader(
        Path.of("huge_file.txt"), 
        StandardCharsets.UTF_8)) {
    
    String line;
    while ((line = reader.readLine()) != null) {
        processLine(line);
    }
}
```

### 3.3 청크 단위 처리

특정 크기 단위로 나누어 처리하는 방법도 있다.

```java
public void processLargeFile(Path filePath, int chunkSize) 
        throws IOException {
    
    try (FileChannel channel = FileChannel.open(filePath, 
            StandardOpenOption.READ)) {
        
        ByteBuffer buffer = ByteBuffer.allocate(chunkSize);
        StringBuilder leftover = new StringBuilder();
        
        while (channel.read(buffer) != -1) {
            buffer.flip();
            
            String chunk = StandardCharsets.UTF_8
                .decode(buffer)
                .toString();
            
            // 이전 청크의 잘린 부분과 합치기
            String data = leftover.toString() + chunk;
            leftover.setLength(0);
            
            // 마지막 줄바꿈 이후는 다음 청크로
            int lastNewline = data.lastIndexOf('\n');
            if (lastNewline >= 0 && lastNewline < data.length() - 1) {
                leftover.append(data.substring(lastNewline + 1));
                data = data.substring(0, lastNewline + 1);
            }
            
            processChunk(data);
            buffer.clear();
        }
        
        // 남은 데이터 처리
        if (leftover.length() > 0) {
            processChunk(leftover.toString());
        }
    }
}
```

### 3.4 메모리 매핑 파일 사용

매우 큰 파일을 처리할 때는 메모리 매핑 파일(Memory-Mapped File)을
고려할 수 있다.

```java
public void searchInLargeFile(Path filePath, String searchTerm) 
        throws IOException {
    
    try (FileChannel channel = FileChannel.open(filePath, 
            StandardOpenOption.READ)) {
        
        long fileSize = channel.size();
        
        // 파일을 메모리에 매핑 (실제로 전체를 로드하지 않음)
        MappedByteBuffer buffer = channel.map(
            FileChannel.MapMode.READ_ONLY, 
            0, 
            fileSize
        );
        
        // CharBuffer로 변환하여 검색
        CharBuffer charBuffer = StandardCharsets.UTF_8
            .decode(buffer);
        
        // Pattern으로 검색
        Pattern pattern = Pattern.compile(
            Pattern.quote(searchTerm)
        );
        Matcher matcher = pattern.matcher(charBuffer);
        
        while (matcher.find()) {
            System.out.println("Found at position: " + matcher.start());
        }
    }
}
```

메모리 매핑의 장점은 OS가 필요한 부분만 메모리에 로드하고,
자주 접근하는 부분을 캐싱한다는 것이다.

> **출처**: Java API Documentation - MappedByteBuffer
> https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/nio/MappedByteBuffer.html

---

## 4. 인코딩과 문자셋 심화

### 4.1 Charset 선택의 중요성

```java
// 시스템 기본 인코딩에 의존 (위험!)
byte[] bytes1 = "한글".getBytes();

// 명시적 인코딩 지정 (권장)
byte[] bytes2 = "한글".getBytes(StandardCharsets.UTF_8);

// 잘못된 인코딩으로 읽으면 깨짐
String wrong = new String(bytes2, StandardCharsets.ISO_8859_1);
// "íêµ­ì´" 같은 깨진 문자
```

### 4.2 BOM(Byte Order Mark) 처리

UTF-8 파일에 BOM이 포함된 경우가 있다.
Windows에서 생성된 파일에서 자주 발생한다.

```java
public String readFileWithBomHandling(Path path) throws IOException {
    byte[] bytes = Files.readAllBytes(path);
    
    // UTF-8 BOM 체크 (EF BB BF)
    if (bytes.length >= 3 && 
        (bytes[0] & 0xFF) == 0xEF && 
        (bytes[1] & 0xFF) == 0xBB && 
        (bytes[2] & 0xFF) == 0xBF) {
        
        return new String(bytes, 3, bytes.length - 3, 
            StandardCharsets.UTF_8);
    }
    
    return new String(bytes, StandardCharsets.UTF_8);
}
```

### 4.3 서로게이트 페어와 코드 포인트

Java의 `char`는 16비트이므로 BMP(Basic Multilingual Plane) 외부의
문자(이모지 등)를 표현하려면 서로게이트 페어가 필요하다.

```java
String emoji = "😀";  // U+1F600

// 잘못된 이해
emoji.length();        // 2 (char 개수, 서로게이트 페어)
emoji.charAt(0);       // '\uD83D' (High Surrogate)
emoji.charAt(1);       // '\uDE00' (Low Surrogate)

// 올바른 이해
emoji.codePointCount(0, emoji.length());  // 1 (실제 문자 수)
emoji.codePointAt(0);                      // 128512 (0x1F600)

// 코드 포인트 단위로 순회
emoji.codePoints().forEach(cp -> {
    System.out.println("Code point: " + cp);
    System.out.println("Character: " + Character.toString(cp));
});
```

### 4.4 문자열 길이의 함정

```java
public class StringLengthTest {
    public static void main(String[] args) {
        String text = "Hello 😀 World";
        
        System.out.println("length(): " + text.length());
        // 14 (char 개수)
        
        System.out.println("codePointCount(): " + 
            text.codePointCount(0, text.length()));
        // 13 (실제 문자 수)
        
        System.out.println("bytes (UTF-8): " + 
            text.getBytes(StandardCharsets.UTF_8).length);
        // 17 (UTF-8 바이트 수: ASCII 12개 + 이모지 4개 + 공백 1개)
        
        System.out.println("bytes (UTF-16): " + 
            text.getBytes(StandardCharsets.UTF_16).length);
        // 30 (UTF-16 바이트 수: BOM 2개 + 14 char × 2)
    }
}
```

---

## 5. JVM 튜닝 옵션

### 5.1 String 관련 JVM 옵션 정리

```bash
# String Pool 크기 설정 (버킷 수)
-XX:StringTableSize=60013

# String Pool 통계 출력
-XX:+PrintStringTableStatistics

# Compact Strings 비활성화 (거의 사용할 일 없음)
-XX:-CompactStrings

# String Deduplication 활성화 (G1 GC 필요)
-XX:+UseStringDeduplication

# String Deduplication 통계 출력
-XX:+PrintStringDeduplicationStatistics

# String Deduplication 대상 최소 수명 (기본값: 3)
-XX:StringDeduplicationAgeThreshold=3
```

### 5.2 StringTableSize 튜닝

StringTable 크기가 너무 작으면 해시 충돌이 많아져
`intern()` 성능이 저하된다.

```bash
# 현재 StringTable 상태 확인
java -XX:+PrintStringTableStatistics -version

# 출력 예시
StringTable statistics:
Number of buckets       :     65536 =    524288 bytes, each 8
Number of entries       :     25094 =    401504 bytes, each 16
Number of literals      :     25094 =   1952224 bytes, avg  77.000
Total footprint         :           =   2878016 bytes
Average bucket size     :     0.383
Variance of bucket size :     0.385
Std. dev. of bucket size:     0.621
Maximum bucket size     :         4
```

`Average bucket size`가 1 이상이면
StringTableSize를 늘리는 것을 고려해볼 수 있다.

```bash
# 더 큰 StringTable 사용
java -XX:StringTableSize=120121 MyApp
```

---

## 6. 메모리 이슈 진단

### 6.1 힙 덤프 분석

String 관련 메모리 문제가 의심될 때 힙 덤프를 분석할 수 있다.

```bash
# 힙 덤프 생성
jmap -dump:format=b,file=heap.hprof <pid>

# OutOfMemoryError 시 자동 덤프
java -XX:+HeapDumpOnOutOfMemoryError \
     -XX:HeapDumpPath=/path/to/dumps/ \
     MyApp
```

### 6.2 Eclipse MAT로 분석

Eclipse Memory Analyzer Tool(MAT)을 사용하면
힙 덤프를 시각적으로 분석할 수 있다.

주요 확인 포인트는 다음과 같다.

1. **Dominator Tree**: String 객체가 차지하는 메모리 비율 확인
2. **Histogram**: String, char[], byte[] 개수와 크기 확인
3. **Duplicate Classes**: 중복 문자열 분석

> **출처**: Eclipse Memory Analyzer Documentation
> https://www.eclipse.org/mat/

### 6.3 jcmd를 이용한 실시간 분석

```bash
# String 관련 통계
jcmd <pid> VM.stringtable

# 클래스별 인스턴스 수와 메모리 사용량
jcmd <pid> GC.class_histogram | grep -E "String|char\[\]|byte\[\]"
```

### 6.4 메모리 누수 패턴

String 관련 메모리 누수의 대표적인 패턴을 알아두면
진단이 쉬워진다.

**패턴 1: 과도한 intern() 사용**
```java
// 고유한 문자열을 계속 intern() → StringTable 비대화
for (String line : lines) {
    String interned = line.intern();  // 위험!
    cache.put(interned, data);
}
```

**패턴 2: 캐시에서 문자열 참조 유지**
```java
// 캐시가 계속 커지면서 String 참조 유지
private Map<String, Data> cache = new HashMap<>();

public void process(String key, Data data) {
    cache.put(key, data);  // 제거 로직이 없으면 계속 쌓임
}
```

**패턴 3: 클로저에서 큰 문자열 캡처**
```java
String hugeContent = Files.readString(largefile);

// 람다가 hugeContent를 캡처하여 참조 유지
executor.submit(() -> {
    // hugeContent의 일부만 사용해도 전체가 메모리에 유지됨
    processFirstLine(hugeContent.split("\n")[0]);
});
```

---

## 7. 실무 최적화 체크리스트

### 7.1 코드 레벨

아래 체크리스트를 코드 리뷰 시 활용할 수 있다.

- [ ] 반복문 내에서 `+` 연산자로 문자열 연결하고 있지 않은가?
- [ ] `matches()`, `replaceAll()`, `split()`을 반복 호출하고 있지 않은가?
- [ ] 문자열 비교 시 `==` 대신 `equals()`를 사용하고 있는가?
- [ ] null 가능성이 있는 문자열을 안전하게 처리하고 있는가?
- [ ] 대용량 파일을 한 번에 읽지 않고 스트리밍으로 처리하고 있는가?
- [ ] 인코딩을 명시적으로 지정하고 있는가?
- [ ] 로그 메시지를 파라미터화하여 불필요한 문자열 생성을 피하고 있는가?

### 7.2 JVM 레벨

- [ ] String Deduplication이 도움이 될 수 있는 환경인가?
- [ ] StringTableSize가 적절하게 설정되어 있는가?
- [ ] 메모리 사용량 모니터링이 설정되어 있는가?

### 7.3 아키텍처 레벨

- [ ] 대용량 텍스트 처리 시 스트리밍 아키텍처를 사용하고 있는가?
- [ ] 문자열 캐싱이 필요한 경우 적절한 캐시 정책(LRU 등)을 사용하고 있는가?
- [ ] 외부 시스템과의 인코딩 불일치 가능성을 고려했는가?

---

## 8. 실습: 성능 측정 및 분석

### 실습 8.1: String Deduplication 효과 측정

```java
import java.util.ArrayList;
import java.util.List;

public class DeduplicationTest {
    public static void main(String[] args) throws InterruptedException {
        List<String> strings = new ArrayList<>();
        
        // 중복 문자열 대량 생성
        for (int i = 0; i < 1_000_000; i++) {
            // new String()으로 별도 객체 생성
            strings.add(new String("DuplicatedString_" + (i % 100)));
        }
        
        System.out.println("Strings created: " + strings.size());
        System.out.println("Press Enter to trigger GC and check memory...");
        System.in.read();
        
        System.gc();
        Thread.sleep(5000);  // Deduplication 시간 확보
        
        System.out.println("GC completed. Check memory usage.");
        System.in.read();
    }
}
```

실행 비교:
```bash
# Deduplication 없이
java -Xmx512m DeduplicationTest

# Deduplication 활성화
java -Xmx512m \
     -XX:+UseG1GC \
     -XX:+UseStringDeduplication \
     -XX:+PrintStringDeduplicationStatistics \
     DeduplicationTest
```

### 실습 8.2: StringTable 충돌 확인

```java
public class StringTableTest {
    public static void main(String[] args) {
        // 대량의 고유 문자열 intern
        long start = System.currentTimeMillis();
        
        for (int i = 0; i < 1_000_000; i++) {
            String s = ("unique_string_" + i).intern();
        }
        
        System.out.println("Time: " + 
            (System.currentTimeMillis() - start) + "ms");
    }
}
```

비교 실행:
```bash
# 작은 StringTable
java -XX:StringTableSize=1009 \
     -XX:+PrintStringTableStatistics \
     StringTableTest

# 큰 StringTable  
java -XX:StringTableSize=1000003 \
     -XX:+PrintStringTableStatistics \
     StringTableTest
```

---

## 9. 핵심 정리

String Deduplication은 G1 GC에서 중복 문자열의 내부 배열을
공유하게 하여 메모리를 절약한다.
코드 수정 없이 JVM 옵션만으로 활성화할 수 있다.

StringJoiner와 Collectors.joining()은 내부적으로 StringBuilder를 사용하며,
구분자와 접두사/접미사를 깔끔하게 처리할 수 있다.

대용량 문자열 처리 시에는 전체를 메모리에 올리지 말고
스트리밍 방식이나 청크 단위로 처리해야 한다.
필요하면 메모리 매핑 파일을 활용할 수 있다.

이모지 등 BMP 외부 문자를 다룰 때는 `length()` 대신
`codePointCount()`를 사용해야 정확한 문자 수를 얻을 수 있다.

메모리 이슈 진단에는 힙 덤프 분석(MAT), jcmd,
StringTable 통계 출력 등을 활용할 수 있다.

---

## 10. 참고 자료

- OpenJDK 17 소스 코드:
  https://github.com/openjdk/jdk/tree/jdk-17%2B35
- Oracle G1 GC Tuning Guide:
  https://docs.oracle.com/en/java/javase/17/gctuning/
- Eclipse Memory Analyzer:
  https://www.eclipse.org/mat/
- JEP 192: String Deduplication in G1:
  https://openjdk.org/jeps/192

---

## 11. 다음 단계 예고

7단계(마지막)에서는 지금까지 배운 내용을 종합하여
실제 면접에서 자주 나오는 String 관련 질문들을 정리하고,
각 질문에 대한 모범 답변 전략을 학습한다.
