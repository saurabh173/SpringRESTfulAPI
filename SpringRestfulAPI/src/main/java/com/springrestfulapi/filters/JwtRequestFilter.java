package com.springrestfulapi.filters;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.springrestfulapi.security.JwtUtil;
import com.springrestfulapi.security.MyUserDetailsService;

import io.jsonwebtoken.ExpiredJwtException;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

	@Autowired
	private MyUserDetailsService myUserDetailsService;
	
	@Autowired
	private JwtUtil jwtUtil;
	
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		
		final String authorizingHeader = request.getHeader("Authorization");
		
		String username = null;
		String jwtToken = null;
		
		System.out.println("authorizingHeader = " + authorizingHeader);
		// JWT Token is in the form "Bearer token". Remove Bearer word and get
		// only the Token
		if (authorizingHeader != null && authorizingHeader.startsWith("Bearer ")) {
			jwtToken = authorizingHeader.substring(7);
				try {
					username = jwtUtil.getUsernameFromToken(jwtToken);
					System.out.println("username = " + username);
				} catch (IllegalArgumentException e) {
					System.out.println("Unable to get JWT Token");
				} catch (ExpiredJwtException e) {
					System.out.println("JWT Token has expired");
				}
		} else {
			System.out.println("JWT Token does not begin with Bearer String");
		}
		
		// Once we get the token validate it. Only if the context does not have the user already
		if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
			UserDetails userDetails = this.myUserDetailsService.loadUserByUsername(username);
			// if token is valid configure Spring Security to manually set
			// authentication
			
			System.out.println("Checking Token Valiadtion");
			if (jwtUtil.validateToken(jwtToken, userDetails)) {
				
				System.out.println("Token is valid");
				
				//SM: usernamePasswordAuthenticationToken is the default token 
				// from spring security. This would have happened by default by Spring but
				// in here we are doing it only if token is valid
				
				UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
				userDetails, null, userDetails.getAuthorities());
				
				usernamePasswordAuthenticationToken
				.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
				
				// After setting the Authentication in the context, we specify
				// that the current user is authenticated. So it passes the
				// Spring Security Configurations successfully.
				SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
			}
		}
		filterChain.doFilter(request, response);
		}
	}


