/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package managedbean;

import entity.Customer;
import entity.Registration;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
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
import javax.servlet.ServletContext;
import javax.servlet.http.Part;
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
    private String base64Image;
    private Customer customer;
    private List<Registration> regs;
    private Part uploadedfile;
    private String filename = "";

    /**
     * Creates a new instance of CustomerManagedBean
     */
    public CustomerManagedBean() {
    }

    public void addCustomer(ActionEvent evt) throws IOException {
        FacesContext context = FacesContext.getCurrentInstance();
        Flash flash = context.getExternalContext().getFlash();
        flash.setKeepMessages(true); // Keep messages for the redirect

        Customer c = new Customer();
        c.setName(name);
        c.setUsername(username);
        c.setPassword(password);
        c.setEmail(email);
        c.setPhoneNumber(phoneNumber);
        // c.setUploadedFile(uploadedfile);
        
        if (filename != null) {
            c.setFilename(filename);
        }
        
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
            if (c.getFilename() != null) {
                setFilename(c.getFilename());
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
                
                if (!filename.equals(currentUser.getFilename())) {
                    currentUser.setFilename(filename);
                    System.out.println("Helllo");
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

    public List<Registration> filter() {
        regs = registrationSessionBeanLocal.retrieveAllRegistrationsWithCustomer(customer.getCustomerId());
        return regs;
    }
    
    public void upload() throws IOException {
        // ServletContext ctx = (ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();

        //get the deployment path
        String UPLOAD_DIRECTORY = "C:/Users/justi/OneDrive/Documents/NetBeans/event-management-system/EMS/EMS-war/web/upload/";
        System.out.println("#UPLOAD_DIRECTORY : " + UPLOAD_DIRECTORY);
        System.out.println(uploadedfile);

        //debug purposes
        setFilename(Paths.get(uploadedfile.getSubmittedFileName()).getFileName().toString());
        System.out.println("filename: " + getFilename());
        System.out.println(filename);
        //---------------------
        
        //replace existing file
        Path path = Paths.get(UPLOAD_DIRECTORY + getFilename());
        InputStream bytes = uploadedfile.getInputStream();
        Files.copy(bytes, path, StandardCopyOption.REPLACE_EXISTING);
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

    public String getBase64Image() {
        return base64Image;
    }

    public void setBase64Image(String base64Image) {
        this.base64Image = base64Image;
    }

    public Part getUploadedfile() {
        return uploadedfile;
    }

    public void setUploadedfile(Part uploadedfile) {
        this.uploadedfile = uploadedfile;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }
    
}
