/*
 * Copyright 2021-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.data.relational.repository.query;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.data.domain.ExampleMatcher.*;
import static org.springframework.data.domain.ExampleMatcher.GenericPropertyMatchers.*;
import static org.springframework.data.domain.ExampleMatcher.StringMatcher.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.relational.core.mapping.RelationalMappingContext;
import org.springframework.data.relational.core.query.Query;
import org.springframework.lang.Nullable;

/**
 * Verify that the {@link RelationalExampleMapper} properly turns {@link Example}s into {@link Query}'s.
 *
 * @author Greg Turnquist
 * @author Jens Schauder
 */
class RelationalExampleMapperTests {

	RelationalExampleMapper exampleMapper;

	@BeforeEach
	void before() {
		exampleMapper = new RelationalExampleMapper(new RelationalMappingContext());
	}

	@Test // GH-929
	void queryByExampleWithId() {

		Person person = new Person("id1", null, null, null, null, null);

		Example<Person> example = Example.of(person);

		Query query = exampleMapper.getMappedExample(example);

		assertThat(query.getCriteria()) //
				.map(Objects::toString) //
				.hasValue("(id = 'id1')");
	}

	@Test // GH-929
	void queryByExampleWithFirstname() {

		Person person = new Person(null, "Frodo", null, null, null, null);

		Example<Person> example = Example.of(person);

		Query query = exampleMapper.getMappedExample(example);

		assertThat(query.getCriteria()) //
				.map(Object::toString) //
				.hasValue("(firstname = 'Frodo')");
	}

	@Test // GH-929
	void queryByExampleWithFirstnameAndLastname() {

		Person person = new Person(null, "Frodo", "Baggins", null, null, null);

		Example<Person> example = Example.of(person);

		Query query = exampleMapper.getMappedExample(example);
		assertThat(query.getCriteria().map(Object::toString).get()) //
				.contains("(firstname = 'Frodo')", //
						" AND ", //
						"(lastname = 'Baggins')");
	}

	@Test // GH-929
	void queryByExampleWithNullMatchingLastName() {

		Person person = new Person(null, null, "Baggins", null, null, null);

		ExampleMatcher matcher = matching().withIncludeNullValues();
		Example<Person> example = Example.of(person, matcher);

		Query query = exampleMapper.getMappedExample(example);

		assertThat(query.getCriteria()) //
				.map(Object::toString) //
				.hasValue("(lastname IS NULL OR lastname = 'Baggins')");
	}

	@Test // GH-929
	void queryByExampleWithNullMatchingFirstnameAndLastname() {

		Person person = new Person(null, "Bilbo", "Baggins", null, null, null);

		ExampleMatcher matcher = matching().withIncludeNullValues();
		Example<Person> example = Example.of(person, matcher);

		Query query = exampleMapper.getMappedExample(example);
		assertThat(query.getCriteria().map(Object::toString).get()) //
				.contains("(firstname IS NULL OR firstname = 'Bilbo')", //
						" AND ", //
						"(lastname IS NULL OR lastname = 'Baggins')");
	}

	@Test // GH-929
	void queryByExampleWithFirstnameAndLastnameIgnoringFirstname() {

		Person person = new Person(null, "Bilbo", "Baggins", null, null, null);

		ExampleMatcher matcher = matching().withIgnorePaths("firstname");
		Example<Person> example = Example.of(person, matcher);

		Query query = exampleMapper.getMappedExample(example);

		assertThat(query.getCriteria()) //
				.map(Object::toString) //
				.hasValue("(lastname = 'Baggins')");
	}

	@Test // GH-929
	void queryByExampleWithFirstnameAndLastnameWithNullMatchingIgnoringFirstName() {

		Person person = new Person(null, "Bilbo", "Baggins", null, null, null);

		ExampleMatcher matcher = matching().withIncludeNullValues().withIgnorePaths("firstname");
		Example<Person> example = Example.of(person, matcher);

		Query query = exampleMapper.getMappedExample(example);

		assertThat(query.getCriteria()) //
				.map(Object::toString) //
				.hasValue("(lastname IS NULL OR lastname = 'Baggins')");
	}

	@Test // GH-929
	void queryByExampleWithFirstnameWithStringMatchingAtTheBeginning() {

		Person person = new Person(null, "Fro", null, null, null, null);

		ExampleMatcher matcher = matching().withStringMatcher(STARTING);
		Example<Person> example = Example.of(person, matcher);

		Query query = exampleMapper.getMappedExample(example);

		assertThat(query.getCriteria()) //
				.map(Object::toString) //
				.hasValue("(firstname LIKE 'Fro%')");
	}

	@Test // GH-929
	void queryByExampleWithFirstnameWithStringMatchingOnTheEnding() {

		Person person = new Person(null, "do", null, null, null, null);

		ExampleMatcher matcher = matching().withStringMatcher(ENDING);
		Example<Person> example = Example.of(person, matcher);

		Query query = exampleMapper.getMappedExample(example);

		assertThat(query.getCriteria()) //
				.map(Object::toString) //
				.hasValue("(firstname LIKE '%do')");
	}

	@Test // GH-929
	void queryByExampleWithFirstnameWithStringMatchingContaining() {

		Person person = new Person(null, "do", null, null, null, null);

		ExampleMatcher matcher = matching().withStringMatcher(CONTAINING);
		Example<Person> example = Example.of(person, matcher);

		Query query = exampleMapper.getMappedExample(example);

		assertThat(query.getCriteria()) //
				.map(Object::toString) //
				.hasValue("(firstname LIKE '%do%')");
	}

	@Test // GH-929
	void queryByExampleWithFirstnameWithStringMatchingRegEx() {

		Person person = new Person(null, "do", null, null, null, null);

		ExampleMatcher matcher = matching().withStringMatcher(ExampleMatcher.StringMatcher.REGEX);
		Example<Person> example = Example.of(person, matcher);

		assertThatIllegalStateException().isThrownBy(() -> exampleMapper.getMappedExample(example))
				.withMessageContaining("REGEX is not supported");
	}

	@Test // GH-929
	void queryByExampleWithFirstnameWithFieldSpecificStringMatcherEndsWith() {

		Person person = new Person(null, "do", null, null, null, null);

		ExampleMatcher matcher = matching().withMatcher("firstname", endsWith());
		Example<Person> example = Example.of(person, matcher);

		Query query = exampleMapper.getMappedExample(example);

		assertThat(query.getCriteria()) //
				.map(Object::toString) //
				.hasValue("(firstname LIKE '%do')");
	}

	@Test // GH-929
	void queryByExampleWithFirstnameWithFieldSpecificStringMatcherStartsWith() {

		Person person = new Person(null, "Fro", null, null, null, null);

		ExampleMatcher matcher = matching().withMatcher("firstname", startsWith());
		Example<Person> example = Example.of(person, matcher);

		Query query = exampleMapper.getMappedExample(example);

		assertThat(query.getCriteria()) //
				.map(Object::toString) //
				.hasValue("(firstname LIKE 'Fro%')");
	}

	@Test // GH-929
	void queryByExampleWithFirstnameWithFieldSpecificStringMatcherContains() {

		Person person = new Person(null, "do", null, null, null, null);

		ExampleMatcher matcher = matching().withMatcher("firstname", contains());
		Example<Person> example = Example.of(person, matcher);

		Query query = exampleMapper.getMappedExample(example);

		assertThat(query.getCriteria()) //
				.map(Object::toString) //
				.hasValue("(firstname LIKE '%do%')");
	}

	@Test // GH-929
	void queryByExampleWithFirstnameWithStringMatchingAtTheBeginningIncludingNull() {

		Person person = new Person(null, "Fro", null, null, null, null);

		ExampleMatcher matcher = matching().withStringMatcher(STARTING).withIncludeNullValues();
		Example<Person> example = Example.of(person, matcher);

		Query query = exampleMapper.getMappedExample(example);

		assertThat(query.getCriteria()) //
				.map(Object::toString) //
				.hasValue("(firstname IS NULL OR firstname LIKE 'Fro%')");
	}

	@Test // GH-929
	void queryByExampleWithFirstnameWithStringMatchingOnTheEndingIncludingNull() {

		Person person = new Person(null, "do", null, null, null, null);

		ExampleMatcher matcher = matching().withStringMatcher(ENDING).withIncludeNullValues();
		Example<Person> example = Example.of(person, matcher);

		Query query = exampleMapper.getMappedExample(example);

		assertThat(query.getCriteria()) //
				.map(Object::toString) //
				.hasValue("(firstname IS NULL OR firstname LIKE '%do')");
	}

	@Test // GH-929
	void queryByExampleWithFirstnameIgnoreCaseFieldLevel() {

		Person person = new Person(null, "fro", null, null, null, null);

		ExampleMatcher matcher = matching().withMatcher("firstname", startsWith().ignoreCase());
		Example<Person> example = Example.of(person, matcher);

		Query query = exampleMapper.getMappedExample(example);

		assertThat(query.getCriteria()) //
				.map(Object::toString) //
				.hasValue("(firstname LIKE 'fro%')");

		assertThat(example.getMatcher().getPropertySpecifiers().getForPath("firstname").getIgnoreCase()).isTrue();
	}

	@Test // GH-929
	void queryByExampleWithFirstnameWithStringMatchingContainingIncludingNull() {

		Person person = new Person(null, "do", null, null, null, null);

		ExampleMatcher matcher = matching().withStringMatcher(CONTAINING).withIncludeNullValues();
		Example<Person> example = Example.of(person, matcher);

		Query query = exampleMapper.getMappedExample(example);

		assertThat(query.getCriteria()) //
				.map(Object::toString) //
				.hasValue("(firstname IS NULL OR firstname LIKE '%do%')");
	}

	@Test // GH-929
	void queryByExampleWithFirstnameIgnoreCase() {

		Person person = new Person(null, "Frodo", null, null, null, null);

		ExampleMatcher matcher = matching().withIgnoreCase(true);
		Example<Person> example = Example.of(person, matcher);

		Query query = exampleMapper.getMappedExample(example);

		assertThat(query.getCriteria()) //
				.map(Object::toString) //
				.hasValue("(firstname = 'Frodo')");

		assertThat(example.getMatcher().isIgnoreCaseEnabled()).isTrue();
	}

	@Test // GH-929
	void queryByExampleWithFirstnameOrLastname() {

		Person person = new Person(null, "Frodo", "Baggins", null, null, null);

		ExampleMatcher matcher = matchingAny();
		Example<Person> example = Example.of(person, matcher);

		Query query = exampleMapper.getMappedExample(example);
		assertThat(query.getCriteria().map(Object::toString).get()) //
				.contains("(firstname = 'Frodo')", //
						" OR ", //
						"(lastname = 'Baggins')");
	}

	@Test // GH-929
	void queryByExampleEvenHandlesInvisibleFields() {

		Person person = new Person(null, "Frodo", null, "I have the ring!", null, null);

		Example<Person> example = Example.of(person);

		Query query = exampleMapper.getMappedExample(example);

		assertThat(query.getCriteria().map(Object::toString).get()) //
				.contains("(firstname = 'Frodo')", //
						" AND ", //
						"(secret = 'I have the ring!')");
	}

	@Test // GH-929
	void queryByExampleSupportsPropertyTransforms() {

		Person person = new Person(null, "Frodo", "Baggins", "I have the ring!", null, null);

		ExampleMatcher matcher = matching() //
				.withTransformer("firstname", o -> {
					if (o.isPresent()) {
						return o.map(o1 -> ((String) o1).toUpperCase());
					}
					return o;
				}) //
				.withTransformer("lastname", o -> {
					if (o.isPresent()) {
						return o.map(o1 -> ((String) o1).toLowerCase());
					}
					return o;
				});

		Example<Person> example = Example.of(person, matcher);

		Query query = exampleMapper.getMappedExample(example);

		assertThat(query.getCriteria().map(Object::toString).get()) //
				.contains("(firstname = 'FRODO')", //
						" AND ", //
						"(lastname = 'baggins')", //
						"(secret = 'I have the ring!')");
	}

	@Test // GH-1969
	void collectionLikeAttributesGetIgnored() {

		Example<Person> example = Example.of(new Person(null, "Frodo", null, null, List.of(new Possession("Ring")), null));

		Query query = exampleMapper.getMappedExample(example);

		assertThat(query.getCriteria().orElseThrow().toString()).doesNotContainIgnoringCase("possession");
	}

	@Test // GH-1969
	void mapAttributesGetIgnored() {

		Example<Person> example = Example
				.of(new Person(null, "Frodo", null, null, null, Map.of("Home", new Address("Bag End"))));

		Query query = exampleMapper.getMappedExample(example);

		assertThat(query.getCriteria().orElseThrow().toString()).doesNotContainIgnoringCase("address");
	}

	record Person(@Id @Nullable String id, @Nullable String firstname, @Nullable String lastname, @Nullable String secret,
			@Nullable List<Possession> possessions, @Nullable Map<String, Address> addresses) {
	}

	record Possession(String name) {
	}

	record Address(String description) {
	}
}
