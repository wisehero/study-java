package string;

import java.lang.reflect.Field;

public class StringInternalsTest {
	public static void main(String[] args) throws Exception {
		String ascii = "Hello";
		String korean = "안녕";

		inspectString("ASCII 문자열", ascii);
		inspectString("한글 문자열", korean);
	}

	private static void inspectString(String label, String s) throws Exception {
		System.out.println("=== " + label + ": \"" + s + "\" ===");

		// value 필드 접근
		Field valueField = String.class.getDeclaredField("value");
		valueField.setAccessible(true);
		byte[] value = (byte[]) valueField.get(s);

		// coder 필드 접근
		Field coderField = String.class.getDeclaredField("coder");
		coderField.setAccessible(true);
		byte coder = coderField.getByte(s);

		System.out.println("coder: " + coder + " (" + (coder == 0 ? "LATIN1" : "UTF16") + ")");
		System.out.println("value.length: " + value.length + " bytes");
		System.out.println("문자 수: " + s.length());
		System.out.print("value 내용: ");
		for (byte b : value) {
			System.out.printf("0x%02X ", b);
		}
		System.out.println("\n");
	}
}
