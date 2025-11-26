package com.diego.interview.infraestructure;

import com.diego.interview.domain.exception.BusinessException;
import com.diego.interview.infraestructure.in.rest.advice.RestExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.lang.reflect.Method;
import java.util.Locale;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RestExceptionHandlerTest {

    @Mock
    private MessageSource messageSource;

    private RestExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new RestExceptionHandler(messageSource);
    }

    @Test
    void handleBusiness_shouldResolveMessageAndReturnBadRequest() {
        String code = "user.email.exists";
        Object[] args = new Object[]{"john.doe@test.com"};
        BusinessException ex = new BusinessException(code, args);

        Locale locale = Locale.ENGLISH;
        String translated = "The email is already registered";

        when(messageSource.getMessage(code, args, locale)).thenReturn(translated);

        ResponseEntity<Map<String, String>> response = handler.handleBusiness(ex, locale);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).containsEntry("mensaje", translated);

        verify(messageSource, times(1)).getMessage(code, args, locale);
    }

    @Test
    void handleValidation_shouldReturnFirstFieldErrorMessage() throws NoSuchMethodException {
        Object target = new Object();
        BeanPropertyBindingResult bindingResult =
                new BeanPropertyBindingResult(target, "createUserRequest");

        bindingResult.addError(new FieldError("createUserRequest",
                "email", "El correo es obligatorio"));
        bindingResult.addError(new FieldError("createUserRequest",
                "password", "La contraseña es obligatoria"));

        Method method = DummyController.class.getDeclaredMethod("dummyMethod", String.class);
        MethodParameter methodParameter = new MethodParameter(method, 0);

        MethodArgumentNotValidException ex =
                new MethodArgumentNotValidException(methodParameter, bindingResult);

        ResponseEntity<Map<String, String>> response = handler.handleValidation(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).containsEntry("mensaje", "El correo es obligatorio");
    }

    @Test
    void handleValidation_shouldReturnDefaultMessageWhenNoFieldErrors() throws NoSuchMethodException {
        Object target = new Object();
        BeanPropertyBindingResult bindingResult =
                new BeanPropertyBindingResult(target, "createUserRequest");

        Method method = DummyController.class.getDeclaredMethod("dummyMethod", String.class);
        MethodParameter methodParameter = new MethodParameter(method, 0);

        MethodArgumentNotValidException ex =
                new MethodArgumentNotValidException(methodParameter, bindingResult);

        ResponseEntity<Map<String, String>> response = handler.handleValidation(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).containsEntry("mensaje", "Solicitud inválida");
    }

    static class DummyController {
        @SuppressWarnings("unused")
        public void dummyMethod(String param) {
        }
    }
}
