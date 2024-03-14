/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package managedbean;

import entity.Customer;
import entity.Registration;
import java.io.Serializable;
import java.util.Base64;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.context.Flash;
import javax.faces.event.ActionEvent;
import javax.persistence.NoResultException;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.file.UploadedFile;
import session.CustomerSessionBeanLocal;
import session.RegistrationSessionBeanLocal;

/**
 *
 * @author justi
 */
@Named(value = "customerManagedBean")
@SessionScoped
public class CustomerManagedBean implements Serializable {

    @EJB(name = "RegistrationSessionBeanLocal")
    private RegistrationSessionBeanLocal registrationSessionBeanLocal;

    @EJB(name = "CustomerSessionBeanLocal")
    private CustomerSessionBeanLocal customerSessionBeanLocal;
    
   
    private String name;
    private String username;
    private String password;
    private String phoneNumber;
    private String email;
    private byte[] profilePicByte;
    private String base64Image;
    private Customer customer;
    private List<Registration> regs;
    private UploadedFile file;

    /**
     * Creates a new instance of CustomerManagedBean
     */
    public CustomerManagedBean() {
    }

    public void addCustomer(ActionEvent evt) {
        FacesContext context = FacesContext.getCurrentInstance();
        Flash flash = context.getExternalContext().getFlash();
        flash.setKeepMessages(true); // Keep messages for the redirect

        Customer c = new Customer();
        c.setName(name);
        c.setUsername(username);
        c.setPassword(password);
        c.setEmail(email);
        c.setPhoneNumber(phoneNumber);
        c.setProfilePicByte(profilePicByte);

        try {
            customerSessionBeanLocal.createNewCustomer(c);
            context.addMessage("growl", new FacesMessage(FacesMessage.SEVERITY_INFO, "Registration Success", "Successfully registered"));
        } catch (Exception e) {
            context.addMessage("growl", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Registration Error", "Unable to register"));
        }
    }


    public void getCustomer(Long id) {
        try {
            Customer c = customerSessionBeanLocal.getCustomer(id);
            // System.out.println("CUSTOMER" + c.getName());
            this.customer = c;
            setUsername(c.getUsername());
            setName(c.getName());
            setEmail(c.getEmail());
            setPassword(c.getPassword());
            setPhoneNumber(c.getPhoneNumber());

            if (c.getProfilePicByte() != null) {
                this.setBase64Image("data:image/png;base64," + getImageAsBase64(c.getProfilePicByte()));
            }
        } catch (NoResultException ex) {
            Logger.getLogger(CustomerManagedBean.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public String updateProfile() {
        FacesContext context = FacesContext.getCurrentInstance();
        Flash flash = context.getExternalContext().getFlash();
        flash.setKeepMessages(true); // Keep messages for the redirect
        try {
            Customer currentUser = customerSessionBeanLocal.getCustomer(customer.getCustomerId()); // Get the current user from session

            boolean isUpdated = false; // Flag to check if anything was updated

            if (currentUser != null) {
                // Check each field for changes and update as necessary

                if (!name.equals(currentUser.getName())) {
                    currentUser.setName(name);
                    isUpdated = true;
                }

                if (!email.equals(currentUser.getEmail())) {
                    currentUser.setEmail(email);
                    isUpdated = true;
                }

                if (!phoneNumber.equals(currentUser.getPhoneNumber())) {
                    currentUser.setPhoneNumber(phoneNumber);
                    isUpdated = true;
                }

                if (file != null) {
                    byte[] newProfilePic = file.getContent();
                    currentUser.setProfilePicByte(newProfilePic);
                    this.setBase64Image("data:image/png;base64," + getImageAsBase64(newProfilePic));
                    System.out.println(this.base64Image);
                    isUpdated = true;
                }

                if (isUpdated) {
                    customerSessionBeanLocal.updateCustomer(currentUser); // Method to persist the changes
                    context.addMessage("growl",
                            new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Profile updated successfully."));
                } else {
                    context.addMessage("growl",
                            new FacesMessage(FacesMessage.SEVERITY_WARN, "No changes", "No changes detected."));
                }
            } else {
                context.addMessage("growl",
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No user logged in."));
                return null;
            }
        } catch (Exception e) {
            context.addMessage("growl",
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Update Error", "There was a problem updating the profile."));
            // Log the exception
            return null;
        }
        return "/secret/viewProfile?faces-redirect=true";
    }

    public String getImageAsBase64(byte[] profileImage) {
        return Base64.getEncoder().encodeToString(profileImage);
    }

    public List<Registration> filter() {
        regs = registrationSessionBeanLocal.retrieveAllRegistrationsWithCustomer(customer.getCustomerId());
        return regs;
    }
    
    public void upload(FileUploadEvent event) {
        file = event.getFile();
        if (file != null && file.getContent() != null) {
            try {
                byte[] content = file.getContent();
                // Here, you would typically process the file, such as saving it to the database.
                // For demonstration purposes, this example just logs the file size.
                this.profilePicByte = content;
                this.base64Image = "data:image/png;base64," + getImageAsBase64(profilePicByte);
                FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", file.getFileName() + " uploaded successfully."));
            } catch (Exception e) {
                FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Upload Failure", "Failed to upload " + file.getFileName()));
            }
        } else {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_WARN, "Warning", "No file uploaded."));
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public byte[] getProfilePicByte() {
        return profilePicByte;
    }

    public void setProfilePicByte(byte[] profilePicByte) {
        this.profilePicByte = profilePicByte;
    }

    public String getBase64Image() {
        return base64Image;
    }

    public void setBase64Image(String base64Image) {
        this.base64Image = base64Image;
    }

    public UploadedFile getFile() {
        return file;
    }

    public void setFile(UploadedFile file) {
        this.file = file;
    }

}
