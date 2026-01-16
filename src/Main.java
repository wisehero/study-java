import java.util.*;

public class Main {

	public static void main(String[] args) {

		String s = "43 + 12";
		String[] split = s.split(" ");
		int a = Integer.parseInt(split[0]);
		String op = split[1];
		int b = Integer.parseInt(split[2]);

		switch (op) {
			case "+":
				System.out.println(a + b);
				break;
			case "-":
				System.out.println(a - b);
				break;
			case "*":
				System.out.println(a * b);
		}
		System.out.println(Arrays.toString(split));
	}
}
