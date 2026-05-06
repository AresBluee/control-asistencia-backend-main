package com.agro.control_asistencia_backend.employee.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class EmployeeProfileUpdateDTO {

    @Size(max = 50, message = "El nombre no puede exceder los 50 caracteres.")
    private String firstName;

    @Size(max = 50, message = "El apellido no puede exceder los 50 caracteres.")
    private String lastName;

    @Email(message = "Formato de email inválido.")
    private String email;

    @Size(max = 15, message = "El número de teléfono no puede exceder los 15 caracteres.")
    private String phoneNumber;

    @Size(max = 8, message = "El DNI debe tener 8 caracteres.")
    private String dni;

    @Size(max = 200, message = "La dirección no puede exceder los 200 caracteres.")
    private String address;
}
