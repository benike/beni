package hu.beni.amusementpark.config.security;

import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.lang.Nullable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.fasterxml.jackson.databind.ObjectMapper;

import hu.beni.amusementpark.mapper.VisitorMapper;
import hu.beni.amusementpark.service.VisitorService;

public class ValidateingUsernamePasswordAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

	private String usernameParameter = "email";
	private String passwordParameter = "password";

	private int min = 5;
	private int max = 25;

	public ValidateingUsernamePasswordAuthenticationFilter(VisitorService visitorService, ObjectMapper objectMapper,
			VisitorMapper visitorMapper, AuthenticationManager authenticationManager) {
		super(new AntPathRequestMatcher("/api/login", "POST"));
		setAuthenticationSuccessHandler(
				new AmusementParkAuthenticationSuccessHandler(visitorService, objectMapper, visitorMapper));
		setAuthenticationFailureHandler(new AmusementParkAuthenticationFailureHandler());
		setAuthenticationManager(authenticationManager);
	}

	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) {
		if (!request.getMethod().equals("POST")) {
			throw new AuthenticationServiceException("Authentication method not supported: " + request.getMethod());
		}

		String username = validateEmail(obtainUsername(request));
		String password = validatePassword(obtainPassword(request));

		UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(username, password);

		authRequest.setDetails(authenticationDetailsSource.buildDetails(request));

		return this.getAuthenticationManager().authenticate(authRequest);
	}

	private String validateEmail(String email) {
		return Optional.ofNullable(email).filter(this::isValidEmail)
				.orElseThrow(() -> new BadCredentialsException("Email must be a well-formed email address"));
	}

	private boolean isValidEmail(String email) {
		return email.matches(".+@.+\\..+");
	}

	private String validatePassword(String credential) {
		return Optional.ofNullable(credential).map(String::trim).filter(this::isLengthBetweenMinAndMax).orElseThrow(
				() -> new BadCredentialsException(String.format("Password size must be between %d and %d", min, max)));
	}

	private boolean isLengthBetweenMinAndMax(String string) {
		return string.length() >= min && string.length() <= max;
	}

	@Nullable
	protected String obtainPassword(HttpServletRequest request) {
		return request.getParameter(passwordParameter);
	}

	@Nullable
	protected String obtainUsername(HttpServletRequest request) {
		return request.getParameter(usernameParameter);
	}

}
