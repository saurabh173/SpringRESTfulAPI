package com.springrestfulapi.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Controller {

	@RequestMapping(value = "/login", method = RequestMethod.GET)
	public String showLoginPage() {
		return "Login successful";
	}

	@RequestMapping(value = "/loginx", method = RequestMethod.GET)
	public String showLoginPage2() {
		return "Login successful x";
	}
	
	
	@RequestMapping(value = "/hello", method = RequestMethod.GET)
	public String sayHello() {
		return "Hi there";
	}

	@RequestMapping(value = "/hellox", method = RequestMethod.GET)
	public String sayHello2() {
		return "Hi there x";
	}
	
	
}