package echo.controller;

import echo.exception.AppException;
import echo.model.Account;
import echo.model.Role;
import echo.model.RoleName;
import echo.payload.ApiResponse;
import echo.payload.JwtAuthenticationResponse;
import echo.payload.LoginRequest;
import echo.payload.SignUpRequest;
import echo.repository.AccountRepository;
import echo.repository.RoleRepository;
import echo.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import java.net.URI;
import java.util.Collections;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    JwtTokenProvider tokenProvider;

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        // ResponseEntity 타입은 Http 의 상태코드를 함께 전송해주기 위해 사용한다.
        // ResponseEntity 오브젝트는 HTTP 응답의 바디에 삽입할 오브젝트를 소유한다.(예제에서 Customer 해당)
        // ResponseEntity 오브젝트는 바디 오브젝트에 추가해 스테이터스 코드와 HTTP 응답 헤더를 설정할 수 있다.
        // @RequestBody 혹은 @ResponseBody는 자바객체를 http으로(json) http를 자바객체로 변환해준다.
        // 고로 @RequestBody를 사용할려면 변환하고자 하는 자바객체 즉 class를 생성하여야한다.
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = tokenProvider.generateToken(authentication);
        return ResponseEntity.ok(new JwtAuthenticationResponse(jwt));
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignUpRequest signUpRequest) {
        if (accountRepository.existsByEmail(signUpRequest.getEmail())) {
            return new ResponseEntity(new ApiResponse(false, "Username is already taken!"), HttpStatus.BAD_REQUEST);
        }
        // Creating user's account
        Account accont = new Account(signUpRequest.getEmail(), signUpRequest.getPassword());

        accont.setPassword(passwordEncoder.encode(accont.getPassword()));

        Role userRole = roleRepository.findByName(RoleName.ROLE_USER).orElseThrow(() -> new AppException("User Role not set."));

        accont.setRoles(Collections.singleton(userRole));

        Account result = accountRepository.save(accont);

        URI location = ServletUriComponentsBuilder.fromCurrentContextPath().path("/api/users/{email}")
                .buildAndExpand(result.getEmail()).toUri();
        return ResponseEntity.created(location).body(new ApiResponse(true, "User registered successfully"));
    }
}