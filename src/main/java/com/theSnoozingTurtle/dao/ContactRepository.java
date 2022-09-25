package com.theSnoozingTurtle.dao;

import com.theSnoozingTurtle.entities.Contact;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContactRepository extends JpaRepository<Contact, Integer> {

    //pagination..
    //Pageable stores page properties:
    //current page
    //contact per page
    public Page<Contact> findContactsByUserId(int userId, Pageable pageable);
}
