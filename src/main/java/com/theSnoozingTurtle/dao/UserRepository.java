package com.theSnoozingTurtle.dao;

import com.theSnoozingTurtle.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Integer> {
}
