package miaosha.service;

import miaosha.dao.UserDao;
import miaosha.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

	@Autowired
	private UserDao userDao ;
	
	public User getById(int id) {
		return userDao.getById(id);
	}



}
