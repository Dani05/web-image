package org.example.webimagebackend.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class SecurityProperties {

    @Value("${security.jwt.secret-key}")
    private String jwtSecretKey;

}

