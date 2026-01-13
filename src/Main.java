import java.util.*;

public class Main {

	public static void main(String[] args) {

	}

	public int[] solution(int[] arr, int[] delete_list) {
		List<Integer> list = new ArrayList<>();

		Set<Integer> set = new HashSet<>();
		for (int e : delete_list) {
			set.add(e);
		}

		for (int e : arr) {
			if (!set.contains(e)) {
				list.add(e);
			}
		}

		return list.stream()
			.mapToInt(i -> i)
			.toArray();
	}
}
