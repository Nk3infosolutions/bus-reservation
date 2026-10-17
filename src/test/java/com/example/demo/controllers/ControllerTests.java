package com.example.demo.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;

import com.example.demo.entities.Admin;
import com.example.demo.entities.Orders;
import com.example.demo.entities.Product;
import com.example.demo.entities.User;
import com.example.demo.loginCredentials.AdminLogin;
import com.example.demo.loginCredentials.UserLogin;
import com.example.demo.services.AdminServices;
import com.example.demo.services.OrderServices;
import com.example.demo.services.ProductServices;
import com.example.demo.services.UserServices;

@ExtendWith(MockitoExtension.class)
class ControllerTests {

	@Mock
	private UserServices userServices;
	@Mock
	private ProductServices productServices;
	@Mock
	private AdminServices adminServices;
	@Mock
	private OrderServices orderServices;
	@Mock
	private Model model;

	@InjectMocks
	private HomeController homeController;
	@InjectMocks
	private UserController userController;
	@InjectMocks
	private ProductController productController;
	@InjectMocks
	private AdminController adminController;

	@Test
	void homeControllerReturnsExpectedStaticViews() {
		assertEquals("Home", homeController.home());
		assertEquals("Locate_us", homeController.location());
		assertEquals("About", homeController.about());
	}

	@Test
	void homeControllerLoadsProductsIntoModel() {
		List<Product> products = List.of(new Product());
		when(productServices.getAllProducts()).thenReturn(products);

		assertEquals("Products", homeController.products(model));
		verify(model).addAttribute("products", products);
	}

	@Test
	void homeControllerInitializesAdminLoginModel() {
		assertEquals("Login", homeController.login(model));
		verify(model).addAttribute(eq("adminLogin"), any(AdminLogin.class));
	}

	@Test
	void userControllerAddsUpdatesAndDeletes() {
		User user = new User();

		assertEquals("redirect:/admin/services", userController.addUser(user));
		assertEquals("redirect:/admin/services", userController.updateUser(user, 4));
		assertEquals("redirect:/admin/services", userController.deleteUser(4));

		verify(userServices).addUser(user);
		verify(userServices).updateUser(user, 4);
		verify(userServices).deleteUser(4);
	}

	@Test
	void productControllerAddsUpdatesAndDeletes() {
		Product product = new Product();

		assertEquals("redirect:/admin/services", productController.addProduct(product));
		assertEquals("redirect:/admin/services", productController.updateProduct(product, 6));
		assertEquals("redirect:/admin/services", productController.delete(6));

		verify(productServices).addProduct(product);
		verify(productServices).updateproduct(product, 6);
		verify(productServices).deleteProduct(6);
	}

	@Test
	void adminLoginRedirectsForValidCredentialsAndShowsErrorOtherwise() {
		AdminLogin login = new AdminLogin();
		login.setEmail("admin@example.com");
		login.setPassword("correct");
		when(adminServices.validateAdminCredentials("admin@example.com", "correct")).thenReturn(true);

		assertEquals("redirect:/admin/services", adminController.getAllData(login, model));

		login.setPassword("wrong");
		when(adminServices.validateAdminCredentials("admin@example.com", "wrong")).thenReturn(false);
		assertEquals("Login", adminController.getAllData(login, model));
		verify(model).addAttribute("error", "Invalid email or password");
	}

	@Test
	void userLoginLoadsOrdersAndNameForValidCredentials() {
		UserLogin login = new UserLogin();
		login.setUserEmail("user@example.com");
		login.setUserPassword("correct");
		User user = new User("user@example.com", "correct");
		user.setUname("Taylor");
		List<Orders> orders = List.of(new Orders());
		when(userServices.validateLoginCredentials("user@example.com", "correct")).thenReturn(true);
		when(userServices.getUserByEmail("user@example.com")).thenReturn(user);
		when(orderServices.getOrdersForUser(user)).thenReturn(orders);

		assertEquals("BuyProduct", adminController.userLogin(login, model));
		verify(model).addAttribute("orders", orders);
		verify(model).addAttribute("name", "Taylor");
	}

	@Test
	void userLoginShowsErrorForInvalidCredentials() {
		UserLogin login = new UserLogin();
		login.setUserEmail("user@example.com");
		login.setUserPassword("wrong");
		when(userServices.validateLoginCredentials("user@example.com", "wrong")).thenReturn(false);

		assertEquals("Login", adminController.userLogin(login, model));
		verify(model).addAttribute("error2", "Invalid email or password");
	}

	@Test
	void productSearchDisplaysFoundProductAndCurrentOrders() {
		Product product = new Product();
		List<Orders> orders = List.of(new Orders());
		when(productServices.getProductByName("Pizza")).thenReturn(product);
		when(orderServices.getOrdersForUser(null)).thenReturn(orders);

		assertEquals("BuyProduct", adminController.seachHandler("Pizza", model));
		verify(model).addAttribute("orders", orders);
		verify(model).addAttribute("product", product);
	}

	@Test
	void productSearchDisplaysUnavailableMessageWhenProductIsMissing() {
		List<Orders> orders = List.of();
		when(productServices.getProductByName("Missing")).thenReturn(null);
		when(orderServices.getOrdersForUser(null)).thenReturn(orders);

		assertEquals("BuyProduct", adminController.seachHandler("Missing", model));
		verify(model).addAttribute("message", "SORRY...!  Product Unavailable");
		verify(model).addAttribute("product", null);
		verify(model).addAttribute("orders", orders);
	}

	@Test
	void adminDashboardLoadsUsersAdminsProductsAndOrders() {
		List<User> users = List.of(new User());
		List<Admin> admins = List.of(new Admin());
		List<Product> products = List.of(new Product());
		List<Orders> orders = List.of(new Orders());
		when(userServices.getAllUser()).thenReturn(users);
		when(adminServices.getAll()).thenReturn(admins);
		when(productServices.getAllProducts()).thenReturn(products);
		when(orderServices.getOrders()).thenReturn(orders);

		assertEquals("Admin_Page", adminController.returnBack(model));
		verify(model).addAttribute("users", users);
		verify(model).addAttribute("admins", admins);
		verify(model).addAttribute("products", products);
		verify(model).addAttribute("orders", orders);
	}

	@Test
	void adminControllerReturnsAddAndUpdateViews() {
		assertEquals("Add_Admin", adminController.addAdminPage());
		assertEquals("Add_Product", adminController.addProduct());
		assertEquals("Add_User", adminController.addUser());

		Admin admin = new Admin();
		Product product = new Product();
		User user = new User();
		when(adminServices.getAdmin(1)).thenReturn(admin);
		when(productServices.getProduct(2)).thenReturn(product);
		when(userServices.getUser(3)).thenReturn(user);
		assertEquals("Update_Admin", adminController.update(1, model));
		assertEquals("Update_Product", adminController.updateProduct(2, model));
		assertEquals("Update_User", adminController.updateUserPage(3, model));
		verify(model).addAttribute("admin", admin);
		verify(model).addAttribute("product", product);
		verify(model).addAttribute("user", user);
	}

	@Test
	void adminControllerMutationsRedirectToDashboard() {
		Admin admin = new Admin();

		assertEquals("redirect:/admin/services", adminController.addAdmin(admin));
		assertEquals("redirect:/admin/services", adminController.updateAdmin(admin, 4));
		assertEquals("redirect:/admin/services", adminController.deleteAdmin(4));

		verify(adminServices).addAdmin(admin);
		verify(adminServices).update(admin, 4);
		verify(adminServices).delete(4);
	}

	@Test
	void orderHandlerCalculatesAmountAssociatesUserAndSavesOrder() {
		Orders order = new Orders();
		order.setoPrice(7.5);
		order.setoQuantity(4);

		assertEquals("Order_success", adminController.orderHandler(order, model));

		assertEquals(30.0, order.getTotalAmmout());
		assertNull(order.getUser());
		assertTrue(order.getOrderDate() != null);
		verify(orderServices).saveOrder(order);
		verify(model).addAttribute("amount", 30.0);
	}

	@Test
	void backLoadsOrdersForCurrentUser() {
		List<Orders> orders = List.of(new Orders());
		when(orderServices.getOrdersForUser(null)).thenReturn(orders);

		assertEquals("BuyProduct", adminController.back(model));
		verify(model).addAttribute("orders", orders);
	}
}
