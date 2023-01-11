package org.hibernate.bugs;

import org.hibernate.entity.User;
import org.hibernate.entity.UserDetail;
import org.hibernate.entity.UserSkill;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.persistence.*;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * This template demonstrates how to develop a test case for Hibernate ORM, using the Java Persistence API.
 */
public class JPAUnitTestCase {

	private EntityManagerFactory entityManagerFactory;

	@Before
	public void init() {
		entityManagerFactory = Persistence.createEntityManagerFactory( "templatePU" );
	}

	@After
	public void destroy() {
		deleteAllRecords();
		entityManagerFactory.close();
	}

	public User findUserByIdUsingEntityGraph(Long id) {
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		entityManager.getTransaction().begin();

		EntityGraph<?> entityGraph = entityManager.getEntityGraph("user-entity-graph");

		Map<String, Object> properties = new HashMap<>();
		properties.put("javax.persistence.fetchgraph", entityGraph);

		User user = entityManager.find(User.class, id, properties);

		entityManager.getTransaction().commit();
		entityManager.close();

		return user;
	}

	public User findUserByNameUsingEntityGraph(String name) {
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		entityManager.getTransaction().begin();

		EntityGraph<?> entityGraph = entityManager.getEntityGraph("user-entity-graph");

		TypedQuery<User> query = entityManager.createQuery("SELECT u FROM User u WHERE u.name = :name", User.class)
				.setParameter("name", name)
				.setHint("javax.persistence.loadgraph", entityGraph);

		User user = query.getSingleResult();

		entityManager.getTransaction().commit();
		entityManager.close();

		return user;
	}

	public void deleteAllRecords() {
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		entityManager.getTransaction().begin();

		entityManager.createNativeQuery("DELETE FROM user_skills").executeUpdate();
		entityManager.createNativeQuery("DELETE FROM user_details").executeUpdate();
		entityManager.createNativeQuery("DELETE FROM users").executeUpdate();

		entityManager.getTransaction().commit();
		entityManager.close();
	}

	@Test
	public void findUserByIdWithNoDetailAndNoSkillTest() {
		// Given
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		entityManager.getTransaction().begin();

		User user = new User();
		user.setName("Alice");
		entityManager.persist(user);

		entityManager.getTransaction().commit();
		entityManager.close();

		Long userId = user.getId();

		// When
		User alice = findUserByIdUsingEntityGraph(userId);

		// Then
		assertNotNull(alice);
		assertEquals("Alice", alice.getName());

		assertNull(alice.getDetail());

		assertTrue(alice.getSkills().isEmpty());
	}

	@Test
	public void findUserByNameWithNoDetailAndNoSkillTest() {
		// Given
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		entityManager.getTransaction().begin();

		User user = new User();
		user.setName("Alice");
		entityManager.persist(user);

		entityManager.getTransaction().commit();
		entityManager.close();

		// When
		User alice = findUserByNameUsingEntityGraph("Alice");

		// Then
		assertNotNull(alice);
		assertEquals("Alice", alice.getName());

		assertNull(alice.getDetail());

		assertTrue(alice.getSkills().isEmpty());
	}

	@Test
	public void findUserByIdWithInactiveDetailAndNoSkillTest() {
		// Given
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		entityManager.getTransaction().begin();

		User user = new User();
		user.setName("Bob");
		entityManager.persist(user);

		UserDetail detail = new UserDetail();
		detail.setCity("New York");
		detail.setActive(false);
		detail.setUser(user);
		entityManager.persist(detail);

		entityManager.getTransaction().commit();
		entityManager.close();

		Long userId = user.getId();

		// When
		User bob = findUserByIdUsingEntityGraph(userId);

		// Then
		assertNotNull(bob);
		assertEquals("Bob", bob.getName());

		assertNull(bob.getDetail());

		assertTrue(bob.getSkills().isEmpty());
	}

	@Test
	public void findUserByNameWithInactiveDetailAndNoSkillTest() {
		// Given
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		entityManager.getTransaction().begin();

		User user = new User();
		user.setName("Bob");
		entityManager.persist(user);

		UserDetail detail = new UserDetail();
		detail.setCity("New York");
		detail.setActive(false);
		detail.setUser(user);
		entityManager.persist(detail);

		entityManager.getTransaction().commit();
		entityManager.close();

		// When
		User bob = findUserByNameUsingEntityGraph("Bob");

		// Then
		assertNotNull(bob);
		assertEquals("Bob", bob.getName());

		assertNull(bob.getDetail());

		assertTrue(bob.getSkills().isEmpty());
	}

	@Test
	public void findUserByIdWithActiveDetailAndDeletedSkillTest() {
		// Given
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		entityManager.getTransaction().begin();

		User user = new User();
		user.setName("Charlie");
		entityManager.persist(user);

		UserDetail detail = new UserDetail();
		detail.setCity("Paris");
		detail.setActive(true);
		detail.setUser(user);
		entityManager.persist(detail);

		UserSkill skill = new UserSkill();
		skill.setSkillName("Java");
		skill.setDeleted(true);
		skill.setUser(user);
		entityManager.persist(skill);

		entityManager.getTransaction().commit();
		entityManager.close();

		Long userId = user.getId();

		// When
		User charlie = findUserByIdUsingEntityGraph(userId);

		// Then
		assertNotNull(charlie);
		assertEquals("Charlie", charlie.getName());

		assertNotNull(charlie.getDetail());
		assertTrue(charlie.getDetail().getActive());
		assertEquals("Paris", charlie.getDetail().getCity());

		assertTrue(charlie.getSkills().isEmpty());
	}

	@Test
	public void findUserByNameWithActiveDetailAndDeletedSkillTest() {
		// Given
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		entityManager.getTransaction().begin();

		User user = new User();
		user.setName("Charlie");
		entityManager.persist(user);

		UserDetail detail = new UserDetail();
		detail.setCity("Paris");
		detail.setActive(true);
		detail.setUser(user);
		entityManager.persist(detail);

		UserSkill skill = new UserSkill();
		skill.setSkillName("Java");
		skill.setDeleted(true);
		skill.setUser(user);
		entityManager.persist(skill);

		entityManager.getTransaction().commit();
		entityManager.close();

		// When
		User charlie = findUserByNameUsingEntityGraph("Charlie");

		// Then
		assertNotNull(charlie);
		assertEquals("Charlie", charlie.getName());

		assertNotNull(charlie.getDetail());
		assertTrue(charlie.getDetail().getActive());
		assertEquals("Paris", charlie.getDetail().getCity());

		assertTrue(charlie.getSkills().isEmpty());
	}

	@Test
	public void findUserByIdWithMultipleDetailsAndSingleSkillTest() {
		// Given
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		entityManager.getTransaction().begin();

		User user = new User();
		user.setName("David");
		entityManager.persist(user);

		UserDetail detail1 = new UserDetail();
		detail1.setCity("London");
		detail1.setActive(false);
		detail1.setUser(user);
		entityManager.persist(detail1);

		UserDetail detail2 = new UserDetail();
		detail2.setCity("Rome");
		detail2.setActive(true);
		detail2.setUser(user);
		entityManager.persist(detail2);

		UserSkill skill = new UserSkill();
		skill.setSkillName("Kotlin");
		skill.setDeleted(false);
		skill.setUser(user);
		entityManager.persist(skill);

		entityManager.getTransaction().commit();
		entityManager.close();

		Long userId = user.getId();

		// When
		User david = findUserByIdUsingEntityGraph(userId);

		// Then
		assertNotNull(david);
		assertEquals("David", david.getName());

		assertNotNull(david.getDetail());
		assertTrue(david.getDetail().getActive());
		assertEquals("Rome", david.getDetail().getCity());

		assertFalse(david.getSkills().isEmpty());
		assertTrue(david.getSkills().stream().noneMatch(UserSkill::getDeleted));
		assertEquals(1, david.getSkills().size());
		assertEquals("Kotlin", david.getSkills().stream().findFirst().orElseThrow(IllegalStateException::new).getSkillName());
	}

	@Test
	public void findUserByNameWithMultipleDetailsAndSingleSkillTest() {
		// Given
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		entityManager.getTransaction().begin();

		User user = new User();
		user.setName("David");
		entityManager.persist(user);

		UserDetail detail1 = new UserDetail();
		detail1.setCity("London");
		detail1.setActive(false);
		detail1.setUser(user);
		entityManager.persist(detail1);

		UserDetail detail2 = new UserDetail();
		detail2.setCity("Rome");
		detail2.setActive(true);
		detail2.setUser(user);
		entityManager.persist(detail2);

		UserSkill skill = new UserSkill();
		skill.setSkillName("Kotlin");
		skill.setDeleted(false);
		skill.setUser(user);
		entityManager.persist(skill);

		entityManager.getTransaction().commit();
		entityManager.close();

		// When
		User david = findUserByNameUsingEntityGraph("David");

		// Then
		assertNotNull(david);
		assertEquals("David", david.getName());

		assertNotNull(david.getDetail());
		assertTrue(david.getDetail().getActive());
		assertEquals("Rome", david.getDetail().getCity());

		assertFalse(david.getSkills().isEmpty());
		assertTrue(david.getSkills().stream().noneMatch(UserSkill::getDeleted));
		assertEquals(1, david.getSkills().size());
		assertEquals("Kotlin", david.getSkills().stream().findFirst().orElseThrow(IllegalStateException::new).getSkillName());
	}

	@Test
	public void findUserByIdWithSingleDetailAndMultipleSkillsTest() {
		// Given
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		entityManager.getTransaction().begin();

		User user = new User();
		user.setName("Frank");
		entityManager.persist(user);

		UserDetail detail = new UserDetail();
		detail.setCity("Madrid");
		detail.setActive(true);
		detail.setUser(user);
		entityManager.persist(detail);

		UserSkill skill1 = new UserSkill();
		skill1.setSkillName("Rust");
		skill1.setDeleted(true);
		skill1.setUser(user);
		entityManager.persist(skill1);

		UserSkill skill2 = new UserSkill();
		skill2.setSkillName("Erlang");
		skill2.setDeleted(false);
		skill2.setUser(user);
		entityManager.persist(skill2);

		UserSkill skill3 = new UserSkill();
		skill3.setSkillName("Go");
		skill3.setDeleted(false);
		skill3.setUser(user);
		entityManager.persist(skill3);

		UserSkill skill4 = new UserSkill();
		skill4.setSkillName("C");
		skill4.setDeleted(true);
		skill4.setUser(user);
		entityManager.persist(skill4);

		entityManager.getTransaction().commit();
		entityManager.close();

		Long userId = user.getId();

		// When
		User frank = findUserByIdUsingEntityGraph(userId);

		// Then
		assertNotNull(frank);
		assertEquals("Frank", frank.getName());

		assertNotNull(frank.getDetail());
		assertTrue(frank.getDetail().getActive());
		assertEquals("Madrid", frank.getDetail().getCity());

		assertFalse(frank.getSkills().isEmpty());
		assertTrue(frank.getSkills().stream().noneMatch(UserSkill::getDeleted));
		assertEquals(2, frank.getSkills().size());
	}

	@Test
	public void findUserByNameWithSingleDetailAndMultipleSkillsTest() {
		// Given
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		entityManager.getTransaction().begin();

		User user = new User();
		user.setName("Frank");
		entityManager.persist(user);

		UserDetail detail = new UserDetail();
		detail.setCity("Madrid");
		detail.setActive(true);
		detail.setUser(user);
		entityManager.persist(detail);

		UserSkill skill1 = new UserSkill();
		skill1.setSkillName("Rust");
		skill1.setDeleted(true);
		skill1.setUser(user);
		entityManager.persist(skill1);

		UserSkill skill2 = new UserSkill();
		skill2.setSkillName("Erlang");
		skill2.setDeleted(false);
		skill2.setUser(user);
		entityManager.persist(skill2);

		UserSkill skill3 = new UserSkill();
		skill3.setSkillName("Go");
		skill3.setDeleted(false);
		skill3.setUser(user);
		entityManager.persist(skill3);

		UserSkill skill4 = new UserSkill();
		skill4.setSkillName("C");
		skill4.setDeleted(true);
		skill4.setUser(user);
		entityManager.persist(skill4);

		entityManager.getTransaction().commit();
		entityManager.close();

		// When
		User frank = findUserByNameUsingEntityGraph("Frank");

		// Then
		assertNotNull(frank);
		assertEquals("Frank", frank.getName());

		assertNotNull(frank.getDetail());
		assertTrue(frank.getDetail().getActive());
		assertEquals("Madrid", frank.getDetail().getCity());

		assertFalse(frank.getSkills().isEmpty());
		assertTrue(frank.getSkills().stream().noneMatch(UserSkill::getDeleted));
		assertEquals(2, frank.getSkills().size());
	}

	@Test
	public void findUserByIdWithMultipleDetailsAndMultipleSkillsTest1() {
		// Given
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		entityManager.getTransaction().begin();

		User user = new User();
		user.setName("Eve");
		entityManager.persist(user);

		UserDetail detail1 = new UserDetail();
		detail1.setCity("Moscow");
		detail1.setActive(false);
		detail1.setUser(user);
		entityManager.persist(detail1);

		UserDetail detail2 = new UserDetail();
		detail2.setCity("Istanbul");
		detail2.setActive(false);
		detail2.setUser(user);
		entityManager.persist(detail2);

		UserDetail detail3 = new UserDetail();
		detail3.setCity("Berlin");
		detail3.setActive(true);
		detail3.setUser(user);
		entityManager.persist(detail3);

		UserSkill skill1 = new UserSkill();
		skill1.setSkillName("Python");
		skill1.setDeleted(true);
		skill1.setUser(user);
		entityManager.persist(skill1);

		UserSkill skill2 = new UserSkill();
		skill2.setSkillName("Ruby");
		skill2.setDeleted(false);
		skill2.setUser(user);
		entityManager.persist(skill2);

		entityManager.getTransaction().commit();
		entityManager.close();

		Long userId = user.getId();

		// When
		User eve = findUserByIdUsingEntityGraph(userId);

		// Then
		assertNotNull(eve);
		assertEquals("Eve", eve.getName());

		assertNotNull(eve.getDetail());
		assertTrue(eve.getDetail().getActive());
		assertEquals("Berlin", eve.getDetail().getCity());

		assertFalse(eve.getSkills().isEmpty());
		assertTrue(eve.getSkills().stream().noneMatch(UserSkill::getDeleted));
		assertEquals(1, eve.getSkills().size());
		assertEquals("Ruby", eve.getSkills().stream().findFirst().orElseThrow(IllegalStateException::new).getSkillName());
	}

	@Test
	public void findUserByNameWithMultipleDetailsAndMultipleSkillsTest1() {
		// Given
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		entityManager.getTransaction().begin();

		User user = new User();
		user.setName("Eve");
		entityManager.persist(user);

		UserDetail detail1 = new UserDetail();
		detail1.setCity("Moscow");
		detail1.setActive(false);
		detail1.setUser(user);
		entityManager.persist(detail1);

		UserDetail detail2 = new UserDetail();
		detail2.setCity("Istanbul");
		detail2.setActive(false);
		detail2.setUser(user);
		entityManager.persist(detail2);

		UserDetail detail3 = new UserDetail();
		detail3.setCity("Berlin");
		detail3.setActive(true);
		detail3.setUser(user);
		entityManager.persist(detail3);

		UserSkill skill1 = new UserSkill();
		skill1.setSkillName("Python");
		skill1.setDeleted(true);
		skill1.setUser(user);
		entityManager.persist(skill1);

		UserSkill skill2 = new UserSkill();
		skill2.setSkillName("Ruby");
		skill2.setDeleted(false);
		skill2.setUser(user);
		entityManager.persist(skill2);

		entityManager.getTransaction().commit();
		entityManager.close();

		// When
		User eve = findUserByNameUsingEntityGraph("Eve");

		// Then
		assertNotNull(eve);
		assertEquals("Eve", eve.getName());

		assertNotNull(eve.getDetail());
		assertTrue(eve.getDetail().getActive());
		assertEquals("Berlin", eve.getDetail().getCity());

		assertFalse(eve.getSkills().isEmpty());
		assertTrue(eve.getSkills().stream().noneMatch(UserSkill::getDeleted));
		assertEquals(1, eve.getSkills().size());
		assertEquals("Ruby", eve.getSkills().stream().findFirst().orElseThrow(IllegalStateException::new).getSkillName());
	}

	@Test
	public void findUserByIdWithMultipleDetailsAndMultipleSkillsTest2() {
		// Given
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		entityManager.getTransaction().begin();

		User user = new User();
		user.setName("Grace");
		entityManager.persist(user);

		UserDetail detail1 = new UserDetail();
		detail1.setCity("Vienna");
		detail1.setActive(false);
		detail1.setUser(user);
		entityManager.persist(detail1);

		UserDetail detail2 = new UserDetail();
		detail2.setCity("Barcelona");
		detail2.setActive(true);
		detail2.setUser(user);
		entityManager.persist(detail2);

		UserSkill skill1 = new UserSkill();
		skill1.setSkillName("PHP");
		skill1.setDeleted(false);
		skill1.setUser(user);
		entityManager.persist(skill1);

		UserSkill skill2 = new UserSkill();
		skill2.setSkillName("Swift");
		skill2.setDeleted(false);
		skill2.setUser(user);
		entityManager.persist(skill2);

		UserSkill skill3 = new UserSkill();
		skill3.setSkillName("Dart");
		skill3.setDeleted(false);
		skill3.setUser(user);
		entityManager.persist(skill3);

		UserSkill skill4 = new UserSkill();
		skill4.setSkillName("Scala");
		skill4.setDeleted(false);
		skill4.setUser(user);
		entityManager.persist(skill4);

		entityManager.getTransaction().commit();
		entityManager.close();

		Long userId = user.getId();

		// When
		User grace = findUserByIdUsingEntityGraph(userId);

		// Then
		assertNotNull(grace);
		assertEquals("Grace", grace.getName());

		assertNotNull(grace.getDetail());
		assertTrue(grace.getDetail().getActive());
		assertEquals("Barcelona", grace.getDetail().getCity());

		assertFalse(grace.getSkills().isEmpty());
		assertTrue(grace.getSkills().stream().noneMatch(UserSkill::getDeleted));
		assertEquals(4, grace.getSkills().size());
	}

	@Test
	public void findUserByNameWithMultipleDetailsAndMultipleSkillsTest2() {
		// Given
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		entityManager.getTransaction().begin();

		User user = new User();
		user.setName("Grace");
		entityManager.persist(user);

		UserDetail detail1 = new UserDetail();
		detail1.setCity("Vienna");
		detail1.setActive(false);
		detail1.setUser(user);
		entityManager.persist(detail1);

		UserDetail detail2 = new UserDetail();
		detail2.setCity("Barcelona");
		detail2.setActive(true);
		detail2.setUser(user);
		entityManager.persist(detail2);

		UserSkill skill1 = new UserSkill();
		skill1.setSkillName("PHP");
		skill1.setDeleted(false);
		skill1.setUser(user);
		entityManager.persist(skill1);

		UserSkill skill2 = new UserSkill();
		skill2.setSkillName("Swift");
		skill2.setDeleted(false);
		skill2.setUser(user);
		entityManager.persist(skill2);

		UserSkill skill3 = new UserSkill();
		skill3.setSkillName("Dart");
		skill3.setDeleted(false);
		skill3.setUser(user);
		entityManager.persist(skill3);

		UserSkill skill4 = new UserSkill();
		skill4.setSkillName("Scala");
		skill4.setDeleted(false);
		skill4.setUser(user);
		entityManager.persist(skill4);

		entityManager.getTransaction().commit();
		entityManager.close();

		// When
		User grace = findUserByNameUsingEntityGraph("Grace");

		// Then
		assertNotNull(grace);
		assertEquals("Grace", grace.getName());

		assertNotNull(grace.getDetail());
		assertTrue(grace.getDetail().getActive());
		assertEquals("Barcelona", grace.getDetail().getCity());

		assertFalse(grace.getSkills().isEmpty());
		assertTrue(grace.getSkills().stream().noneMatch(UserSkill::getDeleted));
		assertEquals(4, grace.getSkills().size());
	}
}
