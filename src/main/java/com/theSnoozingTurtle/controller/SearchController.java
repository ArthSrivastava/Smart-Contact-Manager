package com.theSnoozingTurtle.controller;

import com.theSnoozingTurtle.dao.ContactRepository;
import com.theSnoozingTurtle.dao.UserRepository;
import com.theSnoozingTurtle.entities.Contact;
import com.theSnoozingTurtle.entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
public class SearchController {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ContactRepository contactRepository;

    @GetMapping("/search/{query}")
    private ResponseEntity<?> search(@PathVariable("query") String query, Principal principal) {
        User user = userRepository.getUserByUserName(principal.getName());
        List<Contact> contacts = contactRepository.findByNameContainingAndUser(query, user);
        return ResponseEntity.ok(contacts);
    }
}
