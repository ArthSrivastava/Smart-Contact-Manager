package com.theSnoozingTurtle.controller;

import com.theSnoozingTurtle.dao.UserRepository;
import com.theSnoozingTurtle.entities.User;
import com.theSnoozingTurtle.helper.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

@Controller
public class HomeController {

    @Autowired
    BCryptPasswordEncoder passwordEncoder;
    UserRepository userRepository;

    public HomeController(@Autowired UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    //handler for home page
    @RequestMapping("/")
    public String home(Model model) {
        model.addAttribute("title", "Home - Smart Contact Manager");
        return "home";
    }

    //handler for about page
    @RequestMapping("/about")
    public String about(Model model) {
        model.addAttribute("title", "About - Smart Contact Manager");
        return "about";
    }

    //handler to show the signup form
    @RequestMapping("/signup")
    public String signup(Model model) {
        model.addAttribute("title", "Register - Smart Contact Manager");
        model.addAttribute("user", new User());
        return "signup";
    }

    //handler for registering the user
    @PostMapping("/register_user")
    public String registerUser(@Valid @ModelAttribute("user") User user, BindingResult bindingResult,
                               @RequestParam(value = "agreement", defaultValue = "false") boolean agreement,
                               Model model, HttpSession session) {

        try {
            if (!agreement) {
                model.addAttribute("user", user);
                throw new RuntimeException("You must accept the terms and conditions!");
            }

            if (bindingResult.hasErrors()) {
                System.out.println(bindingResult.toString());
                model.addAttribute("user", user);
                return "signup";
            }
            user.setRole("ROLE_USER");
            user.setEnabled(true);
            user.setImageUrl("Default.png");
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            userRepository.save(user);
            model.addAttribute("user", new User());
            session.setAttribute("message", new Message("User created successfully!", "alert-success"));

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("user", new User());
            session.setAttribute("message", new Message("Something went wrong: " + e.getMessage(), "alert-danger"));
        }
        return "signup";
    }

    @GetMapping("/signin")
    public String login(Model model) {
        model.addAttribute("title", "Login Page");
        return "login";
    }
}
