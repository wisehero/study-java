package string;

public class SyncOverheadTest {
	public static void main(String[] args) {
		int iterations = 10_000_000;

		// StringBuilder
		StringBuilder sb = new StringBuilder();
		long start1 = System.currentTimeMillis();
		for (int i = 0; i < iterations; i++) {
			sb.append("a");
			sb.setLength(0);  // 초기화
		}
		System.out.println("StringBuilder: " + (System.currentTimeMillis() - start1) + "ms");

		// StringBuffer
		StringBuffer sbuf = new StringBuffer();
		long start2 = System.currentTimeMillis();
		for (int i = 0; i < iterations; i++) {
			sbuf.append("a");
			sbuf.setLength(0);  // 초기화
		}
		System.out.println("StringBuffer: " + (System.currentTimeMillis() - start2) + "ms");
	}
}
