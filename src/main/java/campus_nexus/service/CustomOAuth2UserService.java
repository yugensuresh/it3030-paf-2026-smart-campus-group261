package campus_nexus.service;

import campus_nexus.entity.User;
import campus_nexus.enums.Role;
import campus_nexus.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private static final Logger logger = LoggerFactory.getLogger(CustomOAuth2UserService.class);

    @Autowired
    private UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        Map<String, Object> attributes = oAuth2User.getAttributes();
        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");

        logger.info("OAuth2 login attempt for email: {}", email);

        // Check if user exists, if not create new user
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> createNewUser(email, name));

        logger.info("OAuth2 login successful for user: {}, role: {}", email, user.getRole());

        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())),
                attributes,
                "email"
        );
    }

    private User createNewUser(String email, String name) {
        logger.info("Creating new user with email: {}", email);

        User newUser = new User();
        newUser.setEmail(email);
        newUser.setName(name);
        newUser.setRole(Role.USER);

        User savedUser = userRepository.save(newUser);
        logger.info("New user created with ID: {}", savedUser.getId());

        return savedUser;
    }
}