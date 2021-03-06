package eu.cloudteams.repository.service;


import eu.cloudteams.repository.dao.UserRepository;
import eu.cloudteams.repository.domain.SonarqubeUser;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author Christos Paraskeva <ch.paraskeva at gmail dot com>
 */
@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    private static final Logger logger = Logger.getLogger(UserService.class.getName());

    public boolean storeUser(SonarqubeUser user) {
        try {
            userRepository.save(user);
        } catch (Exception ex) {
            logger.severe(ex.getMessage());
            return false;
        }
        return user.getId() > 0;
    }

    public boolean deleteUser(long id) {
        try {
            userRepository.delete(id);
        } catch (Exception ex) {
            Logger.getLogger(UserService.class.getName()).severe(ex.getMessage());
            return false;
        }
        return true;
    }

    public SonarqubeUser fetchUserById(long id) {
        return userRepository.findOne(id);
    }

    /**
     * Fetch a user from database , given a username and a password
     *
     * @param username The username
     * @return A User object (null if no user is found)
     */
    public SonarqubeUser findByUsername(String username) {
        return userRepository.findByUsername(username);
    }



}
