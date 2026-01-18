package datastructure.phase03;

import java.util.HashMap;
import java.util.Map;

/**
 * HashMap의 기본 연산과 유용한 메서드들을 실습하는 클래스입니다.
 * <p>
 * 학습 목표:
 * 1. 기본 CRUD 연산 (put, get, remove, containsKey)
 * 2. 순회 방법 (keySet, values, entrySet)
 * 3. Java 8+ 유용한 메서드 (getOrDefault, putIfAbsent, compute, merge)
 */
public class HashMapDemo {

	public static void main(String[] args) {

		// ========================================
		// 1. 생성
		// ========================================

		// 기본 생성 - 초기 용량 16, load factor 0.75
		Map<String, Integer> scores = new HashMap<>();

		// 초기 용량 지정 - 대량 데이터를 넣을 예정이라면
		Map<String, Integer> largeMap = new HashMap<>(1000);

		// 초기 용량과 load factor 모두 지정
		Map<String, Integer> customMap = new HashMap<>(16, 0.75f);

		// ========================================
		// 2. 추가 (Create)
		// ========================================

		scores.put("홍길동", 85);
		scores.put("이순신", 92);
		scores.put("강감찬", 78);

		// 같은 키로 다시 put하면 값이 덮어써진다.
		// put()은 이전 값을 반환(없었으면 null);
		Integer oldValue = scores.put("홍길동", 90);
		System.out.println("홍길동의 이전 점수: " + oldValue); // 85
		System.out.println("홍길동의 현재 점수: " + scores.get("홍길동")); // 90

		// putIfAbsent() - 키가 없을 때만 추가
		// 이미 존재하면 기존 값을 유지하고, 없으면 새 값 추가
		scores.putIfAbsent("홍길동", 100); // 이미 있으므로 무시
		scores.putIfAbsent("유관순", 88); // 없으므로 추가됨
		System.out.println("홍길동: " + scores.get("홍길동"));
		System.out.println("유관수: " + scores.get("유관순"));

		// ========================================
		// 3. 조회 (Read)
		// ========================================

		// get(key) - 값 조회, 없으면 null 반환
		Integer score = scores.get("이순신");
		System.out.println("이순신 점수: " + score);

		Integer notFound = scores.get("없는 사람");
		System.out.println("없는 키 조회: " + notFound);

		// getOrDefault() - 없으면 기본값 반환 NPE 방지
		Integer scoreOrDefault = scores.getOrDefault("없는사람", 0);
		System.out.println("기본값 적용: " + scoreOrDefault);

		// containsKey - 키 존재 여부 확인
		boolean hasKey = scores.containsKey("홍길동");
		System.out.println("홍길동 존재? " + hasKey);

		// containsValue() - > 값 존재 여부 확인 O(n)
		boolean hasValue = scores.containsValue("92");
		System.out.println("92점인 사람 존재? " + hasValue); // true

		// size - 저장된 항목 수
		System.out.println("총 인원: " + scores.size());

		// isEmpty() - 비어있는지 확인
		System.out.println("비어있나? " + scores.isEmpty());

		// ========================================
		// 4. 수정 (Update)
		// ========================================

		// put()으로 덮어쓰기
		scores.put("강감찬", 80);

		// replace() - 키가 존재할 때만 값 변경
		// 존재하면 이전 값 반환, 없으면 null
		Integer replaced = scores.replace("강감찬", 82);
		System.out.println("변경된 강감찬: " + scores.get("강감찬")); // 82

		Integer notReplaced = scores.replace("없는사람", 100);
		System.out.println("없는 키 replace 결과: " + notReplaced);  // null

		// replace(key, oldValue, newValue) - 값도 일치할 때만 변경
		boolean success = scores.replace("강감찬", 82, 85);
		System.out.println("조건부 변경 성공? " + success);  // true

		// ========================================
		// 5. 삭제 (Delete)
		// ========================================

		// remove(key) - 키로 삭제, 삭제된 값 반환
		Integer removed = scores.remove("유관순");
		System.out.println("삭제된 값: " + removed);

		// remove(key, value) - 키와 값이 모두 일치할 때만 삭제
		boolean removeSuccess = scores.remove("홍길동", 90);
		System.out.println("조건부 삭제 성공? " + removeSuccess);

		// clear() - 전체 삭제
		// scores.clear();

		// ========================================
		// 6. 순회 (Iteration)
		// ========================================

		scores.put("홍길동", 90);
		scores.put("유관순", 88);

		System.out.println("\n=== 순회 방법들 ===");

		// 방법 1: keySet() - 키만 필요할 때
		System.out.println("키 순회: ");
		for (String name : scores.keySet()) {
			System.out.println("  " + name);
		}

		// 방법 2: values() - 값만 필요할 때
		System.out.println("값 순회:");
		for (Integer s : scores.values()) {
			System.out.println("  " + s);
		}

		// 방법 3: entrySet() - 키와 값 모두 필요할 때 (가장 효율적)
		System.out.println("키-값 순회:");
		for (Map.Entry<String, Integer> entry : scores.entrySet()) {
			System.out.println("  " + entry.getKey() + " = " + entry.getValue());
		}

		// 방법 4: forEach() - Java 8+ 람다 활용
		System.out.println("forEach 람다:");
		scores.forEach((name, s) -> {
			System.out.println("  " + name + " : " + s);
		});

		// ========================================
		// 7. Java 8+ 고급 메서드
		// ========================================

		System.out.println("\n=== Java 8+ 고급 메서드 ===");

		// compute() - 키의 값을 계산해서 업데이트
		// 키가 있으면 기존 값을 사용, 없으면 null이 전달됨
		scores.compute("홍길동", (key, val) -> val + 10);
		System.out.println("홍길동 +10점: " + scores.get("홍길동"));  // 100

		// computeIfAbsent() - 키가 없을 때만 계산해서 추가
		// 비싼 연산을 필요할 때만 실행하고 싶을 때 유용
		scores.computeIfAbsent("신사임당", key -> {
			System.out.println("  새로운 키 발견, 계산 실행!");
			return 95;
		});
		System.out.println("신사임당: " + scores.get("신사임당"));  // 95

		// computeIfPresent() - 키가 있을 때만 계산해서 업데이트
		scores.computeIfPresent("이순신", (key, val) -> val + 5);
		System.out.println("이순신 +5점: " + scores.get("이순신"));  // 97

		// merge() - 값 병합 (카운팅, 누적에 최적)
		Map<String, Integer> wordCount = new HashMap<>();
		String[] words = {"apple", "banana", "apple", "cherry", "apple", "banana"};

		for (String word : words) {
			// 기존 값이 있으면 더하고, 없으면 1로 시작
			wordCount.merge(word, 1, Integer::sum);
		}
		System.out.println("단어 카운팅: " + wordCount);
		// {banana=2, apple=3, cherry=1}


		// ========================================
		// 8. 실무 패턴: 그룹핑
		// ========================================

		System.out.println("\n=== 실무 패턴: 그룹핑 ===");

		// 점수대별로 학생 그룹핑
		Map<String, java.util.List<String>> gradeGroups = new HashMap<>();

		// computeIfAbsent로 리스트 초기화를 깔끔하게
		gradeGroups.computeIfAbsent("A등급", k -> new java.util.ArrayList<>()).add("홍길동");
		gradeGroups.computeIfAbsent("A등급", k -> new java.util.ArrayList<>()).add("이순신");
		gradeGroups.computeIfAbsent("B등급", k -> new java.util.ArrayList<>()).add("강감찬");

		System.out.println("등급별 그룹: " + gradeGroups);
		// {A등급=[홍길동, 이순신], B등급=[강감찬]}
	}
}
