package com.thesis.alumni.system.controller;

import com.thesis.alumni.system.dto.BaseResponse;
import com.thesis.alumni.system.dto.UserDto;
import com.thesis.alumni.system.exception.UserHandleException;
import com.thesis.alumni.system.model.JwtRequest;
import com.thesis.alumni.system.model.JwtResponse;
import com.thesis.alumni.system.service.UserService;
import com.thesis.alumni.system.service.impl.JwtUserDetailsService;
import com.thesis.alumni.system.utils.JwtTokenUtil;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.mail.MessagingException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;

@RestController
@CrossOrigin("*")
@AllArgsConstructor
@RequestMapping("/api/accounts")
public class JwtAuthenticationController {
    final AuthenticationManagerBuilder authenticationManagerBuilder;
    final JwtTokenUtil jwtTokenUtil;
    final JwtUserDetailsService jwtUserDetailsService;
    final UserService userService;

    @PostMapping("/authenticate")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody JwtRequest jwtRequest) throws UserHandleException {
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                jwtRequest.getEmail(), jwtRequest.getPassword()
        );
        Authentication authentication = null;
        try {
            authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
        } catch (DisabledException e) {
            throw new UserHandleException("T??i kho???n n??y ch??a ???????c k??ch ho???t!", HttpStatus.FORBIDDEN);
        }
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String token = jwtTokenUtil.generateToken(authentication);
        return new ResponseEntity<>(
                BaseResponse.builder().status(200).message("OK").data(new JwtResponse(token)).build(),
                HttpStatus.OK);
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerAccount(@RequestBody UserDto user) throws MessagingException {
        userService.processActiveAccount(user);
        return new ResponseEntity<>(
                BaseResponse.builder().status(200).message("OK").data(user).build(),
                HttpStatus.OK);
    }

    @GetMapping("/active")
    public ResponseEntity<?> activeAccount(@RequestParam(defaultValue = "") String token) throws UnsupportedEncodingException {
        String message = "X??c nh???n t??i kho???n th??nh c??ng! ????ng nh???p ????? ti???p t???c.";
        try {
            userService.activeAccount(token);
        } catch (Exception exception) {
            message = "???????ng link kh??ng h???p l??? ho???c ???? h???t h???n! Vui l??ng th??? ????ng k?? l???i.";
        }
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create("http://localhost:3000/login?message=" + URLEncoder.encode(message,"UTF-8")))
                .build();
    }

}
