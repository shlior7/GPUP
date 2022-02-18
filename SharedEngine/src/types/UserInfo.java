package types;

import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
public class UserInfo {
    private final String userName;
    private final String role;

    public UserInfo(IUser userToReturn) {
        this.userName = userToReturn.getName();
        this.role = userToReturn.getRole();
    }
}