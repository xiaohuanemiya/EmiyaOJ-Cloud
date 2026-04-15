import com.emiyaoj.auth.AuthApplication;
import com.emiyaoj.auth.domain.pojo.User;
import com.emiyaoj.auth.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

@SpringBootTest(classes = AuthApplication.class)
public class UserInitTest {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    public void initAdminTestUser() {
        User user = new User();
        user.setUsername("admintestuser");
        user.setPassword(passwordEncoder.encode("123456")); // 默认密码
        user.setNickname("Admin Test User");
        user.setStatus(1);
        user.setDeleted(0);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());

        userMapper.insert(user);
        System.out.println("User admintestuser created with id: " + user.getId());
    }
}
