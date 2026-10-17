package com.starterkit.springboot.brs.service;

import com.starterkit.springboot.brs.dto.model.user.UserDto;
import com.starterkit.springboot.brs.model.user.Role;
import com.starterkit.springboot.brs.model.user.User;
import com.starterkit.springboot.brs.model.user.UserRoles;
import com.starterkit.springboot.brs.repository.user.RoleRepository;
import com.starterkit.springboot.brs.repository.user.UserRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.modelmapper.ModelMapper;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Collections;
import java.util.HashSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UserServiceBehaviorTest {

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BusReservationService busReservationService;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    public void signupShouldEncodePasswordAndAssignPassengerRole() {
        UserDto request = new UserDto()
                .setEmail("passenger@example.com")
                .setPassword("plain-secret")
                .setFirstName("Pat")
                .setLastName("Passenger")
                .setMobileNumber("1234567890");
        Role passengerRole = new Role().setRole(UserRoles.PASSENGER);
        when(userRepository.findByEmail(request.getEmail())).thenReturn(null);
        when(roleRepository.findByRole(UserRoles.PASSENGER)).thenReturn(passengerRole);
        when(passwordEncoder.encode("plain-secret")).thenReturn("encoded-secret");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserDto result = userService.signup(request);

        assertEquals(request.getEmail(), result.getEmail());
        assertEquals("Pat", result.getFirstName());
        assertEquals(1, result.getRoles().size());
        assertNull(result.getPassword());
        verify(passwordEncoder).encode("plain-secret");
        verify(roleRepository).findByRole(UserRoles.PASSENGER);
    }

    @Test
    public void signupShouldAssignAdminRoleWhenRequested() {
        UserDto request = new UserDto()
                .setEmail("admin@example.com")
                .setPassword("admin-secret")
                .setFirstName("Ada")
                .setLastName("Admin");
        request.setAdmin(true);
        Role adminRole = new Role().setRole(UserRoles.ADMIN);
        when(userRepository.findByEmail(request.getEmail())).thenReturn(null);
        when(roleRepository.findByRole(UserRoles.ADMIN)).thenReturn(adminRole);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded-admin-secret");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserDto result = userService.signup(request);

        assertEquals(request.getEmail(), result.getEmail());
        assertEquals(1, result.getRoles().size());
        verify(roleRepository).findByRole(UserRoles.ADMIN);
    }

    @Test
    public void findUserByEmailShouldReturnMappedUser() {
        User user = new User()
                .setEmail("reader@example.com")
                .setFirstName("Rae")
                .setLastName("Reader")
                .setRoles(new HashSet<>(Collections.singletonList(new Role().setRole(UserRoles.PASSENGER))));
        UserDto expected = new UserDto()
                .setEmail("reader@example.com")
                .setFirstName("Rae")
                .setLastName("Reader");
        when(userRepository.findByEmail(user.getEmail())).thenReturn(user);
        when(modelMapper.map(user, UserDto.class)).thenReturn(expected);

        UserDto result = userService.findUserByEmail(user.getEmail());

        assertEquals(expected.getEmail(), result.getEmail());
        assertEquals(expected.getFullName(), result.getFullName());
    }

    @Test
    public void updateProfileShouldPersistOnlyProfileFields() {
        User user = new User()
                .setEmail("profile@example.com")
                .setPassword("unchanged")
                .setFirstName("Old")
                .setLastName("Name")
                .setMobileNumber("1111111111")
                .setRoles(new HashSet<>());
        UserDto update = new UserDto()
                .setEmail(user.getEmail())
                .setFirstName("New")
                .setLastName("Name")
                .setMobileNumber("2222222222");
        when(userRepository.findByEmail(user.getEmail())).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);

        UserDto result = userService.updateProfile(update);

        assertEquals("New", result.getFirstName());
        assertEquals("2222222222", result.getMobileNumber());
        assertEquals("unchanged", user.getPassword());
        verify(userRepository).save(user);
    }

    @Test
    public void changePasswordShouldPersistEncodedPassword() {
        User user = new User()
                .setEmail("password@example.com")
                .setPassword("old-encoded")
                .setRoles(new HashSet<>());
        when(userRepository.findByEmail(user.getEmail())).thenReturn(user);
        when(passwordEncoder.encode("new-secret")).thenReturn("new-encoded");
        when(userRepository.save(user)).thenReturn(user);

        userService.changePassword(new UserDto().setEmail(user.getEmail()), "new-secret");

        assertEquals("new-encoded", user.getPassword());
        verify(passwordEncoder).encode("new-secret");
        verify(userRepository).save(user);
    }
}
