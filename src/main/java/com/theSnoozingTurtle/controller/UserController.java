package com.theSnoozingTurtle.controller;

import com.theSnoozingTurtle.dao.UserRepository;
import com.theSnoozingTurtle.entities.Contact;
import com.theSnoozingTurtle.entities.User;
import com.theSnoozingTurtle.helper.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpSession;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;

@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    UserRepository userRepository;

    //This method will run always
    @ModelAttribute
    public void addCommonData(Model model, Principal principal) {
        //email is used as username in the application
        String email = principal.getName();
        User user = userRepository.getUserByUserName(email);
        model.addAttribute("user", user);
    }

    @RequestMapping("/index")
    public String dashboard(Model model, Principal principal) {
        model.addAttribute("title", "User Dashboard");
        return "normal/user_dashboard";
    }

    @GetMapping("/add-contacts")
    public String openAddContactForm(Model model) {
        model.addAttribute("title", "Add Contact");
        model.addAttribute("contact", new Contact());
        return "normal/add-contact-form";
    }

    @PostMapping("/process-contact")
    public String processContact(@ModelAttribute Contact contact,
                                 @RequestParam("profileImage") MultipartFile file,
                                 Principal principal,
                                 HttpSession session) {

        try {
            User user = userRepository.getUserByUserName(principal.getName());
            //processing the contact image
            if (file.isEmpty()) {
                System.out.println("Empty file!");
            } else {
                contact.setImage(file.getOriginalFilename());
                File saveFile = new ClassPathResource("static/img").getFile();
                Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + file.getOriginalFilename());
                Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("Image is uploaded!");
            }
            contact.setUser(user);
            user.getContacts().add(contact);
            userRepository.save(user);
            System.out.println("Data:" + contact);
            session.setAttribute("message", new Message("Contact added successfully!", "success"));

        } catch (Exception e) {
            System.out.println("Error:" + e.getMessage());
            session.setAttribute("message", new Message("Something went wrong", "danger"));
        }
        return "normal/add-contact-form";
    }

    @GetMapping("/show-contacts")
    public String showContacts(Model model) {
        model.addAttribute("title", "Show Contacts");
        return "normal/show-contacts";
    }
}
