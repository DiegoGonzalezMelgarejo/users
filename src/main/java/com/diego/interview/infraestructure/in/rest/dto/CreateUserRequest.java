package com.diego.interview.infraestructure.in.rest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CreateUserRequest {

    @NotBlank(message = "{user.name.required}")
    private String name;

    @NotBlank(message = "{user.email.required}")
    private String email;

    @NotBlank(message = "{user.password.required}")
    private String password;

    @NotNull(message = "{user.phones.required}")
    @Size(min = 1, message = "{user.phones.min}")
    @Valid
    private List<PhoneRequest> phones;

    @Getter
    @Setter
    public static class PhoneRequest {

        @NotBlank(message = "{user.phone.number.required}")
        @Pattern(
                regexp = "^\\d{1,10}$",
                message = "{user.phone.number.maxlength}"
        )
        private String numero;

        @NotBlank(message = "{user.phone.city.required}")
        private String codigoCiudad;

        @NotBlank(message = "{user.phone.country.required}")
        private String codigoPais;
    }
}
