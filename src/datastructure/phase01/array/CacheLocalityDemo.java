package datastructure.phase01.array;

public class CacheLocalityDemo {

	public static void main(String[] args) {

		int size = 10000;
		int[][] matrix = new int[size][size];

		// 행 우선 순회 (Row-major) - 캐시 친화적
		long start1 = System.currentTimeMillis();
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				matrix[i][j] = i + j;
			}
		}

		long end1 = System.currentTimeMillis();

		// 열 우선 순회(Column-major) 캐시 비친화적
		long start2 = System.currentTimeMillis();
		for (int j = 0; j < size; j++) {
			for (int i = 0; i < size; i++) {
				matrix[i][j] = i + j;
			}
		}
		long end2 = System.currentTimeMillis();

		System.out.println("행 우선: " + (end1 - start1) + "ms");
		System.out.println("열 우선: " + (end2 - start2) + "ms");
	}
}
