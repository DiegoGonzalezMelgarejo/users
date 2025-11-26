package com.diego.interview.infraestructure;

import com.diego.interview.application.usecase.CreateUserUseCase;
import com.diego.interview.application.usecase.dto.CreateUserCommand;
import com.diego.interview.application.usecase.dto.UserResponse;
import com.diego.interview.infraestructure.in.rest.UserController;
import com.diego.interview.infraestructure.in.rest.dto.CreateUserRequest;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private CreateUserUseCase createUserUseCase;

    private UserController controller;

    @BeforeEach
    void setUp() {
        controller = new UserController(createUserUseCase);
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
}

