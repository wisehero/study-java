package datastructure.phase01.array;

public class ArrayBasicOperations {

	private int[] arr;
	private int size; // 실제 사용중인 배열의 요소 개수

	public ArrayBasicOperations(int capacity) {
		this.arr = new int[capacity];
		this.size = 0;
	}

	// 접근 - O(1)
	public int get(int index) {
		if (index < 0 || index >= size) {
			throw new IndexOutOfBoundsException("Index: " + index);
		}
		return arr[index];
	}

	// 수정 - O(1)
	public void set(int index, int value) {
		if (index < 0 | index >= size) {
			throw new IndexOutOfBoundsException("Index: " + index);
		}
		arr[index] = value;
	}

	// 끝에 삽입 - O(1)
	public void addLast(int value) {
		if (size >= arr.length) {
			throw new IllegalStateException("배열이 가득 찼습니다.");
		}
		arr[size++] = value;
	}

	// 특정 위치에 삽입 - O(n)
	public void addAt(int index, int value) {
		if (size >= arr.length) {
			throw new IllegalStateException("배열이 가득 찼습니다.");
		}

		if (index < 0 || index > size) {
			throw new IndexOutOfBoundsException("Index: " + index);
		}

		// index부터 끝까지 한 칸씩 뒤로 이동
		for (int i = size; i > index; i--) {
			arr[i] = arr[i - 1];
		}
		arr[index] = value;
		size++;
	}

	// 끝에서 삭제 - O(1)
	public int removeLast(int value) {
		if (size == 0) {
			throw new IllegalStateException("배열이 비어있습니다.");
		}
		return arr[--size];
	}

	// 특정 위치에서 삭제 - O(n)
	// 이유: 삭제 위치 이후의 모든 요소를 한 칸씩 앞으로 이동해야 함
	public int removeAt(int index) {
		if (index < 0 | index >= size) {
			throw new IndexOutOfBoundsException("Index: " + index);
		}

		int removed = arr[index];
		// index + 1부터 끝까지 한 칸씩 앞으로 이동
		for (int i = index; i < size; i++) {
			arr[i] = arr[i + 1];
		}
		size--;
		return removed;
	}

	// 값으로 검색 - O(n)
	public int indexOf(int value) {
		for (int i = 0; i < size; i++) {
			if (arr[i] == value) {
				return i;
			}
		}
		return -1;
	}

	public int size() {
		return size;
	}

	public void print() {
		System.out.print("[");
		for (int i = 0; i < size; i++) {
			System.out.print(arr[i]);
			if (i < size - 1)
				System.out.print(", ");
		}
		System.out.println("]");
	}

	public static void main(String[] args) {
		ArrayBasicOperations array = new ArrayBasicOperations(10);

		// 삽입 테스트
		array.addLast(10);
		array.addLast(20);
		array.addLast(30);
		System.out.print("초기 배열 : ");
		array.print(); // [10, 20, 30]

		// 중간 삽입 O(n)
		array.addAt(1, 15);
		System.out.println("인덱스 1에 15 삽입: ");
		array.print();

		// 접근 - O(1)
		System.out.println("인덱스 2의 값: " + array.get(2)); // 20

		// 수정 - O(1)
		array.set(2, 25);
		System.out.println("인덱스 2를 25로 수정: ");
		array.print(); // [10, 15, 25, 30]

		// 중간 삭제 - O(n)
		array.removeAt(1);
		System.out.print("인덱스 1 삭제: ");
		array.print(); // [10, 25, 30]

		// 검색 - O(n)
		System.out.println("값 25의 인덱스: " + array.indexOf(25)); // 1
	}
}
