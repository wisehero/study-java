package string;

public class ImmutableTest {
	public static void main(String[] args) {
		String original = "Hello";
		String modified = original.concat(" World");

		System.out.println("original: " + original);
		System.out.println("modified: " + modified);
		System.out.println("같은 객체? " + (original == modified));
	}
}
