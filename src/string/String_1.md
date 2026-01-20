# 1단계: String의 본질과 설계 철학

## 학습 목표

이 문서를 학습하고 나면 불변 객체(Immutable Object)의 정의와 특성을 명확히 설명할 수 있어야 한다. 또한 Java 설계자들이 String을 불변으로 만든 이유를 네 가지 관점에서 논리적으로 설명할 수
있어야 하며, 불변성이 실제 코드에서 어떻게 동작하는지 예제를 통해 증명할 수 있어야 한다.

---

## 1. 핵심 질문: 왜 String은 불변인가?

Java를 처음 배울 때 많은 개발자들이 String의 불변성을 단순한 사실로 받아들인다. 하지만 이 설계 결정 뒤에는 깊은 고민이 있다. String은 Java에서 가장 많이 사용되는 자료형이며, 거의 모든
프로그램에서 핵심적인 역할을 한다. 이렇게 중요한 클래스를 불변으로 설계한 데는 분명한 이유가 있다.

불변 객체란 한번 생성되면 그 상태(내부 데이터)가 절대 변경되지 않는 객체를 말한다. String의 경우, 문자열 `"Hello"`를 담고 있는 String 객체는 생성된 이후 그 내용이
`"Hello World"`나 다른 어떤 값으로도 바뀌지 않는다.

```java
String greeting = "Hello";
greeting =greeting +" World";  // greeting이 가리키는 객체가 변경된 것이 아님
```

위 코드에서 실제로 일어나는 일을 이해하는 것이 중요하다. `"Hello"` 객체 자체가 `"Hello World"`로 변경되는 것이 아니다. 대신 완전히 새로운 `"Hello World"` 객체가 메모리에
생성되고, `greeting` 변수가 이 새 객체를 가리키도록 참조가 변경된다. 원래의 `"Hello"` 객체는 메모리 어딘가에 그대로 남아있다(더 이상 참조되지 않으면 나중에 가비지 컬렉션된다).

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

String 클래스 자체가 `final`로 선언되어 있다. 이는 String 클래스를 상속받아 하위 클래스를 만들 수 없다는 의미다. 만약 상속이 가능했다면, 악의적인 개발자가 String을 상속받아 내부 데이터를
변경할 수 있는 메서드를 추가한 `MutableString` 같은 클래스를 만들 수 있었을 것이다. `final` 키워드는 이러한 가능성을 원천 차단한다.

### 2.2 내부 데이터: private final 배열

```java
// Java 8 이전
private final char[] value;

// Java 9 이후
private final byte[] value;
private final byte coder;
```

내부에서 실제 문자 데이터를 저장하는 배열이 `private final`로 선언되어 있다. `private`은 외부에서 이 배열에 직접 접근하는 것을 막고, `final`은 이 참조 변수가 다른 배열을 가리키도록
재할당되는 것을 막는다.

여기서 한 가지 의문이 생길 수 있다. 배열 참조가 `final`이라고 해서 배열의 내용까지 불변이 되는 것은 아니다. 다음 코드를 보자.

```java
final char[] arr = {'H', 'e', 'l', 'l', 'o'};
arr[0]='J';  // 이것은 가능하다! arr이 가리키는 배열의 내용 변경
arr =new char[10];  // 이것은 컴파일 에러! final 참조 재할당 불가
```

그렇다면 String 내부의 `value` 배열 내용도 변경될 수 있지 않을까? 이를 막기 위해 String 클래스는 두 가지 추가적인 보호 장치를 사용한다.

첫째, `value` 배열이 `private`이므로 외부에서 직접 접근할 수 없다. 둘째, String 클래스의 어떤 public 메서드도 이 배열의 내용을 수정하지 않는다. 모든 "변경"처럼 보이는 메서드(
`concat()`, `replace()`, `substring()` 등)는 실제로 새로운 String 객체를 생성하여 반환한다.

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
chars[0]='J';  // 이렇게 하면 s의 내용도 "Jello"로 바뀌어 버린다!

// 실제 String은 방어적 복사를 하므로 위 문제가 발생하지 않음
```

---

## 3. 불변성의 네 가지 이점

### 3.1 스레드 안전성(Thread Safety)

불변 객체는 태생적으로 스레드 안전하다. 여러 스레드가 동시에 같은 String 객체에 접근해도, 어떤 스레드도 그 내용을 변경할 수 없기 때문에 동기화(synchronization) 없이도 안전하게 공유할 수
있다.

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

만약 String이 가변이었다면, 한 스레드가 문자열을 읽는 도중에 다른 스레드가 그 내용을 수정할 수 있어서 예측 불가능한 결과가 발생할 수 있었을 것이다. 불변성 덕분에 String을 사용하는 코드에서는 이런
걱정을 할 필요가 없다.

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

이 최적화가 가능한 이유는 String이 불변이기 때문이다. 내용이 절대 변하지 않으므로 해시코드도 절대 변하지 않는다. 처음 한 번만 O(n) 시간을 들여 계산하면, 이후에는 O(1) 시간에 캐싱된 값을 반환할 수
있다.

이것이 특히 중요한 이유는 String이 HashMap의 키로 매우 자주 사용되기 때문이다. HashMap에서 키를 검색할 때마다 `hashCode()`가 호출되는데, 만약 매번 O(n) 시간이 걸린다면
HashMap의 O(1) 평균 시간 복잡도가 무의미해질 것이다.

```java
// HashMap에서 String 키의 효율성
Map<String, User> userCache = new HashMap<>();
String key = "user_12345_session_abc";  // 긴 문자열

// 첫 번째 접근: hashCode() 계산 (O(n))
userCache.

put(key, new User("John"));

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

System.out.

println(s1 ==s2);  // true - 같은 객체를 참조
```

만약 String이 가변이었다면, 이런 공유가 불가능했을 것이다. `s1`을 통해 내용을 변경하면 `s2`에도 영향을 미치는 심각한 버그가 발생할 수 있기 때문이다.

```java
// 만약 String이 가변이었다면 (가상의 시나리오)
String s1 = "Hello";
String s2 = "Hello";  // s1과 같은 객체 공유

s1.

setCharAt(0,'J');  // s1을 "Jello"로 변경하려 했는데...
System.out.

println(s2);  // "Jello"가 출력됨! 의도치 않은 부작용

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
		System.out.println("속도 향상: " + (time1 / (double)time2) + "배");
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

2단계에서는 String의 내부 구현이 Java 버전에 따라 어떻게 변화했는지 살펴본다. 특히 Java 9에서 도입된 Compact Strings 기능이 메모리 효율성을 어떻게 개선했는지, 그리고 이를 위해 내부
자료구조가 `char[]`에서 `byte[]`로 어떻게 변경되었는지 학습한다.

---

# 2단계: 내부 구현의 진화

## 학습 목표

이 문서를 학습하고 나면 Java 8 이전과 Java 9 이후의 String 내부 구조 차이를 명확히 설명할 수 있어야 한다. Compact Strings가 도입된 배경과 메모리 효율성 개선 원리를 이해하고,
Latin-1과 UTF-16 인코딩이 어떤 기준으로 선택되는지 설명할 수 있어야 한다. 또한 이 변화가 실제 애플리케이션 성능에 미치는 영향을 정량적으로 파악할 수 있어야 한다.

---

## 1. 핵심 질문: Java 9에서 String 내부 구조를 왜 바꿨을까?

Java 9에서 String의 내부 구현이 `char[]`에서 `byte[]`로 변경되었다. 이것은 단순한 리팩토링이 아니라 JEP 254(Compact Strings)라는 정식 제안을 통해 도입된 중요한 변화다.
왜 Java 개발팀은 20년 넘게 잘 동작하던 구조를 바꾸기로 결정했을까?

답은 **메모리 효율성**에 있다. 실제 애플리케이션에서 사용되는 문자열의 대부분은 ASCII 범위 내의 문자들로만 구성되어 있다. 영어 텍스트, URL, JSON 키, 변수명, 로그 메시지 등이 모두 그렇다. 기존
`char[]` 구조에서는 이런 단순한 문자들도 각각 2바이트를 차지했는데, 이는 필요한 것의 두 배에 해당하는 낭비였다.

---

## 2. Java 8 이전: char[] 기반 구현

### 2.1 내부 구조

```java
public final class String {
	/** 문자열 데이터를 저장하는 배열 */
	private final char[] value;

	/** 캐싱된 해시코드 */
	private int hash;

	// Java 7 update 6 이전에는 offset과 count 필드도 있었음
	// private final int offset;
	// private final int count;
}
```

Java에서 `char`는 UTF-16 코드 유닛을 표현하며, 항상 2바이트(16비트)를 차지한다. 따라서 문자열 `"Hello"`를 저장하면 다음과 같은 메모리 구조가 된다.

```
"Hello"의 메모리 구조 (Java 8):

value 배열 (char[]):
┌──────┬──────┬──────┬──────┬──────┐
│  H   │  e   │  l   │  l   │  o   │
│0x0048│0x0065│0x006C│0x006C│0x006F│
├──────┼──────┼──────┼──────┼──────┤
│2bytes│2bytes│2bytes│2bytes│2bytes│
└──────┴──────┴──────┴──────┴──────┘

총 10바이트 (문자당 2바이트 × 5문자)
```

### 2.2 문제점: 메모리 낭비

ASCII 문자(영문자, 숫자, 기본 기호)는 0x00~0x7F 범위의 값만 사용한다. 이 값들은 1바이트로 충분히 표현할 수 있지만, `char` 타입은 항상 2바이트를 사용하므로 상위 바이트가 항상 0x00이
된다.

```java
char c = 'A';  // 실제 값: 0x0041
// 메모리에 저장: [0x00][0x41] - 상위 바이트가 항상 0
```

Oracle의 조사에 따르면, 일반적인 Java 애플리케이션에서 Heap 메모리의 약 25%가 String 객체에 사용되며, 그 중 대부분의 문자열이 Latin-1 문자(1바이트로 표현 가능한 문자)로만 구성되어
있었다. 이는 엄청난 메모리 낭비였다.

---

## 3. Java 9 이후: byte[] 기반 구현 (Compact Strings)

### 3.1 새로운 내부 구조

```java
public final class String {
	/**
	 * 문자열 데이터를 저장하는 배열
	 * LATIN1 인코딩이면 1바이트/문자, UTF16이면 2바이트/문자
	 */
	@Stable
	private final byte[] value;

	/**
	 * 인코딩 식별자
	 * LATIN1 = 0, UTF16 = 1
	 */
	private final byte coder;

	/** 캐싱된 해시코드 */
	private int hash;

	/** 해시코드가 0인지 여부 (0도 유효한 해시값이므로) */
	private boolean hashIsZero;

	// 인코딩 상수
	@Native
	static final byte LATIN1 = 0;
	@Native
	static final byte UTF16 = 1;
}
```

### 3.2 동작 원리

String 객체가 생성될 때, JVM은 문자열의 모든 문자를 검사한다. 모든 문자가 Latin-1 범위(0x00~0xFF) 내에 있으면 `coder`를 `LATIN1`(0)으로 설정하고 각 문자를 1바이트로
저장한다. 하나라도 Latin-1 범위를 벗어나는 문자가 있으면 `coder`를 `UTF16`(1)으로 설정하고 기존처럼 각 문자를 2바이트로 저장한다.

```
"Hello"의 메모리 구조 (Java 9+, LATIN1):

coder = 0 (LATIN1)
value 배열 (byte[]):
┌──────┬──────┬──────┬──────┬──────┐
│  H   │  e   │  l   │  l   │  o   │
│ 0x48 │ 0x65 │ 0x6C │ 0x6C │ 0x6F │
├──────┼──────┼──────┼──────┼──────┤
│1byte │1byte │1byte │1byte │1byte │
└──────┴──────┴──────┴──────┴──────┘

총 5바이트 (문자당 1바이트 × 5문자)
→ Java 8 대비 50% 메모리 절약!
```

```
"안녕"의 메모리 구조 (Java 9+, UTF16):

coder = 1 (UTF16)
value 배열 (byte[]):
┌───────────┬───────────┐
│    안     │    녕     │
│0xC5 0x48  │0xB1 0x55  │  (리틀 엔디안)
├───────────┼───────────┤
│  2bytes   │  2bytes   │
└───────────┴───────────┘

총 4바이트 (문자당 2바이트 × 2문자)
→ 기존과 동일
```

### 3.3 인코딩 선택 로직

실제 JDK 소스 코드에서 인코딩을 결정하는 로직을 살펴보자.

```java
// StringUTF16 클래스의 compress 메서드 (단순화된 버전)
public static int compress(char[] src, int srcOff, byte[] dst, int dstOff, int len) {
	for (int i = 0; i < len; i++) {
		char c = src[srcOff + i];
		if (c > 0xFF) {  // Latin-1 범위를 벗어나면
			return 0;    // 압축 실패, UTF-16 사용해야 함
		}
		dst[dstOff + i] = (byte)c;  // 1바이트로 저장
	}
	return len;  // 압축 성공
}
```

문자열 생성 시 이 로직이 실행되어 적절한 인코딩이 선택된다.

```java
// 내부적으로 일어나는 일 (개념적 설명)
String s = new String(charArray);

// 1. 모든 문자가 0x00~0xFF 범위인지 확인
// 2-a. 그렇다면: coder = LATIN1, byte[]에 1바이트씩 저장
// 2-b. 아니라면: coder = UTF16, byte[]에 2바이트씩 저장
```

---

## 4. 문자 인코딩 기초

Compact Strings를 제대로 이해하려면 문자 인코딩에 대한 기본 지식이 필요하다.

### 4.1 ASCII (7비트, 128문자)

가장 기본적인 문자 인코딩으로, 영문 알파벳, 숫자, 기본 기호를 0~127 범위의 값으로 표현한다.

```
'A' = 65 (0x41)
'Z' = 90 (0x5A)
'a' = 97 (0x61)
'0' = 48 (0x30)
' ' = 32 (0x20)
```

### 4.2 Latin-1 / ISO-8859-1 (8비트, 256문자)

ASCII를 확장하여 서유럽 언어의 악센트 문자들을 포함한다. 0~255 범위의 값을 사용하며, 1바이트로 표현 가능하다.

```
ASCII 범위 (0x00~0x7F): 기존 ASCII와 동일
확장 범위 (0x80~0xFF): é, ñ, ü 같은 악센트 문자

'é' = 233 (0xE9)
'ñ' = 241 (0xF1)
```

### 4.3 UTF-16 (16비트 기본, 가변)

유니코드의 대부분의 문자를 2바이트로 표현한다. 이모지나 일부 한자 등 BMP(Basic Multilingual Plane) 외부의 문자는 서로게이트 쌍(surrogate pair)을 사용하여 4바이트로 표현한다.

```
'A' = 0x0041 (2바이트)
'가' = 0xAC00 (2바이트)
'😀' = 0xD83D 0xDE00 (4바이트, 서로게이트 쌍)
```

### 4.4 Java에서의 적용

Java 9+의 Compact Strings는 다음 기준으로 인코딩을 선택한다.

| 문자열 내용             | coder 값    | 저장 방식   | 예시              |
|--------------------|------------|---------|-----------------|
| 모든 문자가 Latin-1 범위  | LATIN1 (0) | 1바이트/문자 | "Hello", "café" |
| Latin-1 범위 외 문자 포함 | UTF16 (1)  | 2바이트/문자 | "안녕", "Hello世界" |

주의할 점은 한 문자라도 Latin-1 범위를 벗어나면 전체 문자열이 UTF-16으로 저장된다는 것이다.

```java
String s1 = "Hello World";        // LATIN1, 11바이트
String s2 = "Hello World!";       // LATIN1, 12바이트  
String s3 = "Hello 世界";          // UTF16, 18바이트 (9문자 × 2)
String s4 = "안녕하세요";           // UTF16, 10바이트 (5문자 × 2)
```

---

## 5. 메서드 구현의 변화

내부 구조가 바뀌면서 String의 메서드들도 이중 구현을 갖게 되었다.

### 5.1 charAt() 메서드

```java
public char charAt(int index) {
	if (isLatin1()) {
		return StringLatin1.charAt(value, index);
	} else {
		return StringUTF16.charAt(value, index);
	}
}

// StringLatin1.charAt
public static char charAt(byte[] value, int index) {
	if (index < 0 || index >= value.length) {
		throw new StringIndexOutOfBoundsException(index);
	}
	return (char)(value[index] & 0xff);  // byte를 char로 변환
}

// StringUTF16.charAt  
public static char charAt(byte[] value, int index) {
	checkIndex(index, value);
	return getChar(value, index);  // 2바이트를 읽어서 char로 조합
}
```

### 5.2 length() 메서드

```java
public int length() {
	return value.length >> coder;
	// LATIN1(coder=0): value.length >> 0 = value.length
	// UTF16(coder=1):  value.length >> 1 = value.length / 2
}
```

이 비트 시프트 연산은 매우 영리한 트릭이다. LATIN1일 때는 배열 길이가 곧 문자 수이고, UTF16일 때는 배열 길이의 절반이 문자 수다.

### 5.3 equals() 메서드

```java
public boolean equals(Object anObject) {
	if (this == anObject) {
		return true;
	}
	if (anObject instanceof String) {
		String aString = (String)anObject;
		if (coder() == aString.coder()) {
			// 같은 인코딩이면 바이트 배열 직접 비교
			return isLatin1()
				? StringLatin1.equals(value, aString.value)
				: StringUTF16.equals(value, aString.value);
		}
	}
	return false;
}
```

흥미로운 점은 `coder`가 다르면 바로 `false`를 반환한다는 것이다. 같은 문자열 내용이라도 하나는 LATIN1, 하나는 UTF16으로 저장되어 있다면 `equals()`가 `false`를 반환할까?

사실 그런 상황은 발생하지 않는다. LATIN1으로 저장 가능한 문자열은 항상 LATIN1으로 저장되기 때문이다. 따라서 같은 내용의 문자열은 항상 같은 `coder` 값을 갖는다.

---

## 6. 성능 영향 분석

### 6.1 메모리 사용량 비교

```java
public class MemoryComparison {
	public static void main(String[] args) {
		// ASCII 문자열 (LATIN1)
		String ascii = "Hello World! This is a test string.";
		// Java 8:  36 × 2 = 72 bytes (char[])
		// Java 9+: 36 × 1 = 36 bytes (byte[], LATIN1)
		// 절약: 50%

		// 한글 문자열 (UTF16)
		String korean = "안녕하세요 반갑습니다";
		// Java 8:  11 × 2 = 22 bytes
		// Java 9+: 11 × 2 = 22 bytes
		// 절약: 0% (동일)

		// 혼합 문자열 (UTF16)
		String mixed = "Hello 안녕";
		// Java 8:  8 × 2 = 16 bytes
		// Java 9+: 8 × 2 = 16 bytes (한글 때문에 전체가 UTF16)
		// 절약: 0%
	}
}
```

### 6.2 연산 속도

Compact Strings는 메모리만 절약하는 것이 아니라 CPU 캐시 효율성도 높인다. 같은 크기의 캐시에 더 많은 문자열 데이터가 들어갈 수 있기 때문이다.

다만, `coder` 값을 확인하는 분기(branch)가 추가되므로 일부 연산에서는 미세한 오버헤드가 발생할 수 있다. 하지만 JIT 컴파일러의 최적화와 분기 예측 덕분에 이 오버헤드는 거의 무시할 수 있는
수준이다.

### 6.3 실제 애플리케이션에서의 효과

Oracle의 벤치마크에 따르면, 일반적인 서버 애플리케이션에서 Compact Strings 도입 후 Heap 메모리 사용량이 10~15% 감소했다고 한다. 문자열이 전체 Heap의 약 25%를 차지하고, 그 중
대부분이 Latin-1 문자열이므로 이 수치는 타당하다.

```
Heap 메모리 구성 (가정):
- String 객체: 25%
- 그 중 Latin-1 문자열: 90%
- Latin-1 문자열의 메모리 절약: 50%

전체 절약 = 25% × 90% × 50% = 약 11.25%
```

---

## 7. 실습: 내부 구조 확인하기

### 실습 7.1: Reflection으로 내부 필드 확인

```java
import java.lang.reflect.Field;

public class StringInternalsTest {
	public static void main(String[] args) throws Exception {
		String ascii = "Hello";
		String korean = "안녕";

		inspectString("ASCII 문자열", ascii);
		inspectString("한글 문자열", korean);
	}

	private static void inspectString(String label, String s) throws Exception {
		System.out.println("=== " + label + ": \"" + s + "\" ===");

		// value 필드 접근
		Field valueField = String.class.getDeclaredField("value");
		valueField.setAccessible(true);
		byte[] value = (byte[])valueField.get(s);

		// coder 필드 접근
		Field coderField = String.class.getDeclaredField("coder");
		coderField.setAccessible(true);
		byte coder = coderField.getByte(s);

		System.out.println("coder: " + coder + " (" + (coder == 0 ? "LATIN1" : "UTF16") + ")");
		System.out.println("value.length: " + value.length + " bytes");
		System.out.println("문자 수: " + s.length());
		System.out.print("value 내용: ");
		for (byte b : value) {
			System.out.printf("0x%02X ", b);
		}
		System.out.println("\n");
	}
}
```

**실행 방법 (Java 9 이상)**

Java 9부터 도입된 모듈 시스템(JPMS)이 `java.lang` 패키지의 내부 필드에 대한 Reflection 접근을 기본적으로 차단한다. 이 코드를 실행하려면 `--add-opens` 옵션으로 모듈을 열어줘야 한다.

```bash
# 방법 1: 컴파일 후 실행
javac StringInternalsTest.java
java --add-opens java.base/java.lang=ALL-UNNAMED StringInternalsTest

# 방법 2: Java 11+ 단일 파일 직접 실행 (컴파일 없이)
java --add-opens java.base/java.lang=ALL-UNNAMED StringInternalsTest.java
```

`--add-opens` 옵션의 의미를 분석하면 다음과 같다.

| 구성 요소 | 설명 |
|-----------|------|
| `java.base` | String 클래스가 속한 모듈 이름 |
| `java.lang` | 열고자 하는 패키지 이름 |
| `ALL-UNNAMED` | 이름 없는 모듈(클래스패스의 일반 클래스)에게 접근 허용 |

IDE에서 실행하는 경우 VM Options에 `--add-opens java.base/java.lang=ALL-UNNAMED`를 추가하면 된다. IntelliJ의 경우 Run/Debug Configurations > Modify options > Add VM options에서 설정할 수 있다.

이 코드를 실행하면 실제로 어떤 인코딩이 사용되었는지, 바이트 배열의 내용이 어떤지 직접 확인할 수 있다.

**예상 출력 결과**

```
=== ASCII 문자열: "Hello" ===
coder: 0 (LATIN1)
value.length: 5 bytes
문자 수: 5
value 내용: 0x48 0x65 0x6C 0x6C 0x6F 

=== 한글 문자열: "안녕" ===
coder: 1 (UTF16)
value.length: 4 bytes
문자 수: 2
value 내용: 0x48 0xC5 0x55 0xB1 
```

ASCII 문자열 "Hello"는 `coder=0`(LATIN1)으로 5바이트에 저장되고, 한글 문자열 "안녕"은 `coder=1`(UTF16)로 4바이트(2문자 × 2바이트)에 저장된 것을 확인할 수 있다.

### 실습 7.2: JOL로 객체 크기 측정

JOL(Java Object Layout) 라이브러리를 사용하면 객체의 실제 메모리 레이아웃을 확인할 수 있다.

```java
// 의존성 추가 필요: org.openjdk.jol:jol-core:0.16

import org.openjdk.jol.info.ClassLayout;

public class StringMemoryLayout {
	public static void main(String[] args) {
		String ascii = "Hello World";
		String korean = "안녕하세요";

		System.out.println("=== ASCII 문자열 ===");
		System.out.println(ClassLayout.parseInstance(ascii).toPrintable());

		System.out.println("=== 한글 문자열 ===");
		System.out.println(ClassLayout.parseInstance(korean).toPrintable());
	}
}
```

### 실습 7.3: 메모리 절약 효과 측정

```java
public class MemorySavingsTest {
	public static void main(String[] args) {
		int count = 1_000_000;

		// ASCII 문자열 100만 개 생성
		String[] asciiStrings = new String[count];
		long beforeAscii = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

		for (int i = 0; i < count; i++) {
			asciiStrings[i] = "user_" + i + "_session_data";
		}

		System.gc();
		long afterAscii = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

		System.out.println("ASCII 문자열 " + count + "개");
		System.out.println("메모리 사용량: " + (afterAscii - beforeAscii) / 1024 / 1024 + " MB");

		// 참고: Java 8에서 같은 테스트를 실행하면 약 2배의 메모리를 사용함
	}
}
```

---

## 8. JVM 옵션

Compact Strings는 기본적으로 활성화되어 있지만, 필요한 경우 비활성화할 수 있다.

```bash
# Compact Strings 비활성화 (Java 8과 동일한 동작)
java -XX:-CompactStrings MyApp

# Compact Strings 활성화 (기본값)
java -XX:+CompactStrings MyApp
```

비활성화가 필요한 경우는 거의 없지만, 레거시 코드와의 호환성 문제가 발생하거나 특정 성능 테스트를 위해 사용할 수 있다.

---

## 9. 핵심 정리

Java 9에서 String의 내부 구현이 `char[]`에서 `byte[]` + `coder`로 변경되었다. 이 변화를 Compact Strings라고 부른다.

모든 문자가 Latin-1 범위(0x00~0xFF) 내에 있으면 각 문자를 1바이트로 저장하여 메모리를 50% 절약한다. Latin-1 범위를 벗어나는 문자가 하나라도 있으면 전체 문자열을 UTF-16으로 저장하여
기존과 동일한 2바이트/문자를 사용한다.

`coder` 필드가 인코딩을 나타내며, LATIN1은 0, UTF16은 1이다. `length()` 메서드는 `value.length >> coder` 연산으로 문자 수를 계산한다.

일반적인 Java 애플리케이션에서 Heap 메모리 사용량이 10~15% 감소하는 효과가 있다. 이 기능은 기본적으로 활성화되어 있으며 `-XX:-CompactStrings` 옵션으로 비활성화할 수 있다.

---

## 10. 다음 단계 예고

3단계에서는 String Pool의 동작 방식과 JVM 메모리 구조에서의 위치를 학습한다. String Pool이 Java 버전에 따라 PermGen에서 Heap으로 이동한 이유, `intern()` 메서드의 내부
동작, 그리고 `-XX:StringTableSize` 옵션의 역할을 살펴본다.

---

# 3단계: 메모리 구조와 String Pool

## 학습 목표

이 문서를 학습하고 나면 String Pool의 동작 원리와 JVM 메모리 구조에서의 위치를 명확히 설명할 수 있어야 한다. Java 버전에 따라 String Pool의 위치가 어떻게 변했는지(PermGen → Heap) 이해하고, 리터럴 문자열과 `new String()`의 메모리 할당 차이를 그림으로 설명할 수 있어야 한다. 또한 `intern()` 메서드의 내부 동작과 `-XX:StringTableSize` 옵션의 역할을 파악해야 한다.

---

## 1. 핵심 질문: String Pool은 어디에 존재하는가?

Java에서 문자열 리터럴은 특별한 대우를 받는다. 동일한 내용의 문자열 리터럴이 여러 번 등장해도 메모리에는 단 하나만 존재하고, 여러 참조가 이를 공유한다. 이것을 가능하게 하는 것이 바로 String Pool(또는 String Constant Pool)이다.

그런데 이 String Pool은 JVM 메모리의 어디에 위치할까? 이 질문에 대한 답은 Java 버전에 따라 다르다. 그리고 그 변화의 이유를 이해하면 JVM 메모리 구조에 대한 깊은 통찰을 얻을 수 있다.

---

## 2. JVM 메모리 구조 개요

String Pool의 위치를 이해하려면 먼저 JVM 메모리 구조를 알아야 한다.

```
┌─────────────────────────────────────────────────────────────┐
│                        JVM Memory                           │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │                      Heap                            │   │
│  │  (객체 인스턴스가 할당되는 영역, GC 대상)              │   │
│  │                                                      │   │
│  │  ┌──────────────┐  ┌──────────────────────────┐     │   │
│  │  │ Young Gen    │  │       Old Gen            │     │   │
│  │  │ (Eden, S0,S1)│  │   (Tenured)              │     │   │
│  │  └──────────────┘  └──────────────────────────┘     │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │              Metaspace (Java 8+)                     │   │
│  │  또는 PermGen (Java 7 이하)                          │   │
│  │  (클래스 메타데이터, 메서드 정보 등)                   │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
│  ┌───────────────┐  ┌───────────────┐  ┌──────────────┐   │
│  │  Stack        │  │  PC Register  │  │ Native Stack │   │
│  │ (스레드별)     │  │  (스레드별)    │  │ (스레드별)    │   │
│  └───────────────┘  └───────────────┘  └──────────────┘   │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

주요 영역의 역할을 간략히 정리하면 다음과 같다.

**Heap**: 객체 인스턴스가 생성되는 공간이다. `new` 키워드로 생성한 모든 객체가 여기에 할당된다. 가비지 컬렉션의 대상이 되는 영역이다.

**PermGen (Java 7 이하)**: Permanent Generation의 약자로, 클래스 메타데이터, 상수 풀, 메서드 정보 등이 저장되던 고정 크기 영역이다. 크기가 고정되어 있어 `OutOfMemoryError: PermGen space` 에러가 자주 발생했다.

**Metaspace (Java 8+)**: PermGen을 대체한 영역으로, Native Memory에 위치한다. 기본적으로 크기 제한이 없어(OS 가용 메모리까지 확장 가능) PermGen 관련 문제가 해결되었다.

---

## 3. String Pool의 위치 변화

### 3.1 Java 6 이하: PermGen 내부

```
┌─────────────────────────────────────────┐
│                  Heap                   │
│  ┌─────────────────────────────────┐   │
│  │          Object Area            │   │
│  │   new String("Hello") → 여기    │   │
│  └─────────────────────────────────┘   │
└─────────────────────────────────────────┘

┌─────────────────────────────────────────┐
│         PermGen (고정 크기)              │
│  ┌─────────────────────────────────┐   │
│  │        String Pool              │   │
│  │   "Hello" 리터럴 → 여기          │   │
│  └─────────────────────────────────┘   │
│  ┌─────────────────────────────────┐   │
│  │     Class Metadata 등           │   │
│  └─────────────────────────────────┘   │
└─────────────────────────────────────────┘
```

Java 6 이하에서 String Pool은 PermGen 영역 안에 위치했다. 이 구조에는 몇 가지 심각한 문제가 있었다.

첫째, PermGen은 크기가 고정되어 있었다(기본값 64MB~82MB). 대량의 문자열을 `intern()`하면 PermGen이 가득 차서 `OutOfMemoryError: PermGen space`가 발생했다.

둘째, PermGen은 Full GC 시에만 정리되었다. Full GC는 Stop-The-World를 유발하므로 애플리케이션 성능에 큰 영향을 미쳤다.

셋째, `intern()`된 문자열이 더 이상 사용되지 않아도 PermGen에서 쉽게 제거되지 않았다.

### 3.2 Java 7: Heap으로 이동

```
┌─────────────────────────────────────────┐
│                  Heap                   │
│  ┌─────────────────────────────────┐   │
│  │        String Pool ← 이동!       │   │
│  │   "Hello" 리터럴 → 여기          │   │
│  └─────────────────────────────────┘   │
│  ┌─────────────────────────────────┐   │
│  │          Object Area            │   │
│  │   new String("Hello") → 여기    │   │
│  └─────────────────────────────────┘   │
└─────────────────────────────────────────┘

┌─────────────────────────────────────────┐
│              PermGen                    │
│  (클래스 메타데이터만 남음)               │
└─────────────────────────────────────────┘
```

Java 7에서 String Pool이 Heap으로 이동했다. 이 변화로 인해 다음과 같은 이점이 생겼다.

첫째, Heap 크기 제한 내에서 String Pool이 동적으로 확장될 수 있게 되었다.

둘째, 일반 객체와 동일하게 가비지 컬렉션의 대상이 되어, 더 이상 참조되지 않는 인터닝된 문자열이 효율적으로 정리된다.

셋째, Young GC에서도 처리될 수 있어 GC 효율성이 향상되었다.

### 3.3 Java 8+: Metaspace 도입과 String Pool

```
┌─────────────────────────────────────────┐
│                  Heap                   │
│  ┌─────────────────────────────────┐   │
│  │          String Pool             │   │
│  │   "Hello" 리터럴 → 여기          │   │
│  └─────────────────────────────────┘   │
│  ┌─────────────────────────────────┐   │
│  │          Object Area            │   │
│  │   new String("Hello") → 여기    │   │
│  └─────────────────────────────────┘   │
└─────────────────────────────────────────┘

┌─────────────────────────────────────────┐
│     Metaspace (Native Memory)           │
│  (클래스 메타데이터, String Pool 아님!)   │
└─────────────────────────────────────────┘
```

Java 8에서 PermGen이 완전히 사라지고 Metaspace로 대체되었다. 하지만 String Pool은 Metaspace로 가지 않고 Heap에 그대로 남았다. 이것은 Java 7의 결정이 올바랐음을 보여준다.

중요한 점은 **String Pool이 Metaspace에 있다고 오해하면 안 된다**는 것이다. String Pool은 Java 7 이후로 계속 Heap에 위치한다.

---

## 4. String Pool의 내부 구조

String Pool은 내부적으로 해시테이블(HashTable) 구조로 구현되어 있다. 이 해시테이블을 **StringTable**이라고 부른다.

### 4.1 StringTable 구조

```
StringTable (해시테이블):
┌────────┬─────────────────────────────────┐
│ Bucket │ Entries                         │
├────────┼─────────────────────────────────┤
│   0    │ null                            │
├────────┼─────────────────────────────────┤
│   1    │ "Hello" → "World" → null        │
├────────┼─────────────────────────────────┤
│   2    │ "Java" → null                   │
├────────┼─────────────────────────────────┤
│   3    │ null                            │
├────────┼─────────────────────────────────┤
│  ...   │ ...                             │
├────────┼─────────────────────────────────┤
│ 60012  │ "test" → "data" → null          │
└────────┴─────────────────────────────────┘
```

각 버킷은 해시 충돌이 발생한 문자열들의 연결 리스트를 가리킨다. 문자열의 해시코드를 버킷 수로 나눈 나머지가 해당 문자열이 저장될 버킷 인덱스가 된다.

### 4.2 StringTable 크기 설정

StringTable의 버킷 수는 JVM 옵션으로 조절할 수 있다.

```bash
# StringTable 버킷 수 설정 (기본값은 Java 버전마다 다름)
java -XX:StringTableSize=120121 MyApp

# 현재 StringTable 통계 출력
java -XX:+PrintStringTableStatistics MyApp
```

버킷 수의 기본값은 Java 버전에 따라 다르다.

| Java 버전 | 기본 버킷 수 |
|-----------|-------------|
| Java 7    | 1009        |
| Java 7u40+| 60013       |
| Java 8    | 60013       |
| Java 11+  | 65536       |

버킷 수가 너무 작으면 해시 충돌이 많이 발생하여 `intern()` 성능이 저하된다. 대량의 문자열을 인터닝하는 애플리케이션에서는 이 값을 늘리는 것이 좋다.

---

## 5. 리터럴 vs new String() 의 메모리 할당

### 5.1 문자열 리터럴

```java
String s1 = "Hello";
String s2 = "Hello";
```

위 코드가 실행되면 다음과 같은 일이 일어난다.

1. 컴파일 시점에 "Hello"가 클래스 파일의 상수 풀(Constant Pool)에 기록된다.
2. 클래스 로딩 시점에 JVM이 상수 풀의 문자열 리터럴을 String Pool에 등록한다.
3. `s1`에 String Pool의 "Hello" 참조가 할당된다.
4. `s2`도 같은 String Pool의 "Hello" 참조가 할당된다.

```
Stack                          Heap
┌─────────┐                   ┌───────────────────────┐
│   s1    │──────────────────▶│                       │
├─────────┤                   │     String Pool       │
│   s2    │──────────────────▶│  ┌─────────────────┐  │
└─────────┘                   │  │    "Hello"      │  │
                              │  │    (0x1000)     │  │
                              │  └─────────────────┘  │
                              └───────────────────────┘

s1 == s2 → true (같은 객체 참조)
```

### 5.2 new String() 생성자

```java
String s3 = new String("Hello");
```

위 코드가 실행되면 다음과 같은 일이 일어난다.

1. "Hello" 리터럴이 String Pool에 등록된다 (이미 있으면 생략).
2. `new String()`이 Heap의 일반 객체 영역에 **새로운** String 객체를 생성한다.
3. 새 객체의 내용은 String Pool의 "Hello"를 복사한 것이다.
4. `s3`에 이 새 객체의 참조가 할당된다.

```
Stack                          Heap
┌─────────┐                   ┌───────────────────────┐
│   s1    │──────────────────▶│                       │
├─────────┤                   │     String Pool       │
│   s2    │──────────────────▶│  ┌─────────────────┐  │
├─────────┤                   │  │    "Hello"      │  │
│   s3    │───────┐           │  │    (0x1000)     │  │
└─────────┘       │           │  └─────────────────┘  │
                  │           │                       │
                  │           │     Object Area       │
                  │           │  ┌─────────────────┐  │
                  └──────────▶│  │    "Hello"      │  │
                              │  │    (0x2000)     │  │
                              │  └─────────────────┘  │
                              └───────────────────────┘

s1 == s3 → false (다른 객체)
s1.equals(s3) → true (내용은 동일)
```

### 5.3 실제로 몇 개의 객체가 생성될까?

면접에서 자주 나오는 질문이다.

```java
String s = new String("Hello");
```

이 한 줄로 **최대 2개**의 객체가 생성된다.

1. "Hello" 리터럴 → String Pool에 생성 (Pool에 없는 경우)
2. new String() → Heap의 일반 영역에 생성

만약 String Pool에 이미 "Hello"가 있다면 1개만 생성된다.

```java
String s1 = "Hello";  // Pool에 "Hello" 생성 (1개)
String s2 = new String("Hello");  // Heap에 새 객체 생성 (1개, Pool 것 재사용)
// 총 2개
```

```java
String s1 = new String("Hello");  // Pool에 "Hello" + Heap에 새 객체 (2개)
String s2 = new String("Hello");  // Heap에 새 객체만 (1개, Pool 것 재사용)
// 총 3개
```

---

## 6. intern() 메서드 심화

### 6.1 동작 원리

`intern()` 메서드는 String Pool을 명시적으로 활용할 수 있게 해준다.

```java
public native String intern();
```

`intern()`은 네이티브 메서드로 구현되어 있다. 호출하면 다음과 같은 로직이 실행된다.

1. StringTable에서 현재 문자열과 동일한 내용의 문자열을 검색한다.
2. 찾으면: 해당 참조를 반환한다.
3. 못 찾으면: 현재 문자열을 StringTable에 등록하고 그 참조를 반환한다.

```java
String s1 = new String("Hello");  // Heap에 새 객체
String s2 = s1.intern();          // Pool의 "Hello" 반환
String s3 = "Hello";              // Pool의 "Hello"

System.out.println(s1 == s2);  // false (s1은 Heap, s2는 Pool)
System.out.println(s2 == s3);  // true  (둘 다 Pool)
```

### 6.2 Java 7 이후의 intern() 동작 변화

Java 7 이후로 `intern()`의 동작이 미묘하게 변경되었다. 이전에는 항상 Pool 영역에 문자열 복사본을 만들었지만, 이제는 Heap 객체의 참조를 그대로 Pool에 등록할 수 있다.

```java
// Java 7+에서의 동작
String s1 = new String("a") + new String("b");  // "ab"가 Heap에 생성
// 이 시점에서 "ab"는 String Pool에 없음

String s2 = s1.intern();  
// Java 6: Pool에 "ab" 복사본 생성, 복사본 참조 반환
// Java 7+: s1 자체를 Pool에 등록, s1 참조 반환

System.out.println(s1 == s2);  
// Java 6: false
// Java 7+: true
```

이 변화는 메모리 효율성을 높이기 위한 것이다.

### 6.3 intern() 사용 시 주의사항

`intern()`은 양날의 검이다. 잘 사용하면 메모리를 절약하지만, 잘못 사용하면 성능 문제를 일으킨다.

**장점**:
- 동일한 문자열의 중복 저장을 방지하여 메모리 절약
- `==` 연산자로 빠른 비교 가능 (O(1))

**단점**:
- `intern()` 호출 자체에 비용 발생 (해시 계산, Pool 검색)
- 무분별한 사용 시 StringTable 크기 증가로 성능 저하
- StringTable은 고정 크기 해시테이블이므로 충돌 증가 시 검색 성능 저하

**권장 사용 시나리오**:
- 제한된 종류의 문자열이 대량으로 반복되는 경우 (예: 국가 코드, 상태값)
- 문자열 비교가 매우 빈번하여 `==` 사용이 유의미한 성능 향상을 주는 경우

**피해야 할 시나리오**:
- 거의 모든 문자열이 고유한 경우 (예: UUID, 사용자 입력)
- 문자열이 일시적으로만 사용되고 바로 버려지는 경우

---

## 7. == vs equals() 완벽 정리

### 7.1 동작 원리

**== 연산자**: 두 참조가 메모리의 **같은 객체**를 가리키는지 비교한다. 주소 비교이므로 O(1) 시간 복잡도를 갖는다.

**equals() 메서드**: 두 객체의 **내용**이 같은지 비교한다. String의 경우 문자를 하나씩 비교하므로 O(n) 시간 복잡도를 갖는다.

```java
public boolean equals(Object anObject) {
    if (this == anObject) {  // 먼저 참조 비교 (빠른 경로)
        return true;
    }
    if (anObject instanceof String) {
        String aString = (String) anObject;
        if (coder() == aString.coder()) {
            return isLatin1() 
                ? StringLatin1.equals(value, aString.value)
                : StringUTF16.equals(value, aString.value);
        }
    }
    return false;
}
```

`equals()` 메서드 내부에서도 먼저 `==`로 참조 비교를 한다. 같은 객체면 바로 `true`를 반환하여 불필요한 문자 비교를 피한다.

### 7.2 다양한 케이스 분석

```java
public class StringComparisonTest {
    public static void main(String[] args) {
        // Case 1: 리터럴끼리
        String a = "Hello";
        String b = "Hello";
        System.out.println("a == b: " + (a == b));           // true
        System.out.println("a.equals(b): " + a.equals(b));   // true
        
        // Case 2: 리터럴 vs new
        String c = new String("Hello");
        System.out.println("a == c: " + (a == c));           // false
        System.out.println("a.equals(c): " + a.equals(c));   // true
        
        // Case 3: new끼리
        String d = new String("Hello");
        System.out.println("c == d: " + (c == d));           // false
        System.out.println("c.equals(d): " + c.equals(d));   // true
        
        // Case 4: 컴파일 타임 상수 결합
        String e = "Hel" + "lo";  // 컴파일러가 "Hello"로 최적화
        System.out.println("a == e: " + (a == e));           // true
        
        // Case 5: 런타임 결합
        String f = "Hel";
        String g = f + "lo";  // 런타임에 결합, 새 객체 생성
        System.out.println("a == g: " + (a == g));           // false
        System.out.println("a.equals(g): " + a.equals(g));   // true
        
        // Case 6: intern() 사용
        String h = g.intern();
        System.out.println("a == h: " + (a == h));           // true
        
        // Case 7: final 변수 결합
        final String i = "Hel";
        String j = i + "lo";  // i가 final이므로 컴파일 타임 상수
        System.out.println("a == j: " + (a == j));           // true
    }
}
```

### 7.3 실무 권장사항

문자열 비교는 항상 `equals()`를 사용하는 것이 안전하다. `==`는 특수한 상황(String Pool 최적화를 의도적으로 활용하는 경우)에만 사용해야 한다.

```java
// 권장: equals() 사용
if (userInput.equals("admin")) {
    // ...
}

// 더 안전한 방식: 리터럴을 앞에 (NPE 방지)
if ("admin".equals(userInput)) {
    // userInput이 null이어도 NPE 발생 안 함
}

// Java 7+: Objects.equals() 활용
if (Objects.equals(userInput, "admin")) {
    // 양쪽 모두 null-safe
}
```

---

## 8. 실습: String Pool 동작 확인

### 실습 8.1: 기본 동작 확인

```java
public class StringPoolBasicTest {
    public static void main(String[] args) {
        String literal1 = "Hello";
        String literal2 = "Hello";
        String newStr1 = new String("Hello");
        String newStr2 = new String("Hello");
        String interned = newStr1.intern();
        
        System.out.println("=== 참조 비교 (==) ===");
        System.out.println("literal1 == literal2: " + (literal1 == literal2));
        System.out.println("literal1 == newStr1: " + (literal1 == newStr1));
        System.out.println("newStr1 == newStr2: " + (newStr1 == newStr2));
        System.out.println("literal1 == interned: " + (literal1 == interned));
        
        System.out.println("\n=== 내용 비교 (equals) ===");
        System.out.println("literal1.equals(newStr1): " + literal1.equals(newStr1));
        
        System.out.println("\n=== Identity Hash Code (메모리 주소 기반) ===");
        System.out.println("literal1: " + System.identityHashCode(literal1));
        System.out.println("literal2: " + System.identityHashCode(literal2));
        System.out.println("newStr1: " + System.identityHashCode(newStr1));
        System.out.println("newStr2: " + System.identityHashCode(newStr2));
        System.out.println("interned: " + System.identityHashCode(interned));
    }
}
```

**예상 출력**:
```
=== 참조 비교 (==) ===
literal1 == literal2: true
literal1 == newStr1: false
newStr1 == newStr2: false
literal1 == interned: true

=== 내용 비교 (equals) ===
literal1.equals(newStr1): true

=== Identity Hash Code (메모리 주소 기반) ===
literal1: 123456789
literal2: 123456789      (literal1과 동일)
newStr1: 987654321
newStr2: 192837465
interned: 123456789      (literal1과 동일)
```

### 실습 8.2: 컴파일 타임 vs 런타임 결합

```java
public class StringConcatTest {
    public static void main(String[] args) {
        String base = "Hello";
        
        // 컴파일 타임 상수 결합
        String compile1 = "Hel" + "lo";
        String compile2 = "He" + "l" + "lo";
        
        // final 변수 결합 (컴파일 타임 상수)
        final String part1 = "Hel";
        final String part2 = "lo";
        String compile3 = part1 + part2;
        
        // 런타임 결합
        String nonFinal = "Hel";
        String runtime1 = nonFinal + "lo";
        
        String runtime2 = "Hello".substring(0, 3) + "lo";
        
        System.out.println("=== 컴파일 타임 상수 ===");
        System.out.println("base == compile1: " + (base == compile1));
        System.out.println("base == compile2: " + (base == compile2));
        System.out.println("base == compile3: " + (base == compile3));
        
        System.out.println("\n=== 런타임 결합 ===");
        System.out.println("base == runtime1: " + (base == runtime1));
        System.out.println("base == runtime2: " + (base == runtime2));
        
        System.out.println("\n=== intern() 적용 후 ===");
        System.out.println("base == runtime1.intern(): " + (base == runtime1.intern()));
    }
}
```

### 실습 8.3: StringTable 통계 확인

```bash
# StringTable 통계를 출력하며 실행
java -XX:+PrintStringTableStatistics StringPoolBasicTest
```

출력 예시:
```
StringTable statistics:
Number of buckets       :     65536 =    524288 bytes, each 8
Number of entries       :     12345 =    197520 bytes, each 16
Number of literals      :     12345 =    567890 bytes, avg  46.000
Total footprint         :           =   1289698 bytes
Average bucket size     :     0.188
Variance of bucket size :     0.189
Std. dev. of bucket size:     0.435
Maximum bucket size     :         3
```

---

## 9. 핵심 정리

String Pool은 Java 7부터 Heap 영역에 위치한다. Java 6 이하에서는 PermGen에 있었으나 메모리 관리 문제로 이동했다. Metaspace에 있다는 것은 오해다.

String Pool의 내부 구조는 StringTable이라는 고정 크기 해시테이블이다. 버킷 수는 `-XX:StringTableSize` 옵션으로 조절할 수 있으며, 기본값은 Java 11 기준 65536이다.

문자열 리터럴은 자동으로 String Pool에 등록되지만, `new String()`으로 생성한 객체는 Heap의 일반 영역에 생성된다. `intern()` 메서드로 명시적으로 Pool에 등록할 수 있다.

`==`는 참조(주소) 비교이고, `equals()`는 내용 비교다. 실무에서는 항상 `equals()`를 사용하는 것이 안전하며, NPE 방지를 위해 리터럴을 앞에 두거나 `Objects.equals()`를 사용하는 것이 좋다.

---

## 10. 다음 단계 예고

4단계에서는 String의 주요 메서드들과 시간/공간 복잡도를 학습한다. `substring()`의 역사적 변화, `indexOf()`의 검색 알고리즘, `split()`의 정규식 오버헤드 등을 분석하고, 각 메서드를 언제 어떻게 사용해야 효율적인지 알아본다.