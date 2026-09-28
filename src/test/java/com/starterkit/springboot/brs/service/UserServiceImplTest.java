package com.starterkit.springboot.brs.service;

import com.starterkit.springboot.brs.dto.model.user.UserDto;
import com.starterkit.springboot.brs.model.user.Role;
import com.starterkit.springboot.brs.model.user.User;
import com.starterkit.springboot.brs.model.user.UserRoles;
import com.starterkit.springboot.brs.repository.user.RoleRepository;
import com.starterkit.springboot.brs.repository.user.UserRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.modelmapper.ModelMapper;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Arrays;
import java.util.HashSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UserServiceImplTest {

    @Mock
    private BCryptPasswordEncoder bCryptPasswordEncoder;

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

    @Before
    public void setUp() {
        when(bCryptPasswordEncoder.encode(anyString())).thenAnswer(invocation -> "encoded:" + invocation.getArgument(0));
    }

    @Test
    public void signupShouldCreatePassengerUserWhenEmailIsNew() {
        UserDto request = new UserDto()
                .setEmail("alice@example.com")
                .setPassword("secret")
                .setFirstName("Alice")
                .setLastName("Jones")
                .setMobileNumber("9999999999");

        Role passengerRole = new Role().setRole(UserRoles.PASSENGER);
        when(userRepository.findByEmail("alice@example.com")).thenReturn(null);
        when(roleRepository.findByRole(UserRoles.PASSENGER)).thenReturn(passengerRole);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(10L);
            return user;
        });

        UserDto response = userService.signup(request);

        assertNotNull(response);
        assertEquals("alice@example.com", response.getEmail());
        assertEquals("Alice", response.getFirstName());
        assertEquals("Jones", response.getLastName());
        assertEquals("9999999999", response.getMobileNumber());
        verify(userRepository).save(any(User.class));
    }

    @Test
    public void findUserByEmailShouldReturnMappedUserDto() {
        User storedUser = new User()
                .setId(2L)
                .setEmail("bob@example.com")
                .setPassword("encoded:pass")
                .setFirstName("Bob")
                .setLastName("Smith")
                .setMobileNumber("1234567890")
                .setRoles(new HashSet<>(Arrays.asList(new Role().setRole(UserRoles.PASSENGER))));

        UserDto expected = new UserDto()
                .setEmail("bob@example.com")
                .setFirstName("Bob")
                .setLastName("Smith")
                .setMobileNumber("1234567890");

        when(userRepository.findByEmail("bob@example.com")).thenReturn(storedUser);
        when(modelMapper.map(storedUser, UserDto.class)).thenReturn(expected);

        UserDto result = userService.findUserByEmail("bob@example.com");

        assertEquals("bob@example.com", result.getEmail());
        assertEquals("Bob", result.getFirstName());
        assertEquals("Smith", result.getLastName());
    }

    @Test
    public void updateProfileShouldPersistUpdatedCustomerData() {
        User existingUser = new User()
                .setId(3L)
                .setEmail("carol@example.com")
                .setPassword("encoded:old")
                .setFirstName("Carol")
                .setLastName("Brown")
                .setMobileNumber("3333333333")
                .setRoles(new HashSet<>());

        UserDto profileUpdate = new UserDto()
                .setEmail("carol@example.com")
                .setFirstName("Carol")
                .setLastName("Green")
                .setMobileNumber("4444444444");

        when(userRepository.findByEmail("carol@example.com")).thenReturn(existingUser);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserDto result = userService.updateProfile(profileUpdate);

        assertEquals("Green", result.getLastName());
        assertEquals("4444444444", result.getMobileNumber());
        verify(userRepository).save(existingUser);
    }

    @Test
    public void changePasswordShouldEncodeNewPassword() {
        User existingUser = new User()
                .setId(4L)
                .setEmail("dave@example.com")
                .setPassword("oldPassword")
                .setFirstName("Dave")
                .setLastName("Scott")
                .setMobileNumber("5555555555")
                .setRoles(new HashSet<>());

        UserDto request = new UserDto().setEmail("dave@example.com");
        when(userRepository.findByEmail("dave@example.com")).thenReturn(existingUser);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        userService.changePassword(request, "newSecret");

        assertEquals("encoded:newSecret", existingUser.getPassword());
        verify(bCryptPasswordEncoder).encode("newSecret");
    }
}
