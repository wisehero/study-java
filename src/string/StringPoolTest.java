package string;

public class StringPoolTest {
	public static void main(String[] args) {
		String literal1 = "Hello";
		String literal2 = "Hello";
		String newString = new String("Hello");
		String interned = newString.intern();

		System.out.println("literal1 == literal2: " + (literal1 == literal2));
		System.out.println("literal1 == newString: " + (literal1 == newString));
		System.out.println("literal1 == interned: " + (literal1 == interned));
		System.out.println("literal1.equals(newString): " + literal1.equals(newString));
	}
}
