package org.isfce.pid.service;

import java.util.List;
import java.util.Optional;

import org.isfce.pid.dao.IUserDao;
import org.isfce.pid.dto.UserDto;
import org.isfce.pid.model.User;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

// L'analyse null d'Eclipse génère des faux positifs sur les retours de Spring Data JPA
// (save(), findById()…) dont les @NonNull ne sont pas toujours lus depuis le classpath.
@SuppressWarnings("null")
@Transactional
@Service
public class UserService {
	IUserDao daoUser;

	public UserService(IUserDao daoUser) {
		this.daoUser = daoUser;
	}

	public List<UserDto> getAllUserDto() {
		return daoUser.getAllUserDto();
	}

	public Optional<User> getUserById(String username) {
		return daoUser.findById(username);
	}

	public User addUser(User user) {
		return daoUser.save(user);
	}

	public boolean existByUsername(String username) {
		return daoUser.existsById(username);
	}

}
