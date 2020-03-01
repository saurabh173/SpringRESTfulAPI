package com.springrestfulapi.security;

import javax.servlet.Filter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.ldap.core.support.BaseLdapPathContextSource;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.encoding.PasswordEncoder;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.springrestfulapi.filters.JwtRequestFilter;

@Configuration
@EnableWebSecurity
@PropertySource("classpath:application.properties")
@ComponentScan({"com.springrestfulapi"})
public class SecurityConfiguration  extends WebSecurityConfigurerAdapter {

   @Autowired
   private Environment env;
   
   @Autowired
   private MyUserDetailsService myUserDetailsService;

   @Autowired
   private JwtRequestFilter jwtRequestFilter;

	@Autowired
	  public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {	
		//myUserDetailsService = new MyUserDetailsService();
		//auth.userDetailsService(myUserDetailsService);
		
		if (env.getProperty("ldap.enabled").equalsIgnoreCase("yes") ) {
			auth
			.ldapAuthentication()
			.userDnPatterns(env.getProperty("ldap.usrdnpattern"))
	        .userSearchFilter("(sAMAccountName={0})")
	        .contextSource(contextSource());			
		} else {
			auth
		      .inMemoryAuthentication()
		        .withUser("user")  // #1
		          .password("password")
		          .roles("USER")
		          .and()
		        .withUser("admin") // #2
		          .password("password")
		          .roles("ADMIN","USER");
		}


	  }
	
    @Bean
    public BaseLdapPathContextSource contextSource() {
        LdapContextSource bean = new LdapContextSource();
        bean.setUrl(env.getProperty("ldap.urls"));
        bean.setBase(env.getProperty("ldap.base"));
 
        bean.setUserDn(env.getProperty("ldap.admuserdn"));
        bean.setPassword(env.getProperty("ldap.admpassword"));
        bean.setPooled(true);
        bean.setReferral("follow");
        bean.afterPropertiesSet();
        return bean;
    }
	
	//SM: Below bean was not required in earlier version of Spring
	@Override  
	@Bean
	  public AuthenticationManager authenticationManagerBean() throws Exception {
		  return super.authenticationManagerBean();
	  }
	  
	  
	  @Bean
	  public org.springframework.security.crypto.password.PasswordEncoder passwordEncoder() {
		return NoOpPasswordEncoder.getInstance();
		  
	  }
	  @Override
	  public void configure(WebSecurity web) throws Exception {
	    web
	      .ignoring()
	         .antMatchers("/resources/**"); // #3
	  }

	  @Override
	  protected void configure(HttpSecurity http) throws Exception {
	    http
	      .csrf().disable()
	      .authorizeRequests().antMatchers("/authenticate").permitAll().
	   // all other requests need to be authenticated
	       anyRequest().authenticated()
	      .and().sessionManagement()
	      .sessionCreationPolicy(SessionCreationPolicy.STATELESS);	      // make sure we use stateless session; session won't be used to

	    http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

	      
	      
/*	      .antMatchers("/signup","/about", "/authenticate").permitAll() // #4
	        .antMatchers("/admin/**").hasRole("ADMIN") // #6
	        .anyRequest().authenticated() // 7
	        .and()
	    .formLogin()  // #8
	        .loginPage("/login.jsp") // #9
	        .permitAll(); // #5
*/	  }
}