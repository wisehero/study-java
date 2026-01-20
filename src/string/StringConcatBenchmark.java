package string;

public class StringConcatBenchmark {
	public static void main(String[] args) {
		int[] sizes = {1000, 5000, 10000, 50000};

		for (int n : sizes) {
			// String 연결
			long start1 = System.currentTimeMillis();
			String s = "";
			for (int i = 0; i < n; i++) {
				s = s + "a";
			}
			long time1 = System.currentTimeMillis() - start1;

			// StringBuilder 연결
			long start2 = System.currentTimeMillis();
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < n; i++) {
				sb.append("a");
			}
			String result = sb.toString();
			long time2 = System.currentTimeMillis() - start2;

			System.out.println("n=" + n + " | String: " + time1 + "ms, StringBuilder: " + time2 + "ms");
		}
	}
}