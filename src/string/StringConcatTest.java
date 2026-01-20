package string;

public class StringConcatTest {
	public static void main(String[] args) {
		String base = "Hello";

		// 컴파일 타임 상수 결합
		String compile1 = "Hel" + "lo";
		String compile2 = "He" + "l" + "lo";

		// final 변수 결합 (컴파일 타임 상수)
		final String part1 = "Hel";
		final String part2 = "lo";
		String compile3 = part1 + part2;

		// 런타임 결합
		String nonFinal = "Hel";
		String runtime1 = nonFinal + "lo";

		String runtime2 = "Hello".substring(0, 3) + "lo";

		System.out.println("=== 컴파일 타임 상수 ===");
		System.out.println("base == compile1: " + (base == compile1));
		System.out.println("base == compile2: " + (base == compile2));
		System.out.println("base == compile3: " + (base == compile3));

		System.out.println("\n=== 런타임 결합 ===");
		System.out.println("base == runtime1: " + (base == runtime1));
		System.out.println("base == runtime2: " + (base == runtime2));

		System.out.println("\n=== intern() 적용 후 ===");
		System.out.println("base == runtime1.intern(): " + (base == runtime1.intern()));
	}
}
