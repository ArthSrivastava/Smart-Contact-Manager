package com.theSnoozingTurtle.controller;

import com.theSnoozingTurtle.dao.UserRepository;
import com.theSnoozingTurtle.entities.User;
import com.theSnoozingTurtle.helper.Message;
import com.theSnoozingTurtle.service.EmailWebService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import java.util.Random;

@Controller
public class ForgotPasswordController {
    Random random = new Random();

    @Autowired
    private EmailWebService emailWebService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @GetMapping("/forgot")
    public String forgotPassword() {
        return "forgot_password";
    }

    @PostMapping("/send-otp")
    public String sendOtp(@RequestParam("email") String email, HttpSession session) {
        User user = userRepository.getUserByUserName(email);
        if (user == null) {
            session.setAttribute("message", "No user exists with this Email");
            return "forgot_password";
        }
        System.out.println("email:" + email);
        int otp = random.nextInt(1000000);
        System.out.println("OTP:" + otp);

        //send the otp to email
        String subject = "OTP for Smart Contact Manager";
        String to = email;
        String message =
                "<div style = 'border: 1px solid #e2e2e2; padding: 20px;'>" +
                        "<h1>" +
                        "OTP is " +
                        "<b>" +
                        otp +
                        "</b>" +
                        "</div>";

        boolean res = this.emailWebService.sendEmail(subject, message, to);
        session.setAttribute("serverotp", otp);
        session.setAttribute("email", email);
        return "verify_otp";
    }

    @PostMapping("/verify-otp")
    public String verifyOtp(@RequestParam("otp") int enteredOtp, HttpSession session) {
        int serverOtp = (int) session.getAttribute("serverotp");
        String email = (String) session.getAttribute("email");
        if (serverOtp == enteredOtp) {
            //otp entered correctly
            User user = userRepository.getUserByUserName(email);

            return "password_change_form";
        } else {
            session.setAttribute("message", "Wrong OTP entered");
            return "verify_otp";
        }
    }

    @PostMapping("/change-password")
    public String changePassword(@RequestParam("newPassword") String newPassword, HttpSession session) {
        String email = (String) session.getAttribute("email");
        User user = this.userRepository.getUserByUserName(email);
        user.setPassword(this.bCryptPasswordEncoder.encode(newPassword));
        this.userRepository.save(user);
        return "redirect:/signin?change=Password Changed Successfully!";
    }
}
