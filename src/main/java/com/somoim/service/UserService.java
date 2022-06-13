package com.somoim.service;

import com.somoim.model.dao.User;
import com.somoim.model.dto.LoginUser;
import com.somoim.model.dto.ResignUser;
import com.somoim.model.dto.SignUpUser;
import com.somoim.exception.DuplicateEmailException;
import com.somoim.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncorder;
    private final HttpSession httpSession;
    private final String USER_EMAIL = "USER_EMAIL";

    @Transactional
    public void insertUser(SignUpUser user) {
        if(checkEmail(user.getEmail())) {
            throw new DuplicateEmailException("This email already registered.");
        }
        User newUser = User.signUpUser()
                .email(user.getEmail())
                        .password(passwordEncorder.encode(user.getPassword()))
                        .createAt(LocalDateTime.now())
                        .build();
        userMapper.createUser(newUser);
    }

    @Transactional(readOnly = true)
    public boolean checkEmail(String email) {
        return userMapper.isExistsEmail(email);
    }

    @Transactional
    public void deleteUser(ResignUser user) {
        User resignUser = User.resignUser()
                .email(user.getEmail())
                .modifyAt(LocalDateTime.now())
                .build();
        userMapper.deleteUser(resignUser);
    }

    @Transactional(readOnly = true)
    public User findUserByEmail(String email) {
        return userMapper.findUserByEmail(email);
    }

    public void loginUser(LoginUser loginUser) {
       User user = findUserByEmail(loginUser.getEmail());
        if(passwordEncorder.matches(loginUser.getPassword(), user.getPassword())) {
            httpSession.setAttribute(USER_EMAIL, user.getEmail());
        }
        else
            throw new IllegalArgumentException("The password is invalid.");
    }

    public void logoutUser() {
        httpSession.removeAttribute(USER_EMAIL);
    }

    public boolean checkLogin()
    {
        if(httpSession.getAttribute(USER_EMAIL) == null)
            return false;
        else
            return true;
    }
}
