package com.b2c.Local.B2C.auths.service;

import com.b2c.Local.B2C.auths.dao.UserRepository;
import com.b2c.Local.B2C.auths.dao.UserSecurityDetailsRepository;
import com.b2c.Local.B2C.auths.dto.LoginDto;
import com.b2c.Local.B2C.auths.dto.UserDto;
import com.b2c.Local.B2C.auths.enums.Role;
import com.b2c.Local.B2C.auths.model.User;
import com.b2c.Local.B2C.auths.model.UserSecurityDetails;
import com.b2c.Local.B2C.exception.*;
import com.b2c.Local.B2C.store.service.LocalStoreService;
import com.b2c.Local.B2C.utility.UserMacAddress;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@Service
public class UserService implements UserDetailsService {


    UserRepository userRepository;

    AuthenticationManager authenticationManager;

    DaoAuthenticationProvider authenticationProvider;

    FindByIndexNameSessionRepository<? extends Session> sessions;

    UserSecurityDetailsRepository userSecurityDetailsRepository;

    LocalStoreService localStoreService;

    UserMacAddress userMacAddress;

    @Autowired
    public UserService(@Lazy UserRepository userRepository, @Lazy AuthenticationManager authenticationManager,
                       @Lazy DaoAuthenticationProvider authenticationProvider,
                       @Lazy FindByIndexNameSessionRepository<? extends Session> sessions,
                       @Lazy UserSecurityDetailsRepository userSecurityDetailsRepository, @Lazy LocalStoreService localStoreService, @Lazy UserMacAddress userMacAddress) {
        this.userRepository = userRepository;
        this.authenticationManager = authenticationManager;
        this.authenticationProvider = authenticationProvider;
        this.sessions = sessions;
        this.userSecurityDetailsRepository = userSecurityDetailsRepository;
        this.localStoreService = localStoreService;
        this.userMacAddress = userMacAddress;
    }


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(username);
        if (user == null) {
            throw new UsernameNotFoundException("User with " + username + " does not exits");
        }
        return user;
    }

    public User addUser(UserDto userDto, HttpServletRequest httpServletRequest) throws IOException {
        User user = new User();
        if (!Objects.isNull(httpServletRequest.getUserPrincipal()))
            throw new Unauthorized401Exception("You are UnAuthorized");
        if (userDto.getEmail() != null) {
            if (!userRepository.existsByEmail(userDto.getEmail())) {
                user.setFirstName(userDto.getFirstName());
                user.setEmail(userDto.getEmail());
                user.setLastName(userDto.getLastName());
                user.setMobileNo(userDto.getMobileNumber());
                user.setStoreOwner(userDto.isStoreOwner());
                user.setUsername(userDto.getUsername());
                LocalDateTime localDateTime = LocalDateTime.now();
                user.setCreateDate(localDateTime);
                BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
                if (userDto.getNewPassword().equals(userDto.getConfirmPassword())) {
                    user.setPassword(bCryptPasswordEncoder.encode(userDto.getNewPassword()));
                } else {
                    throw new Conflict409Exception("New password and confirm password is not match!");
                }
                if (userDto.isStoreOwner()) {
                    user.setRole(Role.STORE_OWNER);
                } else {
                    user.setRole(Role.USER);
                }
                userRepository.save(user);
                UserSecurityDetails userSecurityDetails = new UserSecurityDetails();
                userSecurityDetails.setUser(userRepository.findByEmail(userDto.getEmail()));
                userSecurityDetails.setMaxSession(userDto.getMaxSession());
                userSecurityDetails.setMacAddress(userMacAddress.arpByRemoteIp(httpServletRequest.getRemoteAddr()));
                userSecurityDetailsRepository.save(userSecurityDetails);
            } else {
                throw new Conflict409Exception("Email already exists!");
            }
        }
        return user;
    }

//    public String loginUser(LoginDto loginDto){
//        if (loginDto.getEmail() != null && loginDto.getPassword() != null){
//            if (userRepository.existsByEmail(loginDto.getEmail())) {
//                //LocalDateTime localDateTime = LocalDateTime.now();
//                Authentication authentication = authenticationProvider.authenticate(
//                        new UsernamePasswordAuthenticationToken(
//                                loginDto.getEmail(),
//                                loginDto.getPassword()));
//                SecurityContextHolder.getContext().setAuthentication(authentication);
//            } else throw new BadCredentialsException("Invalid email!");
//        }
//        else throw new BadCredentialsException("Invalid email and password!");
//        return "Login Successfully!";
//    }

    public String loginUser(LoginDto loginDto) {
        if (loginDto.getEmail() != null && loginDto.getPassword() != null) {
            BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
            if (!userRepository.existsByEmailAndIsActiveTrue(loginDto.getEmail())) {
                throw new NotFound404Exception("User not found OR Enter Valid Credentials");
            }
            if (validateSession(loginDto.getEmail())) {
                Authentication authentication = authenticationProvider.authenticate(new UsernamePasswordAuthenticationToken(loginDto.getEmail(), loginDto.getPassword()));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } else {
            throw new BadRequest400Exception("Enter Email and Password");
        }
        return "Success Logged In";
    }

    /*public Map<String, String> getToken(LoginDto loginDto) {
        if (validateSession(loginDto.getEmail())) {
            Authentication authentication = authenticationProvider.authenticate(new UsernamePasswordAuthenticationToken(loginDto.getEmail(), loginDto.getPassword()));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            User user = userRepository.findByEmail(loginDto.getEmail());
            Algorithm algorithm = Algorithm.HMAC512(user.getPassword());
            JwtToken jwtToken = new JwtToken();
            jwtToken.setActive(true);
            jwtToken.setCreateDate(new Date());
            jwtToken.setUser(user);
            jwtToken.setExpireDate(Date.from(Instant.now().plusSeconds(1000)));
            jwtToken.setToken(JWT.create().withIssuer("local-b2c").withIssuedAt(jwtToken.getCreateDate()).withExpiresAt(jwtToken.getExpireDate()).withSubject(user.getEmail()).sign(algorithm));
            jwtTokenRepository.save(jwtToken);
            return new HashMap<String, String>(Map.of("token", jwtToken.getToken()));
        } else {
            throw new BadRequest400Exception("Something Went Wrong");
        }
    }*/

    public boolean validateSession(String email) {
        int userSessionLimit = userSecurityDetailsRepository.findByUserEmail(email).getMaxSession();
        if (this.sessions.findByPrincipalName(email).entrySet().size() < userSessionLimit) {
            return true;
        } else {
            throw new Forbidden403Exception("Your Maximum Token Limit Exceed , Logout Any Session For Logged In");
        }
    }

    private Object getLoggedInUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (Objects.nonNull(principal)) {
            return principal;
        }
        return null;
    }

    public String updateUser(UserDto userDto) {
        User user = new User();
        if (getLoggedInUserId() != null) {
            user = userRepository.findById(((User )getLoggedInUserId()).getId()).get();
            if (userDto.getFirstName() != null) {
                user.setFirstName(userDto.getFirstName());
            }
            if (userDto.getLastName() != null) {
                user.setLastName(userDto.getLastName());
            }
            if (Objects.isNull(((Long) userDto.getMobileNumber()) != null)) {
                user.setMobileNo(userDto.getMobileNumber());
            }
            BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
            if (userDto.getCurrentPassword() != null) {
                if (bCryptPasswordEncoder.matches(userDto.getCurrentPassword(), user.getPassword()) && userDto.getNewPassword().equals(userDto.getConfirmPassword())) {
                    user.setPassword(userDto.getNewPassword());
                }
            }
            if (!Objects.isNull(userDto.getMaxSession())) {
                userSecurityDetailsRepository.findByUserEmail(user.getEmail()).setMaxSession(userDto.getMaxSession());
            }
            userRepository.save(user);
        }
        return "Successfully Updated!";
    }

    public String deleteUser() {
        if (userRepository.findById(((User ) Objects.requireNonNull(getLoggedInUserId())).getId()).isPresent()) {
            userRepository.findById(((User )getLoggedInUserId()).getId()).get().setIsActive(false);
            return "Deleted Successfully";
        } else {
            return "Not Deleted";
        }
    }

    public User get() {
        if (getLoggedInUserId() instanceof User){
            return userRepository.findById(((User )getLoggedInUserId()).getId()).get();
        }else {
            return null;
        }
    }

    public String changePassword(UserDto userDto, UUID uuid) {
        if (!uuid.equals(getLoggedInUserId())) {
            throw new Forbidden403Exception("You Are Not Allowed");
        }
        if (userDto.getCurrentPassword().equals(userDto.getNewPassword())) {
            throw new Conflict409Exception("Current Password and New Password Not Be Same");
        }
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        if (bCryptPasswordEncoder.matches(userDto.getCurrentPassword(), userRepository.findById(uuid).get().getPassword()) && userDto.getNewPassword().equals(userDto.getConfirmPassword())) {
            User user = userRepository.findById(uuid).get();
            user.setPassword(bCryptPasswordEncoder.encode(userDto.getNewPassword()));
            userRepository.save(user);
            return "Password Changed";
        } else {
            throw new BadRequest400Exception("Enter Valid Password");
        }
    }
}
