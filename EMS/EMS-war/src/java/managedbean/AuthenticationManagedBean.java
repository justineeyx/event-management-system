package managedbean;

import entity.Customer;
import error.ErrorException;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import javax.ejb.EJB;
import javax.inject.Inject;
import session.CustomerSessionBeanLocal;

@Named(value = "authenticationManagedBean")
@SessionScoped
public class AuthenticationManagedBean implements Serializable {

    @EJB(name = "CustomerSessionBeanLocal")
    private CustomerSessionBeanLocal customerSessionBeanLocal;
    
    @Inject
    private CustomerManagedBean customerManagedBean;

    private String username = null;
    private String password = null;
    private long userId = -1;

    public AuthenticationManagedBean() {
    }

    public String login() throws ErrorException {
        //by right supposed to use a session bean to
        //do validation here
        //...
        //simulate username/password
        try {
            Customer c = customerSessionBeanLocal.customerLogin(username, password);
            userId = c.getCustomerId();
            customerManagedBean.getCustomer(userId);
            return "/secret/home.xhtml?faces-redirect=true";
        } catch(ErrorException ex) {
            throw new ErrorException("Username does not exist or invalid password!");
        }
    }

    public String logout() {
        username = null;
        password = null;
        userId = -1;
        return "/login.xhtml?faces-redirect=true";
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    
}
