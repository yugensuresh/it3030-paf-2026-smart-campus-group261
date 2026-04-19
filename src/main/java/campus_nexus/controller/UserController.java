package campus_nexus.controller;

import campus_nexus.config.MongoDocumentPreparer;
import campus_nexus.entity.User;
import campus_nexus.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MongoDocumentPreparer mongoDocumentPreparer;

    @GetMapping
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @PostMapping
    public User createUser(@RequestBody User user) {
        return userRepository.save(mongoDocumentPreparer.prepare(user));
    }
}
