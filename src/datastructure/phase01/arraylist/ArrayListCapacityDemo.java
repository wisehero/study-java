package datastructure.phase01.arraylist;

import java.util.ArrayList;

public class ArrayListCapacityDemo {
	public static void main(String[] args) {
		int count = 1_000_000;

		// 초기 용량 미지정 - grow()가 여러 번 발생
		long start1 = System.currentTimeMillis();
		ArrayList<Integer> list1 = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			list1.add(1);
		}
		long end1 = System.currentTimeMillis();

		// 초기 용량 지정 - grow() 없음
		long start2 = System.currentTimeMillis();
		ArrayList<Integer> list2 = new ArrayList<>(count);
		for (int i = 0; i < count; i++) {
			list2.add(i);
		}
		long end2 = System.currentTimeMillis();

		System.out.println("미지정: " + (end1 - start1) + "ms");
		System.out.println("지정: " + (end2 - start2) + "ms");
	}
}
