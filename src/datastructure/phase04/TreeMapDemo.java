package datastructure.phase04;

import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

/**
 * TreeMap의 기본 연산과 NavigableMap 메서드를 실습하는 클래스입니다.
 *
 * 학습 목표:
 * 1. TreeMap의 정렬 특성 이해
 * 2. NavigableMap의 탐색 메서드 활용
 * 3. 범위 검색 (subMap, headMap, tailMap)
 */
public class TreeMapDemo {
	public static void main(String[] args) {
		// ========================================
		// 1. 생성과 기본 연산
		// ========================================

		// 기본 생성 - 키의 자연 순서(Comparable)로 정렬
		TreeMap<Integer, String> scores = new TreeMap<>();

		// 순서 상관없이 삽입
		scores.put(85, "홍길동");
		scores.put(92, "이순신");
		scores.put(78, "강감찬");
		scores.put(95, "유관순");
		scores.put(88, "신사임당");

		// 순회하면 키 기준 오름차순으로 출력
		System.out.println("=== 기본 순회 (키 오름차순) ===");
		for (Map.Entry<Integer, String> entry : scores.entrySet()) {
			System.out.println(entry.getKey() + "점: " + entry.getValue());
		}
		// 78점: 강감찬
		// 85점: 홍길동
		// 88점: 신사임당
		// 92점: 이순신
		// 95점: 유관순

		// ========================================
		// 2. 최솟값/최댓값 관련 메서드
		// ========================================
		System.out.println("\n=== 최솟값/최댓값 ===");

		// 가장 작은 키와 가장 큰 키
		System.out.println("최저점: " + scores.firstKey());
		System.out.println("최고점: " + scores.lastKey());

		// 키-값 쌍으로 가져오기
		System.out.println("최저점 엔트리: " + scores.firstEntry());  // 78=강감찬
		System.out.println("최고점 엔트리: " + scores.lastEntry());   // 95=유관순

		// 꺼내면서 제거 (Queue처럼 사용 가능)
		// Map.Entry<Integer, String> lowest = scores.pollFirstEntry();
		// Map.Entry<Integer, String> highest = scores.pollLastEntry();

		// ========================================
		// 3. 근접 값 검색 (floor, ceiling, lower, higher)
		// ========================================

		System.out.println("\n=== 근접 값 검색 ===");

		// 현재 키들: 78, 85, 88, 92, 95

		// floorKey: 주어진 값 이하 중 가장 큰 키
		System.out.println("90 이하 중 최대: " + scores.floorKey(90));    // 88
		System.out.println("88 이하 중 최대: " + scores.floorKey(88));    // 88 (같으면 포함)

		// ceilingKey: 주어진 값 이상 중 가장 작은 키
		System.out.println("90 이상 중 최소: " + scores.ceilingKey(90));  // 92
		System.out.println("88 이상 중 최소: " + scores.ceilingKey(88));  // 88 (같으면 포함)

		// lowerKey: 주어진 값 미만 중 가장 큰 키 (같은 값 제외)
		System.out.println("88 미만 중 최대: " + scores.lowerKey(88));    // 85

		// higherKey: 주어진 값 초과 중 가장 작은 키 (같은 값 제외)
		System.out.println("88 초과 중 최소: " + scores.higherKey(88));   // 92


		// ========================================
		// 4. 범위 검색 (subMap, headMap, tailMap)
		// ========================================

		System.out.println("\n=== 범위 검색 ===");

		// subMap: from 이상, to 미만 (기본)
		NavigableMap<Integer, String> middle = scores.subMap(80, true, 93, false);
		System.out.println("80~93 범위: " + middle);  // {85=홍길동, 88=신사임당, 92=이순신}

		// headMap: 특정 키 미만의 모든 엔트리
		NavigableMap<Integer, String> low = scores.headMap(88, false);
		System.out.println("88 미만: " + low);  // {78=강감찬, 85=홍길동}

		// tailMap: 특정 키 이상의 모든 엔트리
		NavigableMap<Integer, String> high = scores.tailMap(88, true);
		System.out.println("88 이상: " + high);  // {88=신사임당, 92=이순신, 95=유관순}

		// ========================================
		// 5. 역순 순회
		// ========================================

		System.out.println("\n=== 역순 순회 ===");

		// descendingMap: 역순 뷰 반환
		NavigableMap<Integer, String> descending = scores.descendingMap();
		System.out.println("내림차순: " + descending);
		// {95=유관순, 92=이순신, 88=신사임당, 85=홍길동, 78=강감찬}

		// descendingKeySet으로 키만 역순 순회
		for (Integer key : scores.descendingKeySet()) {
			System.out.println(key);
		}


		// ========================================
		// 6. 커스텀 정렬 (Comparator)
		// ========================================

		System.out.println("\n=== 커스텀 정렬 ===");

		// 역순 정렬 TreeMap
		TreeMap<Integer, String> reverseScores = new TreeMap<>((a, b) -> b - a);
		reverseScores.put(85, "홍길동");
		reverseScores.put(92, "이순신");
		reverseScores.put(78, "강감찬");

		System.out.println("점수 내림차순: " + reverseScores);
		// {92=이순신, 85=홍길동, 78=강감찬}


		// ========================================
		// 7. 문자열 키 예시
		// ========================================

		System.out.println("\n=== 문자열 키 (사전순 정렬) ===");

		TreeMap<String, Integer> nameScores = new TreeMap<>();
		nameScores.put("홍길동", 85);
		nameScores.put("이순신", 92);
		nameScores.put("강감찬", 78);

		// 문자열은 사전순(유니코드순)으로 정렬
		System.out.println(nameScores);
		// {강감찬=78, 이순신=92, 홍길동=85}
	}
}
