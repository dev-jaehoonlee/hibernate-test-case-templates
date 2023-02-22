package org.hibernate.bugs;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.hibernate.entity.User;
import org.hibernate.entity.UserDetail;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

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
        entityManagerFactory = Persistence.createEntityManagerFactory("templatePU");
    }

    @After
    public void destroy() {
        entityManagerFactory.close();
    }

    @Test
    public void hhh16019() {
        // Given
        Long userId = saveSampleUserAndGetId();

        // When
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();

        Map<String, Object> properties = new HashMap<>();
        properties.put("jakarta.persistence.fetchgraph", entityManager.getEntityGraph("user-entity-graph"));

        User foundUser = entityManager.find(User.class, userId, properties);

        entityManager.getTransaction().commit();
        entityManager.close();

        // Then
        assertNotNull(foundUser);
        assertEquals("David", foundUser.getName());

        assertNotNull(foundUser.getDetail());
        assertTrue(foundUser.getDetail().getActive());
        assertEquals("Rome", foundUser.getDetail().getCity());
    }

    private Long saveSampleUserAndGetId() {
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

        entityManager.getTransaction().commit();
        entityManager.close();

        return user.getId();
    }
}
