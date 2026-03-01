package org.example.webimagebackend.service;

import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.webimagebackend.config.SecurityProperties;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Date;
import java.util.Map;

@Service
@AllArgsConstructor
@Slf4j
public class TokenService {

    private final SecurityProperties securityProperties;


    public String generateToken(String subject, Map<String, Object> claims) {
        try{
            Date now = new Date();
            Date exp = new Date(now.getTime() + 3600 * 1000 * 24); // 1 hour expiration

            JWTClaimsSet.Builder builder = new JWTClaimsSet.Builder()
                    .subject(subject)
                    .issueTime(now)
                    .expirationTime(exp)
                    .jwtID(java.util.UUID.randomUUID().toString());

            if (claims != null) {
                claims.forEach(builder::claim);
            }

            JWTClaimsSet claimSet = builder.build();

            JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.HS256)
                    .type(JOSEObjectType.JWT)
                    .build();

            SignedJWT signedJWT = new SignedJWT(header, claimSet);
            signedJWT.sign(new MACSigner(securityProperties.getJwtSecretKey()));

            return signedJWT.serialize();
        }
        catch (Exception e) {
            log.error("JWT TOKEN GENERATION FAILED for subject: {}", subject, e);
            throw new RuntimeException("Failed to generate token", e);
        }
    }

    public void verify(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);

            JWSVerifier verifier = new MACVerifier(securityProperties.getJwtSecretKey());

            if(!signedJWT.verify(verifier)){
                throw new RuntimeException("Invalid token signature");
            }
            Date exp = signedJWT.getJWTClaimsSet().getExpirationTime();
            if (exp == null || exp.before(new Date())) {
                throw new RuntimeException("Token has expired");
            }
        }
        catch (Exception e) {
            log.error("JWT TOKEN VERIFICATION FAILED for token: {}", token, e);
            throw new RuntimeException("Failed to verify token", e);
        }

    }
}
