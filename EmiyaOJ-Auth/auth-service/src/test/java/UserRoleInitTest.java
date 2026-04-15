import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.emiyaoj.auth.AuthApplication;
import com.emiyaoj.auth.domain.pojo.Role;
import com.emiyaoj.auth.domain.pojo.User;
import com.emiyaoj.auth.domain.pojo.UserRole;
import com.emiyaoj.auth.mapper.RoleMapper;
import com.emiyaoj.auth.mapper.UserMapper;
import com.emiyaoj.auth.mapper.UserRoleMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;

@SpringBootTest(classes = AuthApplication.class)
public class UserRoleInitTest {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private UserRoleMapper userRoleMapper;

    @Test
    public void initAdminTestUserRole() {
        // Find user
        QueryWrapper<User> userQw = new QueryWrapper<>();
        userQw.eq("username", "admintestuser");
        User user = userMapper.selectOne(userQw);

        if (user == null) {
            System.out.println("User admintestuser not found!");
            return;
        }

        // Find role
        QueryWrapper<Role> roleQw = new QueryWrapper<>();
        roleQw.eq("role_code", "ROLE_ADMIN");
        Role role = roleMapper.selectOne(roleQw);

        if (role == null) {
            System.out.println("Role ROLE_ADMIN not found!");
            return;
        }

        // Bind user and role
        UserRole userRole = new UserRole();
        userRole.setUserId(user.getId());
        userRole.setRoleId(role.getId());
        // Check if already bounded
        QueryWrapper<UserRole> urQw = new QueryWrapper<>();
        urQw.eq("user_id", user.getId()).eq("role_id", role.getId());
        if (userRoleMapper.selectCount(urQw) > 0) {
            System.out.println("User admintestuser already mapped to ROLE_ADMIN!");
            return;
        }

        userRole.setCreateTime(LocalDateTime.now());
        userRoleMapper.insert(userRole);
        System.out.println("Bound user admintestuser to role ROLE_ADMIN successfully!");
    }
}
