package datastructure.phase01.array;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class ArraysLibraryExamples {

	public static void main(String[] args) {
		// ============================================
		// 1. 정렬 - Arrays.sort()
		// ============================================
		System.out.println("=== 1. 정렬 ===");

		// 기본 오름차순 정렬 - O(n log n) Dual-Pivot Quicksort
		int[] nums = {64, 34, 25, 12, 22, 11, 90};
		Arrays.sort(nums);
		System.out.println("오름차순: " + Arrays.toString(nums));

		// 부분 정렬 - [fromIndex, toIndex)
		int[] partial = {5, 2, 8, 1, 9, 3, 7};
		Arrays.sort(partial, 1, 5); // 인덱스 1~4까지만 정렬
		System.out.println("부분 정렬 [1, 5): " + Arrays.toString(partial)); // [5, 1, 2, 8, 9, 3, 7]

		// 내림차순 정렬 - Integer[] 사용 필요
		Integer[] descNums = {64, 34, 25, 12, 22, 11, 90};
		Arrays.sort(descNums, Comparator.reverseOrder());
		System.out.println("내림차순: " + Arrays.toString(descNums));

		// 2D 배열 정렬 - 특정 기준으로
		int[][] intervals = {{3, 5}, {1, 4}, {2, 6}, {1, 2}};
		Arrays.sort(intervals, Comparator.comparingInt(a -> a[0]));
		System.out.println("2D 정렬 (첫 요소 기준): " + Arrays.deepToString(intervals));

		// 두 번째 요소 기준 정렬
		Arrays.sort(intervals, Comparator.comparingInt(a -> a[1]));
		System.out.println("2D 정렬 (두번째 요소 기준): " + Arrays.deepToString(intervals));

		// 복합 정렬: 첫 번째 오름차순, 같으면 두 번째 내림차순
		Arrays.sort(intervals, (a, b) -> {
			if (a[0] != b[0])
				return a[0] - b[0];
			return b[1] - a[1];
		});
		System.out.println("복합 정렬: " + Arrays.deepToString(intervals));

		// ============================================
		// 2. 이진 탐색 - Arrays.binarySearch()
		// ============================================
		System.out.println("\n=== 2. 이진 탐색 ===");

		int[] sorted = {11, 12, 22, 25, 34, 64, 90};

		// 값이 존재한다면, 인덱스를 반환한다.
		int idx = Arrays.binarySearch(sorted, 25);
		System.out.println("25의 인덱스: " + idx); // 3

		// 값이 없으면 -(삽입위치 + 1) 반환
		int notFound = Arrays.binarySearch(sorted, 30);
		System.out.println("30 탐색 결과: " + notFound); // -5
		System.out.println("30의 삽입 위치: " + (-(notFound) - 1)); // 4

		// 부분 범위 탐색
		int partialSearch = Arrays.binarySearch(sorted, 1, 5, 22);
		System.out.println("부분 탐색 [1,5)에서 22: " + partialSearch); // 2

		// ============================================
		// 3. 채우기 - Arrays.fill()
		// ============================================
		System.out.println("\n=== 3. 채우기 ===");

		int[] filled = new int[5];
		Arrays.fill(filled, 7);
		System.out.println("전체 채우기: " + Arrays.toString(filled)); // [7, 7, 7, 7, 7]

		// 부분 채우기
		Arrays.fill(filled, 1, 4, 0);
		System.out.println("부분 채우기 [1,4): " + Arrays.toString(filled)); // [7, 0, 0, 0, 7]

		// 2D 배열 초기화
		int[][] matrix = new int[3][3];
		for (int[] row : matrix) {
			Arrays.fill(row, -1);
		}
		System.out.println("2D 채우기: " + Arrays.deepToString(matrix));

		// ============================================
		// 4. 복사 - Arrays.copyOf(), Arrays.copyOfRange()
		// ============================================
		System.out.println("\n=== 4. 복사 ===");

		int[] original = {1, 2, 3, 4, 5};

		// 전체 복사 (새 길이 지정 가능)
		int[] copy1 = Arrays.copyOf(original, original.length);
		System.out.println("전체 복사: " + Arrays.toString(copy1));

		// 길이 확장 복사 (나머지는 0으로 채움)
		int[] extended = Arrays.copyOf(original, 8);
		System.out.println("확장 복사: " + Arrays.toString(extended)); // [1, 2, 3, 4, 5, 0, 0, 0]

		// 부분 복사
		int[] partial2 = Arrays.copyOfRange(original, 1, 4);
		System.out.println("부분 복사 [1,4): " + Arrays.toString(partial2)); // [2, 3, 4]

		// ============================================
		// 5. 비교 - Arrays.equals(), Arrays.compare()
		// ============================================
		System.out.println("\n=== 5. 비교 ===");

		int[] arr1 = {1, 2, 3};
		int[] arr2 = {1, 2, 3};
		int[] arr3 = {1, 2, 4};

		System.out.println("arr1 == arr2: " + (arr1 == arr2)); // false (참조 비교)
		System.out.println("Arrays.equals: " + Arrays.equals(arr1, arr2)); // true (내용 비교)
		System.out.println("Arrays.equals: " + Arrays.equals(arr1, arr3)); // false

		// 2D 배열 비교
		int[][] mat1 = {{1, 2}, {3, 4}};
		int[][] mat2 = {{1, 2}, {3, 4}};
		System.out.println("deepEquals: " + Arrays.deepEquals(mat1, mat2)); // true

		// ============================================
		// 6. 변환 - Arrays.asList(), Stream
		// ============================================
		System.out.println("\n=== 6. 변환 ===");

		// 배열 -> List (Wrapper 타입만)
		Integer[] intArr = {1, 2, 3, 4, 5};
		List<Integer> list = Arrays.asList(intArr);
		System.out.println("배열 -> List: " + list);

		// int[] -> Integer[] -> List
		int[] primitiveArr = {1, 2, 3, 4, 5};
		List<Integer> listFromPrimitive = Arrays.stream(primitiveArr)
			.boxed()
			.toList();
		System.out.println("int[] -> List: " + listFromPrimitive);

		// 배열 -> Stream 연산
		int sum = Arrays.stream(primitiveArr).sum();
		int max = Arrays.stream(primitiveArr).max().orElse(0);
		double avg = Arrays.stream(primitiveArr).average().orElse(0);
		System.out.println("합계: " + sum + ", 최대: " + max + ", 평균: " + avg);

		// Stream으로 필터링
		int[] filtered = Arrays.stream(primitiveArr)
			.filter(n -> n % 2 == 0)
			.toArray();
		System.out.println("짝수만: " + Arrays.toString(filtered));

		// ============================================
		// 7. 기타 유용한 메서드
		// ============================================
		System.out.println("\n=== 7. 기타 ===");

		// mismatch - 두 배열이 다른 첫 번째 인덱스
		int[] m1 = {1, 2, 3, 4, 5};
		int[] m2 = {1, 2, 9, 4, 5};
		System.out.println("첫 불일치 인덱스: " + Arrays.mismatch(m1, m2));

		// setAll - 인덱스 기반 초기화 (Java 8+)
		int[] generated = new int[5];
		Arrays.setAll(generated, i -> i * i); // 인덱스의 제곱
		System.out.println("setAll (i²): " + Arrays.toString(generated)); // [0, 1, 4, 9, 16]

		// parallelSort - 병렬 정렬 (대용량 배열에 효과적)
		int[] large = new int[10000];
		Arrays.setAll(large, i -> (int)(Math.random() * 10000));
		Arrays.parallelSort(large);
		System.out.println("parallelSort 완료, 첫 5개: " +
			Arrays.toString(Arrays.copyOf(large, 5)));

		// parallelPrefix - 누적 연산 (Java 8+)
		int[] prefix = {1, 2, 3, 4, 5};
		Arrays.parallelPrefix(prefix, Integer::sum);
		System.out.println("parallelPrefix (누적합): " + Arrays.toString(prefix)); // [1, 3, 6, 10, 15]
	}
}
