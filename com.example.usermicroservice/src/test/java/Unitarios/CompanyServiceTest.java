package Unitarios;

import com.example.quarkusapi.DTO.CreateUserAdminRequestDTO;
import com.example.quarkusapi.Exception.ResourceConflictException;
import com.example.quarkusapi.Repositories.CompanyRepository;
import com.example.quarkusapi.Repositories.UserCompanyRepository;
import com.example.quarkusapi.Repositories.UserRepository;
import com.example.quarkusapi.model.Company;
import com.example.quarkusapi.model.User;
import com.example.quarkusapi.model.UserCompany;
import com.example.quarkusapi.model.newEmployee;
import com.example.quarkusapi.service.CompanyService;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.PersistenceException;
import jakarta.ws.rs.NotFoundException;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@QuarkusTest
public class CompanyServiceTest {
    @Inject
    CompanyService companyService;

    @InjectMock
    UserRepository userRepository;

    @InjectMock
    CompanyRepository companyRepository;

    @InjectMock
    UserCompanyRepository userCompanyRepository;

    private PanacheQuery<Company> mockCompanyQuery(Company result) {
        @SuppressWarnings("unchecked")
        PanacheQuery<Company> query = mock(PanacheQuery.class);
        when(query.firstResult()).thenReturn(result);
        return query;
    }

    private PanacheQuery<User> mockUserQuery(User result) {
        @SuppressWarnings("unchecked")
        PanacheQuery<User> query = mock(PanacheQuery.class);
        when(query.firstResult()).thenReturn(result);
        return query;
    }

    @Test
    void findUsersByCompanyIdPageable_ShouldReturnUsers() {
        // Arrange
        User user = new User();
        user.id = 1L;
        List<User> users = Collections.singletonList(user);

        when(userCompanyRepository.findUsersByCompanyId(1L)).thenReturn(users);

        // Act
        List<User> result = companyService.findUsersByCompanyIdPageable(1L, 1, 10);

        // Assert
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    @Test
    void findUsersByCompanyIdPageable_ShouldThrowNotFoundException() {
        // Arrange
        when(userCompanyRepository.findUsersByCompanyId(1L)).thenReturn(Collections.emptyList());
        // Act & Assert
        assertThrows(NotFoundException.class,
                () -> companyService.findUsersByCompanyIdPageable(999L, 1, 10));
    }

//    @Test
//    void criarFuncionario_ShouldPersistNewEmployee() {
//        // Arrange
//        newEmployee req = new newEmployee();
//        req.setUsername("newuser");
//        req.setCompany_id(1L);
//        req.setEmail("dddd@gmail.com");
//
//        // Create mock for PanacheQuery
//        PanacheQuery userQuery = mock(PanacheQuery.class);
//        when(userRepository.find(eq("username"), eq("newuser"))).thenReturn(userQuery);
//        when(userQuery.firstResult()).thenReturn(null);
//
//// Create mock for PanacheQuery
//        PanacheQuery companyQuery = mock(PanacheQuery.class);
//        when(companyRepository.find(eq("id"), eq(1L))).thenReturn(companyQuery);
//        when(companyQuery.firstResult()).thenReturn(null);
//
//        // Act
//        assertDoesNotThrow(() -> companyService.CriarFuncionario(req));
//
//        // Assert
//        verify(userRepository).persist(any(User.class));
//        verify(userCompanyRepository).persist(any(UserCompany.class));
//    }

    @Test
    void criarFuncionario_ShouldPersistNewEmployee() {
        // Arrange
        newEmployee req = new newEmployee();
        req.setUsername("newuser");
        req.setCompany_id(1L);

        // 1. Create mock PanacheQuery objects
        PanacheQuery<User> userQueryMock = Mockito.mock(PanacheQuery.class);
        PanacheQuery<Company> companyQueryMock = Mockito.mock(PanacheQuery.class);

        // 2. Set up the find() method with varargs parameter correctly
        // Panache's find method has signature: find(String query, Object... params)
        when(userRepository.find(eq("username"), any(Object[].class))).thenReturn(userQueryMock);
        when(companyRepository.find(eq("id"), any(Object[].class))).thenReturn(companyQueryMock);

        // 3. Set up the firstResult() return values
        when(userQueryMock.firstResult()).thenReturn(null);
        when(companyQueryMock.firstResult()).thenReturn(null);

        // Act
        assertDoesNotThrow(() -> companyService.CriarFuncionario(req));

        // Assert
        verify(userRepository).persist(any(User.class));
        verify(userCompanyRepository).persist(any(UserCompany.class));
    }


    @Test
    void criarFuncionario_ShouldThrowConflict() {
        // Arrange
        newEmployee req = new newEmployee();
        req.setUsername("existinguser");
        req.setEmail("eeee@gmail.com");
        req.setCompany_id(1L);

        User existingUser = new User();
        existingUser.username = "existinguser";
        existingUser.email = "dddd@gmail.com";

        // 1. Create mock PanacheQuery objects
        PanacheQuery<User> userQueryMock = Mockito.mock(PanacheQuery.class);
        PanacheQuery<Company> companyQueryMock = Mockito.mock(PanacheQuery.class);

        // 2. Set up the find() method with varargs parameter correctly
        // Panache's find method has signature: find(String query, Object... params)
        when(userRepository.find(eq("username"), any(Object[].class))).thenReturn(userQueryMock);
        when(companyRepository.find(eq("id"), any(Object[].class))).thenReturn(companyQueryMock);

        // 3. Set up the firstResult() return values
        when(userQueryMock.firstResult()).thenReturn(existingUser);
        when(companyQueryMock.firstResult()).thenReturn(null);

        // Act & Assert
        assertThrows(ResourceConflictException.class,
                () -> companyService.CriarFuncionario(req));
    }

    @Test
    void criarEmpresa_ShouldPersistNewCompany() {
        // Arrange
        Company req = new Company();
        req.company_name = "New Company";

        // 1. Create mock PanacheQuery objects
        PanacheQuery<Company> companyQueryMock = Mockito.mock(PanacheQuery.class);

        // 2. Set up the find() method with varargs parameter correctly
        // Panache's find method has signature: find(String query, Object... params)
        when(companyRepository.find(eq("name"), any(Object[].class))).thenReturn(companyQueryMock);

        // 3. Set up the firstResult() return values
        when(companyQueryMock.firstResult()).thenReturn(null);

        // Act
        assertDoesNotThrow(() -> companyService.CriarEmpresa(req));

        // Assert
        verify(companyRepository).persist((Company) argThat(company ->
                ((Company) company).company_name.equals("New Company")
        ));
    }

    @Test
    void createUserAndEmpresa_ShouldCreateFullRelation() {
        // Arrange
        CreateUserAdminRequestDTO request = new CreateUserAdminRequestDTO();
        request.setEmployeeRequest(new newEmployee());
        request.setCompanyRequest(new Company());

        // DECLARA PANACHE QUERY
        PanacheQuery<User> userQueryMock = Mockito.mock(PanacheQuery.class);
        PanacheQuery<Company> companyQueryMock = Mockito.mock(PanacheQuery.class);

        // LINKA O FIND COM O PANACHE QUERY
        when(userRepository.find(eq("username = ?1 or email = ?2"), any(Object[].class))).thenReturn(userQueryMock);
        when(companyRepository.find(eq("company_name"), any(Object[].class))).thenReturn(companyQueryMock);

        // SETA O RESULTADO QUANDO UTILZIAR FIRSTRESULT
        when(userQueryMock.firstResult()).thenReturn(null);
        when(companyQueryMock.firstResult()).thenReturn(null);

        // Act
        User result = assertDoesNotThrow(() -> companyService.CreateUserAndEmpresa(request));

        // Assert
        assertNotNull(result);
        verify(companyRepository).persist(any(Company.class));
        verify(userRepository).persist(any(User.class));
        verify(userCompanyRepository).persist(any(UserCompany.class));
    }

    @Test
    void shouldThrowWhenCompanyNameDuplicate() {
        Company newCompany = new Company();
        newCompany.company_name = "Existing Company";

        doThrow(new PersistenceException(new ConstraintViolationException("Duplicate entry", null, null)))
                .when(companyRepository).persist(any(Company.class));

        ResourceConflictException exception = assertThrows(ResourceConflictException.class,
                () -> companyService.CriarEmpresa(newCompany));

        assertEquals("Empresa ja existe", exception.getMessage());
        verify(companyRepository).persist(any(Company.class));
    }
}