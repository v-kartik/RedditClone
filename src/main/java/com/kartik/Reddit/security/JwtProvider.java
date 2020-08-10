package com.kartik.Reddit.security;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.time.Instant;
import java.util.Date;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.kartik.Reddit.exceptions.SpringRedditException;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParserBuilder;
import io.jsonwebtoken.Jwts;

import static java.util.Date.from;


@Service
public class JwtProvider {
	
	private KeyStore keyStore;
	@Value("${jwt.expiration.time}")
	private Long jwtExpirationMillis;

	@PostConstruct
	 public void init() {
        try {
            keyStore = KeyStore.getInstance("JKS");
            InputStream resourceAsStream = getClass().getResourceAsStream("/springblog.jks");
            keyStore.load(resourceAsStream, "secret".toCharArray());
        } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException | IOException e) {
            throw new SpringRedditException("Exception occurred while loading keystore");
        }

	}
	
	public String generateToken(Authentication authentication) {
		org.springframework.security.core.userdetails.User principal = 
					(org.springframework.security.core.userdetails.User)authentication.getPrincipal();
		return Jwts.builder()
				.setSubject(principal.getUsername())
				.signWith(getPrivateKey())
				.setExpiration(from(Instant.now().plusMillis(jwtExpirationMillis)))
				.compact();
	}

	 private PrivateKey getPrivateKey() {
	        try {
	            return (PrivateKey) keyStore.getKey("springblog", "secret".toCharArray());
	        } catch (KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException e) {
	            throw new SpringRedditException("Exception occured while retrieving public key from keystore");
	        }
    }
	 
	 //@SuppressWarnings("deprecation")
	public boolean validateToken(String jwt) {
		boolean validation = false;
		 try {
			 Jwts.parserBuilder().setSigningKey(getPublicKey()).build().parseClaimsJws(jwt);
			 //jwtParser.build().parseClaimsJws(jwt);
			 validation = true;
		 } catch(ExpiredJwtException e){
		 	 System.out.println("Token Expired");
		 } catch(SignatureException e){
			 System.out.println(JwtProvider.class.getName());
		 } catch (Exception e) {
		 	System.out.println("Some Other Exception");
		 }
		 return validation;
	 }

	private PublicKey getPublicKey() {
		// TODO Auto-generated method stub
		try {
			return keyStore.getCertificate("springblog").getPublicKey();
		}catch(KeyStoreException e) {
			throw new SpringRedditException("Exception occured while retrieving public key from keystore");
		}

	}
	
	//@SuppressWarnings("deprecation")
	public String getusernameFromJwt(String token) {
		Claims claims = Jwts.parserBuilder()
				.setSigningKey(getPublicKey())
				.build()
				.parseClaimsJws(token)
				.getBody();
		return claims.getSubject();
	}

	public Long getJwtExpirationInMillis() {
		return jwtExpirationMillis;
	}

	public String generateTokenWithUserName(String username) {
		return Jwts.builder()
				.setSubject(username)
				.setIssuedAt(from(Instant.now()))
				.signWith(getPrivateKey())
				.setExpiration(from(Instant.now().plusMillis(jwtExpirationMillis)))
				.compact();
	}

}
