package com.example.demo.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.example.demo.loginCredentials.AdminLogin;
import com.example.demo.loginCredentials.UserLogin;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

class EntityAndLoginModelTest {

	@Test
	void userStoresAllPropertiesAndOrders() {
		User user = new User("user@example.com", "secret");
		Date orderDate = new Date();
		Orders order = new Orders();
		order.setOrderDate(orderDate);

		user.setU_id(8);
		user.setUname("Sam");
		user.setUnumber(1234567890L);
		user.setOrders(List.of(order));

		assertEquals(8, user.getU_id());
		assertEquals("Sam", user.getUname());
		assertEquals("user@example.com", user.getUemail());
		assertEquals("secret", user.getUpassword());
		assertEquals(1234567890L, user.getUnumber());
		assertSame(order, user.getOrders().get(0));
	}

	@Test
	void productStoresAllProperties() {
		Product product = new Product();
		product.setPid(4);
		product.setPname("Noodles");
		product.setPprice(8.75);
		product.setPdescription("Spicy noodles");

		assertEquals(4, product.getPid());
		assertEquals("Noodles", product.getPname());
		assertEquals(8.75, product.getPprice());
		assertEquals("Spicy noodles", product.getPdescription());
	}

	@Test
	void orderStoresAllPropertiesAndUser() {
		Orders order = new Orders();
		User user = new User();
		Date orderDate = new Date();
		order.setoId(11);
		order.setoName("Soup");
		order.setoPrice(6.25);
		order.setoQuantity(2);
		order.setOrderDate(orderDate);
		order.setTotalAmmout(12.5);
		order.setUser(user);

		assertEquals(11, order.getoId());
		assertEquals("Soup", order.getoName());
		assertEquals(6.25, order.getoPrice());
		assertEquals(2, order.getoQuantity());
		assertSame(orderDate, order.getOrderDate());
		assertEquals(12.5, order.getTotalAmmout());
		assertSame(user, order.getUser());
	}

	@Test
	void adminStoresAllProperties() {
		Admin admin = new Admin();
		admin.setAdminId(3);
		admin.setAdminName("Ari");
		admin.setAdminEmail("ari@example.com");
		admin.setAdminPassword("secret");
		admin.setAdminNumber("1234567890");

		assertEquals(3, admin.getAdminId());
		assertEquals("Ari", admin.getAdminName());
		assertEquals("ari@example.com", admin.getAdminEmail());
		assertEquals("secret", admin.getAdminPassword());
		assertEquals("1234567890", admin.getAdminNumber());
	}

	@Test
	void loginModelsStoreSubmittedCredentials() {
		AdminLogin adminLogin = new AdminLogin();
		adminLogin.setEmail("admin@example.com");
		adminLogin.setPassword("admin-secret");
		UserLogin userLogin = new UserLogin();
		userLogin.setUserEmail("user@example.com");
		userLogin.setUserPassword("user-secret");

		assertEquals("admin@example.com", adminLogin.getEmail());
		assertEquals("admin-secret", adminLogin.getPassword());
		assertEquals("user@example.com", userLogin.getUserEmail());
		assertEquals("user-secret", userLogin.getUserPassword());
	}

	@Test
	void adminValidationRequiresNameAndValidEmail() {
		try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
			Validator validator = factory.getValidator();
			Admin validAdmin = new Admin();
			validAdmin.setAdminName("Casey");
			validAdmin.setAdminEmail("casey@example.com");
			assertEquals(Set.of(), validator.validate(validAdmin));

			Admin invalidAdmin = new Admin();
			invalidAdmin.setAdminEmail("not-an-email");
			assertEquals(Set.of("adminName", "adminEmail"),
					validator.validate(invalidAdmin).stream()
							.map(violation -> violation.getPropertyPath().toString())
							.collect(java.util.stream.Collectors.toSet()));
		}
	}
}
