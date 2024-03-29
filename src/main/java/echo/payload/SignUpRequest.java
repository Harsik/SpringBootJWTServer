package echo.payload;

import javax.validation.constraints.*;

import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

// @Notnull 일 경우
// null 허용 하지 않는다.“”허용한다.
// @NotEmpty 일 경우 null 허용 하지 않는다.
//“”허용하지 않는다.”“(space)허용한다.
// @NotBlank 일 경우 셋다 허용 하지 않는다.
// @Data //Type definition error 발생 
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SignUpRequest {

    @NotBlank
    @Size(min = 3, max = 15)
    private String email;

    @NotBlank
    @Size(min = 8, max = 20)
    private String password;

}