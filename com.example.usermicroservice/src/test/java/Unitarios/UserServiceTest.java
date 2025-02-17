package Unitarios;

import com.example.quarkusapi.DTO.Login2FrontDTO;
import com.example.quarkusapi.Exception.ResourceConflictException;
import com.example.quarkusapi.Repositories.UserRepository;
import com.example.quarkusapi.model.Company;
import com.example.quarkusapi.model.User;
import com.example.quarkusapi.model.UserCompany;
import com.example.quarkusapi.service.AuthService;
import com.example.quarkusapi.service.RedisService;
import com.example.quarkusapi.service.UserService;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.redis.client.RedisClient;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;
import java.util.Set;

import com.example.quarkusapi.model.UserCompanyId;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@QuarkusTest
public class UserServiceTest {

    @Inject
    UserService userService;

    @InjectMock
    UserRepository userRepository;

    @InjectMock
    RedisService redisService;

    @InjectMock
    AuthService authService;

    private UserCompany mockUserCompany(long userId, long companyId, String permission) {
        UserCompany userCompany = new UserCompany();
        userCompany.id = new UserCompanyId();
        userCompany.id.userId = userId;
        userCompany.id.companyId = companyId;
        userCompany.user = new User();
        userCompany.user.id = userId;
        userCompany.company = new Company();
        userCompany.company.id = companyId;
        userCompany.permission = permission;
        return userCompany;
    }

    private PanacheQuery<User> mockPanacheQuery(User result) {
        @SuppressWarnings("unchecked")
        PanacheQuery<User> query = mock(PanacheQuery.class);
        when(query.firstResult()).thenReturn(result);
        return query;
    }

    @Test
    void shouldCreateUserSuccessfully() {
        User newUser = new User();
        newUser.username = "testuser";
        newUser.email = "test@test.com";
        newUser.password = "password123";

        PanacheQuery<User> mockQuery = mockPanacheQuery(null);
        when(userRepository.find("username = ?1 or email = ?2", newUser.username, newUser.email))
                .thenReturn(mockQuery);

        assertDoesNotThrow(() -> userService.criarUser(newUser));

        verify(userRepository).persist((User) argThat(user ->
                ((User) user).username.equals(newUser.username) &&
                        ((User) user).email.equals(newUser.email)
        ));
    }

    @Test
    void shouldThrowWhenUsernameDuplicate() {
        User newUser = new User();
        newUser.username = "existinguser";
        newUser.email = "new@test.com";

        User existingUser = new User();
        existingUser.username = "existinguser";
        existingUser.email = "existing@test.com";

        PanacheQuery<User> mockQuery = mockPanacheQuery(existingUser);
        when(userRepository.find("username = ?1 or email = ?2", newUser.username, newUser.email))
                .thenReturn(mockQuery);

        ResourceConflictException exception = assertThrows(ResourceConflictException.class,
                () -> userService.criarUser(newUser));

        assertEquals("Nome de usuario ja existe", exception.getMessage());
        verify(userRepository, never()).persist(any(User.class));
    }

    @Test
    void shouldLoginSuccessfully() {
        User loginRequest = new User();
        loginRequest.username = "testuser";
        loginRequest.password = "password123";

        User dbUser = new User();
        dbUser.username = "testuser";
        dbUser.emailVerified = true;
        dbUser.userCompanies = Set.of(mockUserCompany(1L, 1L, "A"));
        dbUser.setHashPassword(loginRequest.password);

        when(userRepository.findUserWithCompanies(loginRequest.username)).thenReturn(dbUser);

        when(redisService.saveToken(anyString(), anySet())).thenReturn(true);
        when(authService.BruteForceCheck("192.168.0.1", "test-agent")).thenReturn(0);

        Login2FrontDTO data = userService.login(loginRequest, "192.168.0.1", "test-agent");

        assertNotNull(data.getToken());
        verify(redisService).saveToken(anyString(), anySet());
    }

    @Test
    void shouldFailLoginWithInvalidPassword() {
        User loginRequest = new User();
        loginRequest.username = "testuser";
        loginRequest.password = "wrongpassword";

        User dbUser = new User();
        dbUser.username = "testuser";
        dbUser.emailVerified = true;
        dbUser.setHashPassword("correctpassword");

        when(userRepository.findUserWithCompanies(loginRequest.username)).thenReturn(dbUser);


        when(redisService.saveToken(anyString(), anySet())).thenReturn(true);

        assertNull(userService.login(loginRequest, "192.168.0.1", "test-agent"));
    }
}