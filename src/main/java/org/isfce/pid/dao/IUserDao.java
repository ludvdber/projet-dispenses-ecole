package org.isfce.pid.dao;

import org.isfce.pid.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * DAO Spring Data JPA pour l'entité User.
 * @author Ludovic
 */
public interface IUserDao extends JpaRepository<User, String> {
}
