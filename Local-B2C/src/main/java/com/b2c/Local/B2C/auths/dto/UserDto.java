package com.b2c.Local.B2C.auths.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Email;
import javax.validation.constraints.Size;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {

    @Size(min = 10)
    //@Pattern(regexp = )
    private String newPassword;

    @Size(min = 10)
    private String confirmPassword;

    @Email
    private String email;

    @Size(min = 2)
    private String firstName;

    private String lastName;

    @Range(min = 6000000000L, max = 9999999999L)
    private Long mobileNumber;

    private boolean storeOwner;

    @Range(min = 1, max = 5)
    private Integer maxSession;

    @Size(min = 2)
    //@NotEmpty(message = "Can't be empty")
    private String username;

    private String currentPassword;

    @AssertTrue
    public boolean matchPassword(){
        return this.newPassword.equals(this.confirmPassword);
    }
}
