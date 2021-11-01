package com.svinarev.compiler.filters;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

import java.io.IOException;
import java.util.Collection;
import java.util.ArrayList;

import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpStatus.FORBIDDEN;

import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;

public class CustomTokenAuthenticationFilter extends OncePerRequestFilter {

	private Algorithm jwtAlgorithm;
	
	public CustomTokenAuthenticationFilter(Algorithm algo) {
		this.jwtAlgorithm = algo;
	}
	
	@Override
	protected void doFilterInternal(HttpServletRequest request,
									HttpServletResponse response,
									FilterChain filterChain) throws ServletException, IOException {
		
		String authHeader = request.getHeader(AUTHORIZATION);
		
		if (authHeader != null && authHeader.startsWith("Bearer ")) {
			String jwt = authHeader.substring("Bearer ".length());
			
			JWTVerifier verifier = JWT.require(jwtAlgorithm).build();
			DecodedJWT decodedToken = verifier.verify(jwt);	
			
			String username = decodedToken.getSubject();
			String[] roles = decodedToken.getClaim("roles").asArray(String.class); 
			
			Collection<GrantedAuthority> authorities = new ArrayList<>();
			for (String roleName: roles) {
				authorities.add(new SimpleGrantedAuthority(roleName));
			}
			
			UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(username, null, authorities);
			SecurityContextHolder.getContext().setAuthentication(token);
			
			filterChain.doFilter(request, response);
		}
		else {
			filterChain.doFilter(request, response);
		}
		
	}
	
}
