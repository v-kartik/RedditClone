package com.kartik.Reddit.service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import com.kartik.Reddit.dto.RefreshTokenRequest;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kartik.Reddit.dto.AuthenticationResponse;
import com.kartik.Reddit.dto.LoginRequest;
import com.kartik.Reddit.dto.RegisterRequest;
import com.kartik.Reddit.exceptions.SpringRedditException;
import com.kartik.Reddit.model.NotificationEmail;
import com.kartik.Reddit.model.User;
import com.kartik.Reddit.model.VerificationToken;
import com.kartik.Reddit.repository.UserRepository;
import com.kartik.Reddit.repository.VerificationTokenRepository;
import com.kartik.Reddit.security.JwtProvider;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class AuthService {
	
	private final PasswordEncoder passwordEncoder;
	private final UserRepository userRepository; 
	private final VerificationTokenRepository verificationTokenRepository;
	private final MailService mailService;
	private final AuthenticationManager authenticationManager;	
	private final JwtProvider jwtProvider;
	private final RefreshTokenService refreshTokenService;
	
	@Transactional
	public void signup(RegisterRequest registerRequest) {
		User user = new User();
		user.setUsername(registerRequest.getUsername());
		user.setEmail(registerRequest.getEmail());
		user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
		user.setCreated(Instant.now());
		user.setEnabled(false);
		
		userRepository.save(user);
		
		String token = generateVerificationToken(user);
		mailService.sendMail(new NotificationEmail("Please Activate your account",
				user.getEmail(),
				"Thank you for signing up to Spring reddit, " + 
				"please click on the below url to activate your account : " + 
				"http://localhost:8080/api/auth/accountVerification/" + token));
	}

	private String generateVerificationToken(User user) {
		// TODO Auto-generated method stub
		String token = UUID.randomUUID().toString();
		VerificationToken verificationToken = new VerificationToken();
		verificationToken.setToken(token);
		verificationToken.setUser(user);
		
		verificationTokenRepository.save(verificationToken);
		return token;
	}

	@Transactional(readOnly = true)
	public User getCurrentUser() {
		org.springframework.security.core.userdetails.User principal = (org.springframework.security.core.userdetails.User) SecurityContextHolder.
				getContext().getAuthentication().getPrincipal();
		return userRepository.findByUsername(principal.getUsername())
				.orElseThrow(() -> new UsernameNotFoundException("User name not found - " + principal.getUsername()));
	}

	public void verifyAccount(String token) {
		// TODO Auto-generated method stub
		Optional<VerificationToken> verificationToken = verificationTokenRepository.findByToken(token);
		verificationToken.orElseThrow(() -> new SpringRedditException("Invalid Token"));
		fetchUserAndEnable(verificationToken.get());
	}
	
	@Transactional
	private void fetchUserAndEnable(VerificationToken verificationToken) {
		// TODO Auto-generated method stub
		String username = verificationToken.getUser().getUsername();
		User user = userRepository.findByUsername(username).orElseThrow(() -> new SpringRedditException("User Not found" + username));
		user.setEnabled(true);
		userRepository.save(user);
	}

	public AuthenticationResponse login(LoginRequest loginRequest) {
		// TODO Auto-generated method stub
		Authentication authenticate = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
				loginRequest.getUsername(),
				loginRequest.getPassword()));
		SecurityContextHolder.getContext().setAuthentication(authenticate);
		String token = jwtProvider.generateToken(authenticate);
		return AuthenticationResponse.builder()
				.authenticateToken(token)
				.refreshToken(refreshTokenService.generateRefreshToken().getToken())
				.expiresAt(Instant.now().plusMillis(jwtProvider.getJwtExpirationInMillis()))
				.username(loginRequest.getUsername())
				.build();
	}
	public boolean isLoggedIn() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		return !(authentication instanceof AnonymousAuthenticationToken) && authentication.isAuthenticated();
	}

	public AuthenticationResponse refreshToken(RefreshTokenRequest refreshTokenRequest) {
		refreshTokenService.validateRefreshToken(refreshTokenRequest.getRefreshToken());
		String token = jwtProvider.generateTokenWithUserName(refreshTokenRequest.getUsername());
		return AuthenticationResponse.builder()
				.authenticateToken(token)
				.refreshToken(refreshTokenRequest.getRefreshToken())
				.expiresAt(Instant.now().plusMillis(jwtProvider.getJwtExpirationInMillis()))
				.username(refreshTokenRequest.getUsername())
				.build();
	}
}
