package com.diego.interview.infraestructure.in.rest.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UpdateUserRequest {

    private String name;

    @Email(message = "{user.email.invalid}")
    private String email;

    private String password;

    private Boolean active;

    @Valid
    private List<PhoneRequest> phones;

    @Getter
    @Setter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class PhoneRequest {

        @Pattern(regexp = "^\\d+$", message = "{user.phone.number.numeric}")
        @Size(max = 10, message = "{user.phone.number.maxlength}")
        private String numero;

        private String codigoCiudad;
        private String codigoPais;
    }
}
