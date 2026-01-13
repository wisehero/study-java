package datastructure.phase02;

import java.util.ArrayDeque;
import java.util.Deque;

public class ArrayDequeDemo {
	public static void main(String[] args) {

		// Stack으로 사용 (LIFO)
		Deque<String> stack = new ArrayDeque<>();
		stack.push("A");
		stack.push("B");
		stack.push("C");

		System.out.println(stack.pop()); // "C"
		System.out.println(stack.pop()); // "B"
		System.out.println(stack.pop()); // "A"

		// Queue로 사용 (LIFO)
		Deque<String> queue = new ArrayDeque<>();
		queue.offer("A");
		queue.offer("B");
		queue.offer("C");

		System.out.println(queue.poll()); // A
		System.out.println(queue.poll()); // B
		System.out.println(queue.poll()); // C
	}
}
