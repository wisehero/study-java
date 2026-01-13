package datastructure.phase02;

import java.util.PriorityQueue;

public class PriorityQueueDemo {
	public static void main(String[] args) {

		// 기본: 작은 값이 높은 우선순위 (Min-Heap)
		PriorityQueue<Integer> minHeap = new PriorityQueue<>();
		minHeap.offer(5);
		minHeap.offer(1);
		minHeap.offer(3);
		minHeap.offer(2);

		// 꺼내면 작은 순서대로 나온다.
		System.out.println(minHeap.poll()); // 1
		System.out.println(minHeap.poll()); // 2
		System.out.println(minHeap.poll()); // 3
		System.out.println(minHeap.poll()); // 5

		PriorityQueue<Integer> maxHeap = new PriorityQueue<>(
			(a, b) -> b - a
		);
		maxHeap.offer(5);
		maxHeap.offer(1);
		maxHeap.offer(3);

		System.out.println(maxHeap.poll()); // 5
		System.out.println(maxHeap.poll()); // 3
		System.out.println(maxHeap.poll()); // 1
	}
}
