package com.example.demo.repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import com.example.demo.entities.Admin;
import com.example.demo.entities.Orders;
import com.example.demo.entities.Product;
import com.example.demo.entities.User;

@DataJpaTest(properties = {
		"spring.datasource.url=jdbc:h2:mem:foodfrenzy-repositories;MODE=MySQL;NON_KEYWORDS=USER;DB_CLOSE_DELAY=-1",
		"spring.datasource.driver-class-name=org.h2.Driver",
		"spring.datasource.username=sa",
		"spring.datasource.password=",
		"spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class RepositoryIntegrationTest {

	@Autowired
	private UserRepository userRepository;
	@Autowired
	private ProductRepository productRepository;
	@Autowired
	private AdminRepository adminRepository;
	@Autowired
	private OrderRepository orderRepository;
	@Autowired
	private TestEntityManager entityManager;

	@Test
	void userRepositoryPersistsAndFindsByEmail() {
		User user = new User("user@example.com", "secret");
		user.setUname("Jordan");

		User saved = userRepository.save(user);
		entityManager.flush();

		assertTrue(saved.getU_id() > 0);
		assertEquals(saved.getU_id(), userRepository.findUserByUemail("user@example.com").getU_id());
	}

	@Test
	void productRepositoryPersistsAndFindsByName() {
		Product product = new Product();
		product.setPname("Pasta");
		product.setPprice(9.5);

		Product saved = productRepository.save(product);
		entityManager.flush();

		assertTrue(saved.getPid() > 0);
		assertEquals(saved.getPid(), productRepository.findByPname("Pasta").getPid());
	}

	@Test
	void adminRepositoryPersistsAndFindsByEmail() {
		Admin admin = new Admin();
		admin.setAdminName("Morgan");
		admin.setAdminEmail("admin@example.com");
		admin.setAdminPassword("secret");

		Admin saved = adminRepository.save(admin);
		entityManager.flush();

		assertTrue(saved.getAdminId() > 0);
		assertEquals(saved.getAdminId(), adminRepository.findByAdminEmail("admin@example.com").getAdminId());
	}

	@Test
	void orderRepositoryFindsOrdersForUser() {
		User user = new User("user@example.com", "secret");
		userRepository.save(user);
		entityManager.flush();
		Orders order = new Orders();
		order.setoName("Salad");
		order.setUser(user);
		orderRepository.saveAndFlush(order);

		List<Orders> found = orderRepository.findOrdersByUser(user);

		assertEquals(1, found.size());
		assertEquals("Salad", found.get(0).getoName());
		assertEquals(user.getU_id(), found.get(0).getUser().getU_id());
	}
}
