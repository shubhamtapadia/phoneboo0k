package com.smart.controller;

import java.util.Random;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.dao.UserRepository;
import com.smart.entities.User;
import com.smart.service.EmailService;

@Controller
public class ForgotController {
	//email id form open handler
	Random random = new Random(1000);
	
	@Autowired
	private EmailService emailService; 
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private BCryptPasswordEncoder bcrypt;
	
	@GetMapping("/forgot")
	public String openEmailForm()
	{
		return "forgot_email_form";
	}
	
	@PostMapping("/send-otp")
	public String sendOTP(@RequestParam("email")String email, HttpSession session)
	{
		System.out.println("Your Email ID is : " +email);
		
		//generat otp 4 digit
		
		
		int otp= random.nextInt(9999);
		
		System.out.println("Your OTP is : "+otp);
		
		//String subject, message, to;
		String subject = "OTP from PhoneBook";
		String message = ""+"<div style='border:1px solid #e2e2e2; padding:20px'>"+"<h4>"+ "OTP is for PhoneBook "+"</h4>"+"<h1>"+""+""+ "OTP : "+"<b>"+otp+"</n>"+"</h1>"+"<p><small>Powered by<b> ITCraft</small></p>"+"</div>";
		String to= email;
		//write code for otp send
		boolean flag = this.emailService.sendEmail(subject, message, to);
		
		if(flag)
		{
			session.setAttribute("myotp", otp);
			session.setAttribute("email", email);
			return "verify_otp";
			
		}
		else
		{
			session.setAttribute("message", "Check your Email id");
			return "forgot_email_form";
		}
		//return "verify_otp";
		
	}
	
	//Verify OTP
	@PostMapping("/verify-otp")
	public String verifyOtp(@RequestParam("otp") int otp, HttpSession session)
	{
		int myOtp= (int)session.getAttribute("myotp");
		String email= (String)session.getAttribute("email");
		
		if(myOtp==otp)
		{
			User user =this.userRepository.getUserByUserName(email);
			if(user==null)
			{
				//Send Error message
				session.setAttribute("message", "User does not exist with this email !!");
				System.out.println("User does not exist with this email !!");
				return "forgot_email_form";
			}
			else
			{
				//Send change password form
			}
			
			return "password_change_form";
		}
		else
		{
			session.setAttribute("message", "You Have Entered wrong OTP !!");
			return "verify_otp";
		}
	}
	
	//change-password
	@PostMapping("/change-password")
	public String changePassword(@RequestParam("newpassword") String newpassword, HttpSession session)
	{
		String email= (String)session.getAttribute("email");
		User user =this.userRepository.getUserByUserName(email);
		user.setPassword(this.bcrypt.encode(newpassword));
		this.userRepository.save(user);
		
		//session.setAttribute("message", "Password change successfully");
		return "redirect:/signin?change=Password changed successfully...! ";
	}
	
	
}
