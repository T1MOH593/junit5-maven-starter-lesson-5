package com.dmdev.junit.dao;

import java.util.HashMap;
import java.util.Map;

public class UserDaoMock extends UserDao {

    private final UserDao userDao;
    private Map<Integer, Boolean> answers = new HashMap<>();

    public UserDaoMock(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public boolean delete(Integer userId) {
        return answers.getOrDefault(userId, userDao.delete(userId));
    }
}
