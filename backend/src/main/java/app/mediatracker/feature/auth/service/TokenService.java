package app.mediatracker.feature.auth.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class TokenService {

    private final Algorithm algorithm;
    private final JWTVerifier verifier;
    private final long accessTokenExpirationInMillis;
    private final long refreshTokenExpirationInMillis;

    public TokenService(@Value("${app.jwt.secret}") String secretKey,
                        @Value("${app.jwt.access-token-expiration-in-seconds}") long accessTokenExpirationInMillis,
                        @Value("${app.jwt.refresh-token-expiration-in-seconds}") long refreshTokenExpirationInMillis) {
        if (secretKey == null || secretKey.isBlank()) {
            throw new IllegalStateException("app.jwt.secret is empty. Set JWT_SECRET, see backend/.env.example.");
        }
        this.algorithm = Algorithm.HMAC256(secretKey);
        this.verifier = JWT.require(algorithm).build();
        this.accessTokenExpirationInMillis = accessTokenExpirationInMillis * 1000;
        this.refreshTokenExpirationInMillis = refreshTokenExpirationInMillis * 1000;
    }

    /**
     * Generates an access token for the specified username.
     *
     * @param userId   the owner of the token
     * @param username the username to be included as a claim
     * @return the generated JWT access token as a String
     */
    public String generateAccessToken(ObjectId userId, String username) {
        return JWT.create()
                .withSubject(userId.toString())
                .withClaim("username", username)
                .withExpiresAt(new Date(System.currentTimeMillis() + accessTokenExpirationInMillis))
                .sign(algorithm);
    }

    /**
     * Generates a refresh token for the specified user ID.
     *
     * @param userId the owner of the token
     * @return the generated JWT refresh token as a String
     */
    public String generateRefreshToken(ObjectId userId) {
        return JWT.create()
                .withSubject(userId.toString())
                .withExpiresAt(new Date(System.currentTimeMillis() + refreshTokenExpirationInMillis))
                .sign(algorithm);
    }


    /**
     * Validates the token and extracts the User ID.
     *
     * @param token the JWT token to verify
     * @return the User ID (Subject) as a String, if the token is valid
     */
    public String verifyTokenAndGetUserId(String token) {
        return JWT.require(algorithm)
                .build()
                .verify(token)
                .getSubject();
    }

    /**
     * Extracts the username from a token.
     *
     * @param token the JWT token
     * @return the username or null if verification fails
     */
    public String extractUsername(String token) {
        try {
            DecodedJWT decodedJWT = verifier.verify(token);
            return decodedJWT.getClaim("username").asString();
        } catch (JWTVerificationException e) {
            return null;
        }
    }
}