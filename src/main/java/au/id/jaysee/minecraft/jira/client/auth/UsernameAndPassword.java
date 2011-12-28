package au.id.jaysee.minecraft.jira.client.auth;

/**
 * Created by IntelliJ IDEA.
 * User: jclark
 * Date: 28/12/11
 * Time: 3:07 PM
 * To change this template use File | Settings | File Templates.
 */
public class UsernameAndPassword {
    private final String username;
    private final String password;

    public UsernameAndPassword(final String username, final String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

}
