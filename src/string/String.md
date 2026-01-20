# 1단계: String의 본질과 설계 철학

## 학습 목표

이 문서를 학습하고 나면 불변 객체(Immutable Object)의 정의와 특성을 명확히 설명할 수 있어야 한다. 또한 Java 설계자들이 String을 불변으로 만든 이유를 네 가지 관점에서 논리적으로 설명할 수 있어야 하며, 불변성이 실제 코드에서 어떻게 동작하는지 예제를 통해 증명할 수 있어야 한다.

---

## 1. 핵심 질문: 왜 String은 불변인가?

Java를 처음 배울 때 많은 개발자들이 String의 불변성을 단순한 사실로 받아들인다. 하지만 이 설계 결정 뒤에는 깊은 고민이 있다. String은 Java에서 가장 많이 사용되는 자료형이며, 거의 모든 프로그램에서 핵심적인 역할을 한다. 이렇게 중요한 클래스를 불변으로 설계한 데는 분명한 이유가 있다.

불변 객체란 한번 생성되면 그 상태(내부 데이터)가 절대 변경되지 않는 객체를 말한다. String의 경우, 문자열 `"Hello"`를 담고 있는 String 객체는 생성된 이후 그 내용이 `"Hello World"`나 다른 어떤 값으로도 바뀌지 않는다.

```java
String greeting = "Hello";
greeting = greeting + " World";  // greeting이 가리키는 객체가 변경된 것이 아님
```

위 코드에서 실제로 일어나는 일을 이해하는 것이 중요하다. `"Hello"` 객체 자체가 `"Hello World"`로 변경되는 것이 아니다. 대신 완전히 새로운 `"Hello World"` 객체가 메모리에 생성되고, `greeting` 변수가 이 새 객체를 가리키도록 참조가 변경된다. 원래의 `"Hello"` 객체는 메모리 어딘가에 그대로 남아있다(더 이상 참조되지 않으면 나중에 가비지 컬렉션된다).

---

## 2. String 클래스의 불변성 보장 메커니즘

String 클래스가 어떻게 불변성을 보장하는지 내부 구조를 살펴보자.

### 2.1 클래스 선언: final 키워드

```java
public final class String
    implements java.io.Serializable, Comparable<String>, CharSequence {
    // ...
}
```

String 클래스 자체가 `final`로 선언되어 있다. 이는 String 클래스를 상속받아 하위 클래스를 만들 수 없다는 의미다. 만약 상속이 가능했다면, 악의적인 개발자가 String을 상속받아 내부 데이터를 변경할 수 있는 메서드를 추가한 `MutableString` 같은 클래스를 만들 수 있었을 것이다. `final` 키워드는 이러한 가능성을 원천 차단한다.

### 2.2 내부 데이터: private final 배열

```java
// Java 8 이전
private final char[] value;

// Java 9 이후
private final byte[] value;
private final byte coder;
```

내부에서 실제 문자 데이터를 저장하는 배열이 `private final`로 선언되어 있다. `private`은 외부에서 이 배열에 직접 접근하는 것을 막고, `final`은 이 참조 변수가 다른 배열을 가리키도록 재할당되는 것을 막는다.

여기서 한 가지 의문이 생길 수 있다. 배열 참조가 `final`이라고 해서 배열의 내용까지 불변이 되는 것은 아니다. 다음 코드를 보자.

```java
final char[] arr = {'H', 'e', 'l', 'l', 'o'};
arr[0] = 'J';  // 이것은 가능하다! arr이 가리키는 배열의 내용 변경
arr = new char[10];  // 이것은 컴파일 에러! final 참조 재할당 불가
```

그렇다면 String 내부의 `value` 배열 내용도 변경될 수 있지 않을까? 이를 막기 위해 String 클래스는 두 가지 추가적인 보호 장치를 사용한다.

첫째, `value` 배열이 `private`이므로 외부에서 직접 접근할 수 없다. 둘째, String 클래스의 어떤 public 메서드도 이 배열의 내용을 수정하지 않는다. 모든 "변경"처럼 보이는 메서드(`concat()`, `replace()`, `substring()` 등)는 실제로 새로운 String 객체를 생성하여 반환한다.

### 2.3 방어적 복사(Defensive Copy)

String 클래스는 생성자에서 외부로부터 받은 배열을 그대로 사용하지 않고 복사한다.

```java
public String(char value[]) {
    this.value = Arrays.copyOf(value, value.length);  // 복사본 생성
}
```

만약 복사하지 않고 전달받은 배열을 그대로 사용했다면, 외부에서 원본 배열을 수정하여 String의 내용을 간접적으로 변경할 수 있었을 것이다.

```java
// 만약 방어적 복사가 없었다면 (가상의 취약한 구현)
char[] chars = {'H', 'e', 'l', 'l', 'o'};
String s = new String(chars);  // chars 배열을 그대로 내부에서 사용한다고 가정
chars[0] = 'J';  // 이렇게 하면 s의 내용도 "Jello"로 바뀌어 버린다!

// 실제 String은 방어적 복사를 하므로 위 문제가 발생하지 않음
```

---

## 3. 불변성의 네 가지 이점

### 3.1 스레드 안전성(Thread Safety)

불변 객체는 태생적으로 스레드 안전하다. 여러 스레드가 동시에 같은 String 객체에 접근해도, 어떤 스레드도 그 내용을 변경할 수 없기 때문에 동기화(synchronization) 없이도 안전하게 공유할 수 있다.

```java
public class ThreadSafetyExample {
    // 이 String은 여러 스레드가 동시에 읽어도 안전하다
    private static final String SHARED_CONFIG = "database.url=localhost:5432";
    
    public void processInMultipleThreads() {
        // 스레드 1
        new Thread(() -> {
            String url = SHARED_CONFIG;  // 안전하게 읽기
            System.out.println("Thread 1: " + url);
        }).start();
        
        // 스레드 2
        new Thread(() -> {
            String url = SHARED_CONFIG;  // 동시에 읽어도 문제없음
            System.out.println("Thread 2: " + url);
        }).start();
        
        // 동기화 코드가 전혀 필요 없다
    }
}
```

만약 String이 가변이었다면, 한 스레드가 문자열을 읽는 도중에 다른 스레드가 그 내용을 수정할 수 있어서 예측 불가능한 결과가 발생할 수 있었을 것이다. 불변성 덕분에 String을 사용하는 코드에서는 이런 걱정을 할 필요가 없다.

### 3.2 해시코드 캐싱(Hash Code Caching)

String은 `hashCode()` 메서드의 결과를 내부에 캐싱한다. 한 번 계산된 해시코드는 다시 계산할 필요가 없다.

```java
public final class String {
    private int hash;  // 캐싱된 해시코드, 기본값 0
    
    public int hashCode() {
        int h = hash;
        if (h == 0 && value.length > 0) {
            // 해시코드 계산 (O(n) 연산)
            for (int i = 0; i < value.length; i++) {
                h = 31 * h + value[i];
            }
            hash = h;  // 캐싱
        }
        return h;
    }
}
```

이 최적화가 가능한 이유는 String이 불변이기 때문이다. 내용이 절대 변하지 않으므로 해시코드도 절대 변하지 않는다. 처음 한 번만 O(n) 시간을 들여 계산하면, 이후에는 O(1) 시간에 캐싱된 값을 반환할 수 있다.

이것이 특히 중요한 이유는 String이 HashMap의 키로 매우 자주 사용되기 때문이다. HashMap에서 키를 검색할 때마다 `hashCode()`가 호출되는데, 만약 매번 O(n) 시간이 걸린다면 HashMap의 O(1) 평균 시간 복잡도가 무의미해질 것이다.

```java
// HashMap에서 String 키의 효율성
Map<String, User> userCache = new HashMap<>();
String key = "user_12345_session_abc";  // 긴 문자열

// 첫 번째 접근: hashCode() 계산 (O(n))
userCache.put(key, new User("John"));

// 이후 모든 접근: 캐싱된 hashCode() 사용 (O(1))
User user = userCache.get(key);  // 빠르다!
```

### 3.3 보안성(Security)

String의 불변성은 보안 측면에서도 중요하다. 데이터베이스 연결 문자열, 파일 경로, 네트워크 주소 등 민감한 정보가 String으로 전달될 때, 그 값이 중간에 변조되지 않음을 보장할 수 있다.

```java
public class SecurityExample {
    
    public void connectToDatabase(String connectionUrl) {
        // 보안 검증
        if (!isValidUrl(connectionUrl)) {
            throw new SecurityException("Invalid URL");
        }
        
        // 만약 String이 가변이었다면, 여기서 다른 스레드가
        // connectionUrl의 내용을 악의적인 URL로 변경할 수 있었을 것이다.
        // 예: "jdbc:mysql://trusted-server" → "jdbc:mysql://malicious-server"
        
        // String이 불변이므로, 위에서 검증한 URL이
        // 아래에서 사용될 때도 동일함이 보장된다.
        establishConnection(connectionUrl);  // 안전!
    }
    
    public void loadFile(String filePath) {
        // 권한 검사
        if (!hasPermission(filePath)) {
            throw new SecurityException("Access denied");
        }
        
        // 불변성 덕분에 filePath가 권한 검사 이후에 변경되지 않음
        // "/home/user/safe.txt" → "/etc/passwd" 같은 변조 불가능
        readFile(filePath);  // 안전!
    }
}
```

이를 TOCTOU(Time Of Check to Time Of Use) 취약점 방지라고 한다. 검사 시점과 사용 시점 사이에 값이 변경되는 공격을 불변성이 원천 차단한다.

### 3.4 String Pool 활용

String이 불변이기 때문에 Java는 String Pool이라는 최적화 기법을 사용할 수 있다. 동일한 내용의 문자열 리터럴은 메모리에 단 하나만 존재하고, 여러 참조가 이를 공유한다.

```java
String s1 = "Hello";  // String Pool에 "Hello" 생성
String s2 = "Hello";  // Pool에서 기존 "Hello" 재사용

System.out.println(s1 == s2);  // true - 같은 객체를 참조
```

만약 String이 가변이었다면, 이런 공유가 불가능했을 것이다. `s1`을 통해 내용을 변경하면 `s2`에도 영향을 미치는 심각한 버그가 발생할 수 있기 때문이다.

```java
// 만약 String이 가변이었다면 (가상의 시나리오)
String s1 = "Hello";
String s2 = "Hello";  // s1과 같은 객체 공유

s1.setCharAt(0, 'J');  // s1을 "Jello"로 변경하려 했는데...
System.out.println(s2);  // "Jello"가 출력됨! 의도치 않은 부작용

// 불변 String은 이런 문제가 원천적으로 불가능
```

---

## 4. 실습: 불변성 확인하기

다음 코드를 직접 실행해보고 결과를 예측해보자.

### 실습 4.1: 참조 변경 vs 객체 변경

```java
public class ImmutabilityTest {
    public static void main(String[] args) {
        String original = "Hello";
        String modified = original.concat(" World");
        
        System.out.println("original: " + original);      // 결과는?
        System.out.println("modified: " + modified);      // 결과는?
        System.out.println("같은 객체? " + (original == modified));  // 결과는?
    }
}
```

예상 결과를 적어본 후 실행해서 확인해보자. `concat()` 메서드가 원본을 변경하는 것이 아니라 새 객체를 반환한다는 것을 확인할 수 있다.

### 실습 4.2: 해시코드 캐싱 확인

```java
public class HashCodeCachingTest {
    public static void main(String[] args) {
        String longString = "a".repeat(1_000_000);  // 100만 글자
        
        // 첫 번째 호출: 해시코드 계산
        long start1 = System.nanoTime();
        int hash1 = longString.hashCode();
        long time1 = System.nanoTime() - start1;
        
        // 두 번째 호출: 캐싱된 값 반환
        long start2 = System.nanoTime();
        int hash2 = longString.hashCode();
        long time2 = System.nanoTime() - start2;
        
        System.out.println("첫 번째 호출 시간: " + time1 + "ns");
        System.out.println("두 번째 호출 시간: " + time2 + "ns");
        System.out.println("속도 향상: " + (time1 / (double) time2) + "배");
    }
}
```

두 번째 호출이 첫 번째보다 훨씬 빠른 것을 확인할 수 있다.

### 실습 4.3: String Pool 동작 확인

```java
public class StringPoolTest {
    public static void main(String[] args) {
        String literal1 = "Hello";
        String literal2 = "Hello";
        String newString = new String("Hello");
        String interned = newString.intern();
        
        System.out.println("literal1 == literal2: " + (literal1 == literal2));
        System.out.println("literal1 == newString: " + (literal1 == newString));
        System.out.println("literal1 == interned: " + (literal1 == interned));
        System.out.println("literal1.equals(newString): " + literal1.equals(newString));
    }
}
```

`==` 연산자와 `equals()` 메서드의 차이, 그리고 `intern()` 메서드의 역할을 이해할 수 있다.

---

## 5. 핵심 정리

String이 불변으로 설계된 이유는 네 가지로 요약된다.

첫째, 스레드 안전성이다. 불변 객체는 여러 스레드가 동시에 접근해도 상태가 변하지 않으므로 동기화 없이 안전하게 공유할 수 있다.

둘째, 해시코드 캐싱이다. 내용이 변하지 않으므로 해시코드를 한 번만 계산하여 캐싱할 수 있고, 이는 HashMap에서 String을 키로 사용할 때 큰 성능 이점을 제공한다.

셋째, 보안성이다. 검증된 문자열 값이 사용 시점까지 변조되지 않음을 보장하여 TOCTOU 취약점을 방지한다.

넷째, String Pool 활용이다. 동일한 내용의 문자열을 여러 참조가 안전하게 공유할 수 있어 메모리를 절약한다.

String 클래스는 `final` 클래스 선언, `private final` 필드, 방어적 복사, 상태를 변경하지 않는 메서드 설계를 통해 불변성을 보장한다.

---

## 6. 다음 단계 예고

2단계에서는 String의 내부 구현이 Java 버전에 따라 어떻게 변화했는지 살펴본다. 특히 Java 9에서 도입된 Compact Strings 기능이 메모리 효율성을 어떻게 개선했는지, 그리고 이를 위해 내부 자료구조가 `char[]`에서 `byte[]`로 어떻게 변경되었는지 학습한다.