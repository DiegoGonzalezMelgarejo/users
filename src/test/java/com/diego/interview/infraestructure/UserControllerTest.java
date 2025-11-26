package com.diego.interview.infraestructure;
import com.diego.interview.application.usecase.CreateUserUseCase;
import com.diego.interview.application.usecase.DeleteUserUseCase;
import com.diego.interview.application.usecase.GetUserByIdUseCase;
import com.diego.interview.application.usecase.ListUsersUseCase;
import com.diego.interview.application.usecase.LoginUserUseCase;
import com.diego.interview.application.usecase.UpdateUserUseCase;
import com.diego.interview.application.usecase.dto.CreateUserCommand;
import com.diego.interview.application.usecase.dto.PagedResponse;
import com.diego.interview.application.usecase.dto.UpdateUserCommand;
import com.diego.interview.application.usecase.dto.UserResponse;
import com.diego.interview.infraestructure.in.rest.UserController;
import com.diego.interview.infraestructure.in.rest.dto.CreateUserRequest;
import com.diego.interview.infraestructure.in.rest.dto.LoginRequest;
import com.diego.interview.infraestructure.in.rest.dto.UpdateUserRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private CreateUserUseCase createUserUseCase;
    @Mock
    private LoginUserUseCase loginUserUseCase;
    @Mock
    private ListUsersUseCase listUsersUseCase;
    @Mock
    private GetUserByIdUseCase getUserByIdUseCase;
    @Mock
    private DeleteUserUseCase deleteUserUseCase;
    @Mock
    private UpdateUserUseCase updateUserUseCase;

    private UserController controller;

    @BeforeEach
    void setUp() {
        controller = new UserController(
                createUserUseCase,
                loginUserUseCase,
                listUsersUseCase,
                getUserByIdUseCase,
                deleteUserUseCase,
                updateUserUseCase
        );
    }

    @Test
    void create_shouldMapRequestToCommandAndReturnCreatedResponse() {
        CreateUserRequest.PhoneRequest phoneReq = new CreateUserRequest.PhoneRequest();
        phoneReq.setNumero("1234567");
        phoneReq.setCodigoCiudad("1");
        phoneReq.setCodigoPais("57");

        CreateUserRequest request = new CreateUserRequest();
        request.setName("John Doe");
        request.setEmail("john.doe@test.com");
        request.setPassword("Password123");
        request.setPhones(List.of(phoneReq));

        LocalDateTime now = LocalDateTime.now();

        UserResponse expectedResponse = new UserResponse();
        expectedResponse.setId("some-id");
        expectedResponse.setName("John Doe");
        expectedResponse.setEmail("john.doe@test.com");
        expectedResponse.setCreated(now);
        expectedResponse.setModified(now);
        expectedResponse.setLastLogin(now);
        expectedResponse.setActive(true);
        expectedResponse.setToken("dummy-token");

        when(createUserUseCase.createUser(any(CreateUserCommand.class)))
                .thenReturn(expectedResponse);

        ResponseEntity<UserResponse> responseEntity = controller.create(request);

        ArgumentCaptor<CreateUserCommand> captor =
                ArgumentCaptor.forClass(CreateUserCommand.class);

        verify(createUserUseCase, times(1)).createUser(captor.capture());

        CreateUserCommand sentCommand = captor.getValue();
        assertThat(sentCommand.getName()).isEqualTo("John Doe");
        assertThat(sentCommand.getEmail()).isEqualTo("john.doe@test.com");
        assertThat(sentCommand.getPassword()).isEqualTo("Password123");
        assertThat(sentCommand.getPhones()).hasSize(1);
        assertThat(sentCommand.getPhones().get(0).getNumber()).isEqualTo("1234567");
        assertThat(sentCommand.getPhones().get(0).getCityCode()).isEqualTo("1");
        assertThat(sentCommand.getPhones().get(0).getCountryCode()).isEqualTo("57");

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getEmail()).isEqualTo("john.doe@test.com");
        assertThat(responseEntity.getBody().getName()).isEqualTo("John Doe");
        assertThat(responseEntity.getBody().getToken()).isEqualTo("dummy-token");
    }

    @Test
    void create_shouldHandleNullPhonesAsEmptyList() {
        CreateUserRequest request = new CreateUserRequest();
        request.setName("Jane Doe");
        request.setEmail("jane.doe@test.com");
        request.setPassword("Password123");
        request.setPhones(null);

        when(createUserUseCase.createUser(any(CreateUserCommand.class)))
                .thenReturn(new UserResponse());

        controller.create(request);

        ArgumentCaptor<CreateUserCommand> captor =
                ArgumentCaptor.forClass(CreateUserCommand.class);

        verify(createUserUseCase, times(1)).createUser(captor.capture());

        CreateUserCommand sentCommand = captor.getValue();
        assertThat(sentCommand.getPhones()).isNotNull();
        assertThat(sentCommand.getPhones()).isEmpty();
    }


    @Test
    void login_shouldDelegateToUseCaseAndReturnOkResponse() {
        LoginRequest request = new LoginRequest();
        request.setEmail("john.doe@test.com");
        request.setPassword("Password123");

        UserResponse expected = new UserResponse();
        expected.setId("user-id");
        expected.setEmail("john.doe@test.com");
        expected.setName("John Doe");
        expected.setToken("jwt-token");

        when(loginUserUseCase.login("john.doe@test.com", "Password123")).thenReturn(expected);

        ResponseEntity<UserResponse> responseEntity = controller.login(request);

        verify(loginUserUseCase, times(1))
                .login("john.doe@test.com", "Password123");

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getEmail()).isEqualTo("john.doe@test.com");
        assertThat(responseEntity.getBody().getToken()).isEqualTo("jwt-token");
    }

    @Test
    void getAllPaged_shouldCallUseCaseWithGivenPageAndSizeAndReturnResponse() {
        int page = 1;
        int size = 5;

        UserResponse u1 = new UserResponse();
        u1.setId("id-1");
        u1.setEmail("u1@test.com");
        UserResponse u2 = new UserResponse();
        u2.setId("id-2");
        u2.setEmail("u2@test.com");

        PagedResponse<UserResponse> paged = new PagedResponse<>();
        paged.setPage(page);
        paged.setSize(size);
        paged.setTotalElements(2L);
        paged.setTotalPages(1);
        paged.setContent(List.of(u1, u2));

        when(listUsersUseCase.listUsers(page, size)).thenReturn(paged);

        ResponseEntity<PagedResponse<UserResponse>> responseEntity =
                controller.getAllPaged(page, size);

        verify(listUsersUseCase, times(1)).listUsers(page, size);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getPage()).isEqualTo(page);
        assertThat(responseEntity.getBody().getSize()).isEqualTo(size);
        assertThat(responseEntity.getBody().getContent()).hasSize(2);
    }

    @Test
    void getById_shouldCallUseCaseAndReturnUserResponse() {
        UUID id = UUID.randomUUID();

        UserResponse expected = new UserResponse();
        expected.setId(id.toString());
        expected.setEmail("john.doe@test.com");
        expected.setName("John Doe");

        when(getUserByIdUseCase.getById(id)).thenReturn(expected);

        ResponseEntity<UserResponse> responseEntity = controller.getById(id);

        verify(getUserByIdUseCase, times(1)).getById(id);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getId()).isEqualTo(id.toString());
        assertThat(responseEntity.getBody().getEmail()).isEqualTo("john.doe@test.com");
    }


    @Test
    void delete_shouldCallUseCaseAndReturnNoContent() {
        UUID id = UUID.randomUUID();

        ResponseEntity<Void> responseEntity = controller.delete(id);

        verify(deleteUserUseCase, times(1)).deleteById(id);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(responseEntity.getBody()).isNull();
    }

    @Test
    void updateUser_shouldMapRequestToUpdateCommandAndReturnResponse() {
        UUID id = UUID.randomUUID();

        UpdateUserRequest.PhoneRequest phoneReq = new UpdateUserRequest.PhoneRequest();
        phoneReq.setNumero("9876543210");
        phoneReq.setCodigoCiudad("2");
        phoneReq.setCodigoPais("34");

        UpdateUserRequest request = new UpdateUserRequest();
        request.setName("New Name");
        request.setEmail("new.email@test.com");
        request.setPassword("NewPassword123");
        request.setActive(Boolean.TRUE);
        request.setPhones(List.of(phoneReq));

        UserResponse expected = new UserResponse();
        expected.setId(id.toString());
        expected.setName("New Name");
        expected.setEmail("new.email@test.com");

        when(updateUserUseCase.update(any(UUID.class), any(UpdateUserCommand.class)))
                .thenReturn(expected);

        ResponseEntity<UserResponse> responseEntity = controller.updateUser(id, request);

        ArgumentCaptor<UUID> idCaptor = ArgumentCaptor.forClass(UUID.class);
        ArgumentCaptor<UpdateUserCommand> cmdCaptor = ArgumentCaptor.forClass(UpdateUserCommand.class);

        verify(updateUserUseCase, times(1)).update(idCaptor.capture(), cmdCaptor.capture());

        UUID capturedId = idCaptor.getValue();
        UpdateUserCommand command = cmdCaptor.getValue();

        assertThat(capturedId).isEqualTo(id);
        assertThat(command.getName()).isEqualTo("New Name");
        assertThat(command.getEmail()).isEqualTo("new.email@test.com");
        assertThat(command.getPassword()).isEqualTo("NewPassword123");
        assertThat(command.getActive()).isTrue();
        assertThat(command.getPhones()).hasSize(1);
        assertThat(command.getPhones().get(0).getNumber()).isEqualTo("9876543210");
        assertThat(command.getPhones().get(0).getCityCode()).isEqualTo("2");
        assertThat(command.getPhones().get(0).getCountryCode()).isEqualTo("34");

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getId()).isEqualTo(id.toString());
        assertThat(responseEntity.getBody().getEmail()).isEqualTo("new.email@test.com");
    }

    @Test
    void updateUser_shouldAllowNullPhonesAndMapToNullInCommand() {
        UUID id = UUID.randomUUID();

        UpdateUserRequest request = new UpdateUserRequest();
        request.setName("Partial Name");
        request.setEmail(null);
        request.setPassword(null);
        request.setActive(null);
        request.setPhones(null);

        when(updateUserUseCase.update(any(UUID.class), any(UpdateUserCommand.class)))
                .thenReturn(new UserResponse());

        controller.updateUser(id, request);

        ArgumentCaptor<UpdateUserCommand> cmdCaptor = ArgumentCaptor.forClass(UpdateUserCommand.class);
        verify(updateUserUseCase, times(1)).update(any(UUID.class), cmdCaptor.capture());

        UpdateUserCommand command = cmdCaptor.getValue();
        assertThat(command.getName()).isEqualTo("Partial Name");
        assertThat(command.getEmail()).isNull();
        assertThat(command.getPassword()).isNull();
        assertThat(command.getActive()).isNull();
        assertThat(command.getPhones()).isNull(); // porque en el controller lo dejas en null si no viene
    }
}