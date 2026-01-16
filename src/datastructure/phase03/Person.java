package datastructure.phase03;

import java.util.Objects;

public class Person {

	private String name;
	private int age;

	public Person(String name, int age) {
		this.name = name;
		this.age = age;
	}

	@Override
	public boolean equals(Object o) {
		// 같은 객체면 true
		if (this == o)
			return true;

		// null 이거나 다른 클래스면 false
		if (o == null || getClass() != o.getClass())
			return false;

		// 내용 비교
		Person person = (Person)o;
		return age == person.age && Objects.equals(name, person.name);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, age);
	}
}
