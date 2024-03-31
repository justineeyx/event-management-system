package managedbean;

import entity.Customer;
import error.ErrorException;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import javax.ejb.EJB;
import javax.faces.context.FacesContext;
import javax.faces.context.Flash;
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
//        FacesContext context = FacesContext.getCurrentInstance();
//        Flash flash = context.getExternalContext().getFlash();
//        flash.setKeepMessages(true); // Keep messages for the redirect

        try {
            Customer c = customerSessionBeanLocal.customerLogin(username, password);
            userId = c.getCustomerId();
            customerManagedBean.getCustomer(userId);
            // context.addMessage("growl", new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Successfully logged in"));
            return "/secret/home.xhtml?faces-redirect=true";
        } catch (ErrorException ex) {
            // context.addMessage("growl", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Login Error", "Username does not exist or invalid password!"));
            return null; // Stay on the same page to display the error
        }
    }
    
    public String logout() {
        username = null;
        password = null;
        userId = -1;
        customerManagedBean.setName(null);
        customerManagedBean.setUsername(null);
        customerManagedBean.setPassword(null);
        customerManagedBean.setEmail(null);
        customerManagedBean.setPhoneNumber(null);
        customerManagedBean.setFilename(null);
        return "/index.xhtml?faces-redirect=true";
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
