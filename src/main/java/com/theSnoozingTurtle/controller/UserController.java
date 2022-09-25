package com.theSnoozingTurtle.controller;

import com.theSnoozingTurtle.dao.ContactRepository;
import com.theSnoozingTurtle.dao.UserRepository;
import com.theSnoozingTurtle.entities.Contact;
import com.theSnoozingTurtle.entities.User;
import com.theSnoozingTurtle.helper.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpSession;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    UserRepository userRepository;

    @Autowired
    ContactRepository contactRepository;

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
                contact.setImage("contact.png");
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

    @GetMapping("/show-contacts/{page}")
    public String showContacts(@PathVariable("page") int page, Model model, Principal principal) {
        String email = principal.getName();
        User user = userRepository.getUserByUserName(email);

        //Pageable stores page properties:
        //current page
        //contact per page
        Pageable pageable = PageRequest.of(page, 5);
        Page<Contact> contacts = contactRepository.findContactsByUserId(user.getId(), pageable);
        model.addAttribute("title", "Show Contacts");
        model.addAttribute("contacts", contacts);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", contacts.getTotalPages());
        return "normal/show-contacts";
    }

    //showing particular contact
    @GetMapping("/contact/{cId}")
    public String showParticularContact(@PathVariable("cId") int cId, Model model, Principal principal) {
        model.addAttribute("title", "Contact Details");
        String name = principal.getName();
        User user = userRepository.getUserByUserName(name);
        Optional<Contact> contactOptional = contactRepository.findById(cId);
        Contact contact = contactOptional.get();
        if (user.getId() == contact.getUser().getId()) {
            model.addAttribute("contact", contact);
        }
        return "normal/contact_detail";
    }

    //deleting the contact
    @GetMapping("/delete/{cId}")
    public String delete(@PathVariable("cId") int cId, Model model, Principal principal, HttpSession session) throws IOException {
        //Fetching the contact
        Optional<Contact> contactOptional = contactRepository.findById(cId);
        Contact contact = contactOptional.get();

        //Fetching the user
        String name = principal.getName();
        User user = userRepository.getUserByUserName(name);

        if (user.getId() == contact.getUser().getId()) {
            //deleting the contact's profile image from the target folder
            String imageName = contact.getImage();
            if(!imageName.equals("contact.png")) {
                File file = new ClassPathResource("static/img").getFile();
                System.out.println(file.getAbsolutePath());
                Path path = Paths.get(file.getAbsolutePath() + File.separator + imageName);
                Files.delete(path);
            }
            contact.setUser(null);
            contactRepository.delete(contact);
            session.setAttribute("message", new Message("Contact deleted successfully!", "success"));
        }
        return "redirect:/user/show-contacts/0";
    }
}
