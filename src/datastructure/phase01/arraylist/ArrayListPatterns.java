package datastructure.phase01.arraylist;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;

public class ArrayListPatterns {
	public static void main(String[] args) {
		// ============================================
		// 1. 스택처럼 사용하기
		// ============================================
		System.out.println("=== 1. 스택 패턴 ===");

		List<Integer> stack = new ArrayList<>();

		// push
		stack.add(1);
		stack.add(2);
		stack.add(3);
		System.out.println("push 후: " + stack);

		// peek
		int top = stack.get(stack.size() - 1);
		System.out.println("peek: " + top);

		// pop
		int popped = stack.remove(stack.size() - 1);
		System.out.println("pop: " + popped + ", 남은 스택: " + stack);

		// ============================================
		// 2. 그래프 인접 리스트
		// ============================================
		System.out.println("\n=== 2. 그래프 인접 리스트 ===");

		int n = 5;
		List<List<Integer>> graph = new ArrayList<>();

		// 초기화
		for (int i = 0; i < n; i++) {
			graph.add(new ArrayList<>());
		}

		// 간선 추가 (무방향)
		addEdge(graph, 0, 1);
		addEdge(graph, 0, 2);
		addEdge(graph, 1, 3);
		addEdge(graph, 2, 4);

		// 출력
		for (int i = 0; i < n; i++) {
			System.out.println("노드 " + i + "의 인접 노드: " + graph.get(i));
		}

		// ============================================
		// 3. 가중치 그래프
		// ============================================
		System.out.println("\n=== 3. 가중치 그래프 ===");

		List<List<int[]>> weightedGraph = new ArrayList<>();
		for (int i = 0; i < n; i++) {
			weightedGraph.add(new ArrayList<>());
		}

		// {인접노드, 가중치}
		weightedGraph.get(0).add(new int[] {1, 5});
		weightedGraph.get(0).add(new int[] {2, 3});
		weightedGraph.get(1).add(new int[] {3, 2});

		System.out.println("노드 0의 간선:");
		for (int[] edge : weightedGraph.get(0)) {
			System.out.println("  -> 노드 " + edge[0] + " (가중치: " + edge[1] + ")");
		}

		// ============================================
		// 4. 2D 동적 배열 (Jagged Array)
		// ============================================
		System.out.println("\n=== 4. 2D 동적 배열 ===");

		List<List<Integer>> matrix = new ArrayList<>();

		// 행마다 다른 크기 가능
		matrix.add(new ArrayList<>(Arrays.asList(1)));
		matrix.add(new ArrayList<>(Arrays.asList(1, 2)));
		matrix.add(new ArrayList<>(Arrays.asList(1, 2, 3)));
		matrix.add(new ArrayList<>(Arrays.asList(1, 2, 3, 4)));

		System.out.println("파스칼 삼각형 형태:");
		for (List<Integer> row : matrix) {
			System.out.println(row);
		}

		// ============================================
		// 5. 그룹핑 (Map + List)
		// ============================================
		System.out.println("\n=== 5. 그룹핑 ===");

		String[] words = {"eat", "tea", "tan", "ate", "nat", "bat"};

		// 애너그램 그룹핑
		Map<String, List<String>> anagramGroups = new HashMap<>();
		for (String word : words) {
			char[] chars = word.toCharArray();
			Arrays.sort(chars);
			String key = new String(chars);

			anagramGroups.computeIfAbsent(key, k -> new ArrayList<>()).add(word);
		}
		System.out.println("애너그램 그룹:");
		anagramGroups.values().forEach(System.out::println);

		// ============================================
		// 6. 조건부 필터링과 변환
		// ============================================
		System.out.println("\n=== 6. Stream 활용 ===");

		List<Integer> numbers = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));

		// 필터 + 변환
		List<Integer> evenSquares = numbers.stream()
			.filter(x -> x % 2 == 0)
			.map(x -> x * x)
			.toList();
		System.out.println("짝수의 제곱: " + evenSquares);

		// 조건 검사
		boolean anyOver5 = numbers.stream().anyMatch(x -> x > 5);
		boolean allPositive = numbers.stream().allMatch(x -> x > 0);
		System.out.println("5 초과 존재: " + anyOver5);
		System.out.println("모두 양수: " + allPositive);

		// 집계
		int sum = numbers.stream().mapToInt(Integer::intValue).sum();
		OptionalInt max = numbers.stream().mapToInt(Integer::intValue).max();
		System.out.println("합계: " + sum + ", 최대: " + max.orElse(0));

		// ============================================
		// 7. 슬라이딩 윈도우 with ArrayList
		// ============================================
		System.out.println("\n=== 7. 슬라이딩 윈도우 ===");

		List<Integer> data = Arrays.asList(1, 3, -1, -3, 5, 3, 6, 7);
		int k = 3;
		List<Integer> maxInWindow = new ArrayList<>();

		Deque<Integer> deque = new ArrayDeque<>(); // 인덱스 저장

		for (int i = 0; i < data.size(); i++) {
			// 윈도우 범위 벗어난 요소 제거
			while (!deque.isEmpty() && deque.peekFirst() < i - k + 1) {
				deque.pollFirst();
			}
			// 현재 값보다 작은 요소 제거
			while (!deque.isEmpty() && data.get(deque.peekLast()) < data.get(i)) {
				deque.pollLast();
			}
			deque.offerLast(i);

			if (i >= k - 1) {
				maxInWindow.add(data.get(deque.peekFirst()));
			}
		}
		System.out.println("윈도우 크기 " + k + " 최대값들: " + maxInWindow);

		// ============================================
		// 8. 실전 문제: 구간 병합
		// ============================================
		System.out.println("\n=== 8. 구간 병합 ===");

		int[][] intervals = {{1,3}, {2,6}, {8,10}, {15,18}};
		List<int[]> merged = mergeIntervals(intervals);

		System.out.print("병합 결과: ");
		merged.forEach(i -> System.out.print(Arrays.toString(i) + " "));
		System.out.println();

		// ============================================
		// 9. 복사 주의사항
		// ============================================
		System.out.println("\n=== 9. 복사 주의사항 ===");

		List<Integer> original = new ArrayList<>(Arrays.asList(1, 2, 3));

		// 얕은 복사 (같은 객체 참조)
		List<Integer> shallowCopy = original;
		shallowCopy.add(4);
		System.out.println("얕은 복사 - original: " + original); // [1, 2, 3, 4]

		// 깊은 복사 방법들
		List<Integer> deepCopy1 = new ArrayList<>(original);
		List<Integer> deepCopy2 = new ArrayList<>();
		deepCopy2.addAll(original);
		List<Integer> deepCopy3 = original.stream().collect(java.util.stream.Collectors.toList());

		deepCopy1.add(5);
		System.out.println("깊은 복사 - original: " + original); // [1, 2, 3, 4]
		System.out.println("깊은 복사 - deepCopy1: " + deepCopy1); // [1, 2, 3, 4, 5]
	}

	private static void addEdge(List<List<Integer>> graph, int u, int v) {
		graph.get(u).add(v);
		graph.get(v).add(u);
	}

	private static List<int[]> mergeIntervals(int[][] intervals) {
		if (intervals.length == 0) return new ArrayList<>();

		// 시작점 기준 정렬
		Arrays.sort(intervals, (a, b) -> a[0] - b[0]);

		List<int[]> result = new ArrayList<>();
		result.add(intervals[0]);

		for (int i = 1; i < intervals.length; i++) {
			int[] last = result.get(result.size() - 1);
			int[] current = intervals[i];

			if (current[0] <= last[1]) {
				// 겹침 -> 병합
				last[1] = Math.max(last[1], current[1]);
			} else {
				// 안 겹침 -> 추가
				result.add(current);
			}
		}
		return result;
	}
}
