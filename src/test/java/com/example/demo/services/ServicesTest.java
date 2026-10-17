package com.example.demo.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.demo.entities.Admin;
import com.example.demo.entities.Orders;
import com.example.demo.entities.Product;
import com.example.demo.entities.User;
import com.example.demo.repositories.AdminRepository;
import com.example.demo.repositories.OrderRepository;
import com.example.demo.repositories.ProductRepository;
import com.example.demo.repositories.UserRepository;

@ExtendWith(MockitoExtension.class)
class ServicesTest {

	@Mock
	private UserRepository userRepository;
	@Mock
	private ProductRepository productRepository;
	@Mock
	private AdminRepository adminRepository;
	@Mock
	private OrderRepository orderRepository;

	@InjectMocks
	private UserServices userServices;
	@InjectMocks
	private ProductServices productServices;
	@InjectMocks
	private AdminServices adminServices;
	@InjectMocks
	private OrderServices orderServices;

	@Test
	void userServiceReadsUsersAndLooksUpByEmail() {
		User user = user("u@example.com", "pass");
		when(userRepository.findAll()).thenReturn(List.of(user));
		when(userRepository.findById(2)).thenReturn(Optional.of(user));
		when(userRepository.findUserByUemail("u@example.com")).thenReturn(user);

		assertEquals(List.of(user), userServices.getAllUser());
		assertSame(user, userServices.getUser(2));
		assertSame(user, userServices.getUserByEmail("u@example.com"));
	}

	@Test
	void userServicePropagatesMissingUserLookup() {
		when(userRepository.findById(99)).thenReturn(Optional.empty());

		assertThrows(java.util.NoSuchElementException.class, () -> userServices.getUser(99));
	}

	@Test
	void userServiceAddsUpdatesAndDeletes() {
		User user = user("u@example.com", "pass");

		userServices.addUser(user);
		userServices.updateUser(user, 5);
		userServices.deleteUser(5);

		assertEquals(5, user.getU_id());
		verify(userRepository, times(2)).save(user);
		verify(userRepository).deleteById(5);
	}

	@Test
	void userCredentialsAcceptOnlyMatchingEmailAndPassword() {
		User matching = user("u@example.com", "correct");
		when(userRepository.findAll()).thenReturn(List.of(user("other@example.com", "correct"), matching));
		assertTrue(userServices.validateLoginCredentials("u@example.com", "correct"));

		when(userRepository.findAll()).thenReturn(List.of(matching));
		assertFalse(userServices.validateLoginCredentials("u@example.com", "wrong"));
		assertFalse(userServices.validateLoginCredentials("missing@example.com", "correct"));
	}

	@Test
	void productServiceReadsProductsAndLooksUpByNameOrId() {
		Product product = product(7, "Pizza");
		when(productRepository.findAll()).thenReturn(List.of(product));
		when(productRepository.findById(7)).thenReturn(Optional.of(product));
		when(productRepository.findByPname("Pizza")).thenReturn(product);
		when(productRepository.findByPname("Absent")).thenReturn(null);

		assertEquals(List.of(product), productServices.getAllProducts());
		assertSame(product, productServices.getProduct(7));
		assertSame(product, productServices.getProductByName("Pizza"));
		assertNull(productServices.getProductByName("Absent"));
	}

	@Test
	void productServicePropagatesMissingProductLookup() {
		when(productRepository.findById(44)).thenReturn(Optional.empty());
		assertThrows(java.util.NoSuchElementException.class, () -> productServices.getProduct(44));
	}

	@Test
	void productServiceAddsUpdatesAndDeletes() {
		Product product = product(0, "Pizza");
		Product stored = product(7, "Old Pizza");
		when(productRepository.findById(7)).thenReturn(Optional.of(stored));

		productServices.addProduct(product);
		productServices.updateproduct(product, 7);
		productServices.deleteProduct(7);

		assertEquals(7, product.getPid());
		verify(productRepository, times(2)).save(product);
		verify(productRepository).deleteById(7);
	}

	@Test
	void productServiceDoesNotSaveUpdateWhenIdentifierDoesNotMatch() {
		Product product = product(0, "Pizza");
		when(productRepository.findById(7)).thenReturn(Optional.of(product(8, "Other")));

		productServices.updateproduct(product, 7);

		verify(productRepository, never()).save(product);
	}

	@Test
	void adminServiceReadsAdminsAndPropagatesMissingLookup() {
		Admin admin = admin(3, "a@example.com", "pass");
		when(adminRepository.findAll()).thenReturn(List.of(admin));
		when(adminRepository.findById(3)).thenReturn(Optional.of(admin));
		when(adminRepository.findById(9)).thenReturn(Optional.empty());

		assertEquals(List.of(admin), adminServices.getAll());
		assertSame(admin, adminServices.getAdmin(3));
		assertThrows(java.util.NoSuchElementException.class, () -> adminServices.getAdmin(9));
	}

	@Test
	void adminServiceAddsUpdatesAndDeletesExistingAdmin() {
		Admin admin = admin(3, "a@example.com", "pass");
		Admin replacement = admin(3, "new@example.com", "new-pass");
		when(adminRepository.findAll()).thenReturn(List.of(admin));

		adminServices.addAdmin(admin);
		adminServices.update(replacement, 3);
		adminServices.delete(3);

		verify(adminRepository).save(admin);
		verify(adminRepository).save(replacement);
		verify(adminRepository).deleteById(3);
	}

	@Test
	void adminServiceDoesNotUpdateMissingAdmin() {
		when(adminRepository.findAll()).thenReturn(List.of(admin(3, "a@example.com", "pass")));
		Admin replacement = admin(4, "b@example.com", "pass");

		adminServices.update(replacement, 4);

		verify(adminRepository, never()).save(replacement);
	}

	@Test
	void adminCredentialsRequireMatchingEmailAndPassword() {
		when(adminRepository.findByAdminEmail("a@example.com")).thenReturn(admin(1, "a@example.com", "secret"));
		when(adminRepository.findByAdminEmail("missing@example.com")).thenReturn(null);

		assertTrue(adminServices.validateAdminCredentials("a@example.com", "secret"));
		assertFalse(adminServices.validateAdminCredentials("a@example.com", "wrong"));
		assertFalse(adminServices.validateAdminCredentials("missing@example.com", "secret"));
	}

	@Test
	void orderServiceReadsSavesUpdatesDeletesAndFiltersByUser() {
		User user = user("u@example.com", "pass");
		Orders order = new Orders();
		order.setoId(2);
		when(orderRepository.findAll()).thenReturn(List.of(order));
		when(orderRepository.findOrdersByUser(user)).thenReturn(List.of(order));

		assertEquals(List.of(order), orderServices.getOrders());
		assertEquals(List.of(order), orderServices.getOrdersForUser(user));
		orderServices.saveOrder(order);
		orderServices.updateOrder(2, order);
		orderServices.deleteOrder(2);

		assertEquals(2, order.getoId());
		verify(orderRepository, times(2)).save(order);
		verify(orderRepository).deleteById(2);
	}

	private static User user(String email, String password) {
		User user = new User(email, password);
		user.setUname("Test user");
		return user;
	}

	private static Product product(int id, String name) {
		Product product = new Product();
		product.setPid(id);
		product.setPname(name);
		return product;
	}

	private static Admin admin(int id, String email, String password) {
		Admin admin = new Admin();
		admin.setAdminId(id);
		admin.setAdminEmail(email);
		admin.setAdminPassword(password);
		return admin;
	}
}
