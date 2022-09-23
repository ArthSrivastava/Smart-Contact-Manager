package com.theSnoozingTurtle.controller;

import com.theSnoozingTurtle.dao.UserRepository;
import com.theSnoozingTurtle.entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;

@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    UserRepository userRepository;
    @RequestMapping("/index")
    public String dashboard(Model model, Principal principal) {
        //email is used as username in the application
        String email = principal.getName();
        User user = userRepository.getUserByUserName(email);
        model.addAttribute("user", user);
        return "normal/user_dashboard";
    }
}
