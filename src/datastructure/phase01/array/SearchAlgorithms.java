package datastructure.phase01.array;

public class SearchAlgorithms {

	// 선형 탐색 - O(n)
	// 정렬되지 않은 배열에서도 사용 가능
	public static int linearSearch(int[] arr, int target) {
		for (int i = 0; i < arr.length; i++) {
			if (arr[i] == target) {
				return i;
			}
		}
		return -1;
	}

	// 이진 탐색 (반복문) - O(log n)
	// 정렬된 배열에서만 사용할 수 있다.
	public static int binarySearch(int[] arr, int target) {
		int left = 0;
		int right = arr.length - 1;

		while (left <= right) {
			int mid = left + (right - left) / 2;

			if (arr[mid] == target) {
				return mid;
			} else if (arr[mid] < target) {
				left = mid + 1;
			} else {
				right = mid - 1;
			}
		}

		return -1;
	}
}
