package com.smart.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.Optional;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.User;
import com.smart.helper.Message;

@Controller
@RequestMapping("/user")
public class UserController {

	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	@Autowired
	UserRepository userRepository; 
	@Autowired
	ContactRepository contactRepository; 
	
	@ModelAttribute
	public void addCommanData(Model model, Principal principle)
	{
		String userName = principle.getName();
		System.out.println("Username : "+ userName);
		
		// get the user using userName from database
		User user = userRepository.getUserByUserName(userName);		
		System.out.println("Username : "+ user);
		
		model.addAttribute("user",user);
		
	
	}
	
	@RequestMapping("/index")
	public String dashboard(Model model, Principal principle)
	{
		String userName = principle.getName();
		System.out.println("Username : "+ userName);
		
		// get the user using userName from database
		User user = userRepository.getUserByUserName(userName);		
		System.out.println("Username : "+ user);
		
		model.addAttribute("user",user);		
		return "/normal/user_dashboard";
	}
	
	//open add form handler
	@GetMapping("/add-contact")
	public String openAddContactForm(Model model)
	{
		model.addAttribute("title","Add Contact");
		model.addAttribute("contact", new Contact());
		
		return "/normal/add_contact_form";
	}
	
	//Processing add contact form
	@PostMapping("/process-contact")
	public String processContact(
			@ModelAttribute Contact contact,
			@RequestParam("profileImage") MultipartFile file, 
			Principal principle ,
			HttpSession session)
	{
		try
		{
			String name = principle.getName();
			User user = this.userRepository.getUserByUserName(name);//kinsa user contact save kr raha he iske liye user liya
			
			//Processing and file uploading ... file realated part
			if(file.isEmpty())
			{
				//if the file is empty 
				System.out.println("file is not available");
				contact.setImage("contact.png");
			}
			else
			{
				//add file to file folder and then update name to contact
				contact.setImage(file.getOriginalFilename());
				File saveFile =new ClassPathResource("static/img").getFile();
				Path path =Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				System.out.println("image is uploaded");
			}
			
			
			user.getContacts().add(contact);// fir us pure contact ko add kiya 
			contact.setUser(user); // contact ko user diya
			this.userRepository.save(user);//or data base me save kiya
			
			System.out.println("Added to Database ");
			System.out.println("Data : "+contact);
			
			// sending sucess message
			session.setAttribute("message", new Message("Your Contact is added", "alert-success") );
		}
		catch(Exception e)
		{
			System.out.println("Error :"+e.getMessage());
			e.printStackTrace();
			//sending error message
			session.setAttribute("message", new Message("Something went wrong try again", "alert-danger") );
		}		
		return "/normal/add_contact_form";
	}
	
	//View Contact handler
	//below page is for pagging per page kitne contact dikhane chahiye 
	@GetMapping("/show-contacts/{page}")
	public String showContacts(@PathVariable("page") Integer page ,Model model, Principal principle)
	{
		model.addAttribute("title","Show User Contacts");
		
		//contact ki list ko bhejni he hame
		//or per page 5 contact dikhane he
		String userName = principle.getName();
		User user = this.userRepository.getUserByUserName(userName);
		
		Pageable pageable = PageRequest.of(page, 3);
		
		Page<Contact> contacts =this.contactRepository.findContactsByUser(user.getId(), pageable);
		
		model.addAttribute("contacts", contacts);
		model.addAttribute("currentPage",page);
		model.addAttribute("totalPages",contacts.getTotalPages());
		
		return "/normal/show_contacts";
	}
	
	//showing perticular contact details
	@RequestMapping("/{cId}/contact")
	public String showContactDetail(@PathVariable("cId") Integer cId, Model model, Principal principle)
	{
		System.out.println("Cid : "+ cId);
		Optional<Contact> contactOptional =this.contactRepository.findById(cId);
		Contact contact = contactOptional.get();
		String userName = principle.getName();
		User user = this.userRepository.getUserByUserName(userName);
		
		if(user.getId()==contact.getUser().getId())
		{
			model.addAttribute("contact",contact);
			model.addAttribute("title",contact.getName());
		}
		
		return "normal/contact_detail";
	}
	
	@GetMapping("/delete/{cId}")
	public String  deleteContact(@PathVariable("cId") Integer cId,Model model, Principal principle, HttpSession session)
	{
		
		Optional<Contact> contactOptional =this.contactRepository.findById(cId);
		Contact contact = contactOptional.get();
		String userName = principle.getName();
		User user = this.userRepository.getUserByUserName(userName);
		if(user.getId()==contact.getUser().getId())
		{
			user.getContacts().remove(contact);
			this.userRepository.save(user);
			//contact.setUser(null);
			//this.contactRepository.delete(contact);
			//session.setAttribute("message", new Message("Contact deleted successfully .. ", "success"));
		}
		
		return "redirect:/user/show-contacts/0";
	}
	
	//OpenUpdate form handler
	@PostMapping("/update-contact/{cId}")
	public String updateForm(@PathVariable("cId") Integer cId,Model model, Principal principle)
	{
		model.addAttribute("title","Update Contact");
		
		Optional<Contact> contactOptional =this.contactRepository.findById(cId);
		Contact contact = contactOptional.get();
		String userName = principle.getName();
		User user = this.userRepository.getUserByUserName(userName);
		if(user.getId()==contact.getUser().getId())
		{
			model.addAttribute("contact",contact);
		}
		
		return "/normal/update_form";
	}
	
	// update contact handler
	@RequestMapping(value = "/process-update", method = RequestMethod.POST)
	public String updateHandler(@ModelAttribute Contact contact,@RequestParam("profileImage") MultipartFile file, Model model, Principal principle,HttpSession session)
	{
		try
		{
			//old contact detail
			Contact oldcontactDetails = this.contactRepository.findById(contact.getcId()).get();
			
			if(! file.isEmpty())
			{
				//if the file is empty 
				System.out.println("file is not available");
				contact.setImage("contact.png");
				
				//delete old pic
				File deleteFile =new ClassPathResource("static/img").getFile();
				File file1 = new File(deleteFile, oldcontactDetails.getImage());
				file1.delete();
				
				
				//update new pic
				File saveFile =new ClassPathResource("static/img").getFile();
				Path path =Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

				contact.setImage(file.getOriginalFilename());
			}
			else
			{
				
				contact.setImage(oldcontactDetails.getImage());
			}
			
				User user =this.userRepository.getUserByUserName(principle.getName());
				contact.setUser(user);
				this.contactRepository.save(contact);
				
				//session.setAttribute("message", new Message("Your contact is updated...", "success"));
			
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		System.out.println("Name : "+ contact.getName());
		return "redirect:/user/"+contact.getcId()+"/contact";
	}
	
	//your profile handler
	@GetMapping("/profile")
	public String yourProfile(Model model)
	{
		model.addAttribute("title","Profile Page");
		return "/normal/profile";
	}
	
	//update your profile 
	@GetMapping("/update_profile")
	public String openSetting(Model model, Principal principle)
	{
		//model.addAttribute("title","Update Your Profile");
		//User user =this.userRepository.getUserByUserName(principle.getName());
		//System.out.println("User id : "+user.getId()+" User Name is : "+user.getName());
		
		return "/normal/settings";
	}
	//user profile update handler
	@RequestMapping(value = "/change-password", method = RequestMethod.POST)
	public String changePassword( @RequestParam("oldPassword") String oldPassword,@RequestParam("newPassword") String newPassword,Model model, Principal principle,HttpSession session)
	{
		System.out.println("OldPass : "+oldPassword);
		System.out.println("NewPass : "+newPassword);
		
		String userName= principle.getName();
		User currentUser = this.userRepository.getUserByUserName(userName);
		System.out.println(currentUser);
	
		if(this.bCryptPasswordEncoder.matches(oldPassword, currentUser.getPassword()))
		{
			//Change the password
			currentUser.setPassword(this.bCryptPasswordEncoder.encode(newPassword));
			this.userRepository.save(currentUser);
			session.setAttribute("message", new Message("Your password is change successfully .. ", "success"));
		}else
		{
			//error...
			session.setAttribute("message", new Message("Wrong Old Password", "danger"));
			return "redirect:/user/update_profile";
		}
		
		return "redirect:/user/index";
	}
	
	
	
	
	
}
