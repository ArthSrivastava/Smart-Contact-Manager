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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
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
import java.util.Optional;

@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    UserRepository userRepository;

    @Autowired
    ContactRepository contactRepository;

    @Autowired
    BCryptPasswordEncoder bCryptPasswordEncoder;

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
            if (!imageName.equals("contact.png")) {
                File file = new ClassPathResource("static/img").getFile();
                System.out.println(file.getAbsolutePath());
                Path path = Paths.get(file.getAbsolutePath() + File.separator + imageName);
                Files.delete(path);
            }
            //unnecessary: not causing any error in intellij
//            contact.setUser(null);
            contactRepository.delete(contact);
            session.setAttribute("message", new Message("Contact deleted successfully!", "success"));
        }
        return "redirect:/user/show-contacts/0";
    }

    //handler to show update contact form
    @PostMapping("/update-contact/{cId}")
    public String updateContact(@PathVariable("cId") int cId, Model model) {
        Contact contact = contactRepository.findById(cId).get();
        model.addAttribute("title", "Update Contact");
        model.addAttribute("contact", contact);
        return "normal/update_contact";
    }

    //handler for processing the update form
    @PostMapping("/process-update")
    public String processUpdateForm(@ModelAttribute Contact contact, @RequestParam("profileImage") MultipartFile file, Model model, HttpSession session,
                                    Principal principal) {
        try {
            Contact oldContactDetails = contactRepository.findById(contact.getcId()).get();
            if (file.isEmpty()) {
                System.out.println("IN this!");
                contact.setImage(oldContactDetails.getImage());
            } else {
                File file1 = new ClassPathResource("static/img").getFile();

                //deleting the old contact image
                Path pathToDeleteOld = Paths.get(file1.getAbsolutePath() + File.separator + oldContactDetails.getImage());
                Files.delete(pathToDeleteOld);

                //saving the new contact image
                Path path = Paths.get(file1.getAbsolutePath() + File.separator + file.getOriginalFilename());
                Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
                contact.setImage(file.getOriginalFilename());
            }
            User user = userRepository.getUserByUserName(principal.getName());
            contact.setUser(user);
            contactRepository.save(contact);
            session.setAttribute("message", new Message("Your contact has been updated", "success"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "redirect:/user/show-contacts/0";
    }


    //handler for showing the profile page of logged in user
    @GetMapping("/profile")
    public String profile(Model model) {
        model.addAttribute("title", "Profile Page");
        return "normal/profile";
    }

    //handler for settings
    @GetMapping("/settings")
    public String settings(Model model) {
        model.addAttribute("title", "User Settings");
        return "normal/settings";
    }

    //change password module
    @PostMapping("/change-password")
    public String changePassword(@RequestParam("oldPassword") String oldPassword,
                                 @RequestParam("newPassword") String newPassword,
                                 Principal principal, HttpSession session) {
//        System.out.println(oldPassword);
//        System.out.println(newPassword);
        String username = principal.getName();
        User user = this.userRepository.getUserByUserName(username);

        if (bCryptPasswordEncoder.matches(oldPassword, user.getPassword())) {
            //change the password
            user.setPassword(this.bCryptPasswordEncoder.encode(newPassword));
            this.userRepository.save(user);
            session.setAttribute("message", new Message("Password changed successfully", "success"));
        } else {
//            error....
            session.setAttribute("message", new Message("Please enter correct old password", "danger"));
            return "redirect:/user/settings";
        }
        return "redirect:/user/index";
    }

}
