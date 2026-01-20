package string;

public class HashCodeCachingTest {
	public static void main(String[] args) {
		String longString = "a".repeat(1_000_000);

		// 첫 번째 호출 : 해시코드 계산
		long start1 = System.nanoTime();
		int hash1 = longString.hashCode();
		long time1 = System.nanoTime() - start1;

		// 두 번째 호출 : 캐싱된 값 반환
		long start2 = System.nanoTime();
		int hash2 = longString.hashCode();
		long time2 = System.nanoTime() - start2;

		System.out.println("첫 번째 호출 시간: " + time1 + "ns");
		System.out.println("두 번째 호출 시간: " + time2 + "ns");
		System.out.println("속도 향상: " + (double)time1 / time2 + "배");

	}
}
