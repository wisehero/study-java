package datastructure.phase01.arraylist;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class ArrayListBasics {
	public static void main(String[] args) {
		// ============================================
		// 1. 생성과 초기화
		// ============================================
		System.out.println("=== 1. 생성과 초기화 ===");

		// 기본 생성 (초기 용량 10)
		List<Integer> list1 = new ArrayList<>();

		// 초기 용량 지정(재할당 줄이기)
		List<Integer> list2 = new ArrayList<>(100);

		// 값과 함께 초기화
		List<Integer> list3 = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5));
		System.out.println("Arrays.asList: " + list3);

		// Java 9+ List.of() - 불변 리스트 생성 후 복사
		List<Integer> list4 = new ArrayList<>(List.of(1, 2, 3, 4, 5));
		System.out.println("List.of: " + list4);

		// Collections.nCopies - 같은 값으로 채우기
		List<Integer> list5 = new ArrayList<>(Collections.nCopies(5, 0));
		System.out.println("nCopies(5, 0): " + list5); // [0, 0, 0, 0, 0]

		// ============================================
		// 2. 기본 CRUD 연산
		// ============================================
		System.out.println("\n=== 2. 기본 CRUD ===");

		List<String> fruits = new ArrayList<>();

		// 추가 - add()
		fruits.add("Apple");
		fruits.add("Banana");
		fruits.add("Cherry");
		fruits.add(1, "Apricot"); // 특정 위치에 삽입
		System.out.println("추가 후: " + fruits);

		// 읽기 - get()
		System.out.println("인덱스 2: " + fruits.get(2));

		// 수정 - set()
		fruits.set(2, "BlueBerry");
		System.out.println("수정 후: " + fruits);

		// 삭제 - remove()
		fruits.remove("Apple"); // 값으로 삭제 (첫 번째 일치 항목)
		fruits.remove(0); // 인덱스로 삭제
		System.out.println("삭제 후: " + fruits);

		// 주의: Integer 리스트에서 remove
		List<Integer> nums = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5));
		nums.remove(Integer.valueOf(3)); // 값 3 삭제
		nums.remove(0); // 인덱스 0 삭제
		System.out.println("Integer 삭제: " + nums); // [2, 4, 5]

		// ============================================
		// 3. 검색과 확인
		// ============================================
		System.out.println("\n=== 3. 검색과 확인 ===");

		List<String> colors = new ArrayList<>(
			Arrays.asList("Red", "Green", "Blue", "Green", "Yellow")
		);

		System.out.println("contains(Green): " + colors.contains("Green")); // true
		System.out.println("indexOf(Green): " + colors.indexOf("Green")); // 1
		System.out.println("lastIndexOf(Green): " + colors.lastIndexOf("Green")); // 3
		System.out.println("isEmpty: " + colors.isEmpty()); // false
		System.out.println("size: " + colors.size()); // 5

		// ============================================
		// 4. 벌크 연산
		// ============================================
		System.out.println("\n=== 4. 벌크 연산 ===");

		List<Integer> list = new ArrayList<>(Arrays.asList(1, 2, 3));
		List<Integer> toAdd = Arrays.asList(4, 5, 6);

		// 전체 추가
		list.addAll(toAdd);
		System.out.println("addAll: " + list);

		// 특정 위치에 전체 추가
		list.addAll(2, Arrays.asList(10, 11));

		// 교집합만 유지
		List<Integer> retain = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5));
		retain.retainAll(Arrays.asList(2, 4, 6));
		System.out.println("retainAll: " + retain); // [2, 4]

		// 차집합 (특정 요소들 제거)
		List<Integer> remove = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5));
		remove.removeAll(Arrays.asList(2, 4));
		System.out.println("removeAll: " + remove); // [1, 3, 5]

		// ============================================
		// 5. 정렬
		// ============================================
		System.out.println("\n=== 5. 정렬 ===");

		List<Integer> unsorted = new ArrayList<>(Arrays.asList(64, 34, 25, 12, 22));

		// 오름차순
		Collections.sort(unsorted);
		System.out.println("오름차순: " + unsorted);

		// 내림차순
		Collections.sort(unsorted, Collections.reverseOrder());
		System.out.println("내림차순: " + unsorted);

		// Java 8+ List.sort()
		unsorted.sort(Comparator.naturalOrder());
		System.out.println("naturalOrder: " + unsorted);

		// 커스텀 정렬 - 객체 리스트
		List<int[]> intervals = new ArrayList<>();
		intervals.add(new int[] {3, 5});
		intervals.add(new int[] {1, 4});
		intervals.add(new int[] {2, 6});
		intervals.sort(Comparator.comparingInt(a -> a[0]));
		System.out.print("구간 정렬: ");
		intervals.forEach(i -> System.out.print(Arrays.toString(i) + " "));
		System.out.println();

		// ============================================
		// 6. 변환
		// ============================================
		System.out.println("\n=== 6. 변환 ===");

		List<Integer> numList = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5));

		// List -> 배열
		Integer[] arr1 = numList.toArray(new Integer[0]);
		System.out.println("List -> Integer[]: " + Arrays.toString(arr1));

		// List<Integer> -> int[]
		int[] arr2 = numList.stream().mapToInt(Integer::intValue).toArray();
		System.out.println("List -> int[]: " + Arrays.toString(arr2));

		// int[] -> List<Integer>
		int[] primitiveArr = {1, 2, 3, 4, 5};
		List<Integer> fromArr = Arrays.stream(primitiveArr)
			.boxed()
			.toList();
		System.out.println("int[] -> List: " + fromArr);

		// 부분 리스트 (뷰)
		List<Integer> subList = numList.subList(1, 4);
		System.out.println("subList(1,4): " + subList); // [2, 3, 4]

		// ============================================
		// 7. 순회
		// ============================================
		System.out.println("\n=== 7. 순회 ===");

		List<String> items = new ArrayList<>(Arrays.asList("A", "B", "C"));

		// for-each
		System.out.print("for-each: ");
		for (String item : items) {
			System.out.print(item + " ");
		}

		// 인덱스가 필요할 때
		System.out.print("for-i: ");
		for (int i = 0; i < items.size(); i++) {
			System.out.print(i + ":" + items.get(i) + " ");
		}
		System.out.println();

		// forEach + 람다
		System.out.print("forEach: ");
		items.forEach(item -> System.out.print(item + " "));
		System.out.println();

		// Iterator (순회 중 삭제 가능)
		List<Integer> mutable = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5));
		Iterator<Integer> it = mutable.iterator();
		while (it.hasNext()) {
			if (it.next() % 2 == 0) {
				it.remove();
			}
		}
		System.out.println("Iterator 삭제: " + mutable); // [1, 3, 5]

		// removeIf (Java 8+)
		List<Integer> mutable2 = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5));
		mutable2.removeIf(n -> n % 2 == 0);
		System.out.println();

		// ============================================
		// 8. 유용한 Collections 메서드
		// ============================================
		System.out.println("\n=== 8. Collections 유틸 ===");

		List<Integer> data = new ArrayList<>(Arrays.asList(3, 1, 4, 1, 5, 9, 2, 6));

		System.out.println("max: " + Collections.max(data)); // 9
		System.out.println("min: " + Collections.min(data)); // 1
		System.out.println("frequency(1): " + Collections.frequency(data, 1)); // 2

		// 뒤집기
		Collections.reverse(data);
		System.out.println("reverse: " + data);

		// 셔플
		Collections.shuffle(data);
		System.out.println("shuffle: " + data);

		// 회전
		List<Integer> rotateList = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5));
		Collections.rotate(rotateList, 2); // 오른쪽으로 2칸
		System.out.println("rotate(2): " + rotateList); // [4, 5, 1, 2, 3]

		// 스왑
		List<Integer> swapList = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5));
		Collections.swap(swapList, 0, 4);
		System.out.println("swap(0, 4): " + swapList);

		// 이진 탐색 (정렬된 리스트에서)
		List<Integer> sortedList = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5));
		int idx = Collections.binarySearch(sortedList, 3);
		System.out.println("binarySearch(3): " + idx);
	}
}
