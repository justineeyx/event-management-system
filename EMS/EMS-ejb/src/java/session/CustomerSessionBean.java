/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package session;

import entity.Customer;
import error.ErrorException;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;
import javax.persistence.Query;

/**
 *
 * @author justi
 */
@Stateless
public class CustomerSessionBean implements CustomerSessionBeanLocal {

    @PersistenceContext(unitName = "EMS-ejbPU")
    private EntityManager em;

    // Add business logic below. (Right-click in editor and choose
    // "Insert Code > Add Business Method")
    
    @Override
    public Customer createNewCustomer(Customer c) throws ErrorException {
       try {
           em.persist(c);
           em.flush();
           return c;
       } catch (PersistenceException ex) {
           if (ex.getCause() != null && ex.getCause().getClass().getName().equals("org.eclipse.persistence.exceptions.DatabaseException")) {
                if (ex.getCause().getCause() != null && ex.getCause().getCause().getClass().getName().equals("java.sql.SQLIntegrityConstraintViolationException")) {
                    throw new ErrorException("A customer with the same identification number exists!");
                } else {
                    throw new ErrorException(ex.getMessage());
                }
            } else {
                throw new ErrorException(ex.getMessage());
            }
        }
    }
    
    @Override
    public List<Customer> retrieveAllCustomers() 
    {
        Query query = em.createQuery("SELECT c FROM Customer c");
        return query.getResultList();
    }
    
    @Override
    public Customer getCustomer(Long customerId) throws NoResultException {
        Customer c = em.find(Customer.class, customerId);

        if (c != null) {
            return c;
        } else {
            throw new NoResultException("Not found");
        }
    }
    
    @Override
    public void updateCustomer(Customer c) throws NoResultException {
        Customer customer = getCustomer(c.getCustomerId());

        customer.setEmail(c.getEmail());
        customer.setPhoneNumber(c.getPhoneNumber());
        customer.setProfilePicByte(c.getProfilePicByte());
        customer.setName(c.getName());
    }
    
    
    @Override
    public Customer retrieveCustomerByUsername(String username) throws ErrorException {
        Query query = em.createQuery("SELECT c FROM Customer c WHERE c.username = :username");
        query.setParameter("username", username);
        
        try {
            return (Customer)query.getSingleResult();
        } catch (NoResultException | NonUniqueResultException ex) {
            throw new ErrorException("Customer username: " + username + "does not exist!\n");
        }
    }
    
    @Override
    public Customer customerLogin(String username, String password) throws ErrorException {
        
        try {
            Customer c = retrieveCustomerByUsername(username);
    
            if(c.getPassword().equals(password)) {
                return c;
            } else {
                throw new NoResultException("Username does not exist or invalid password!");
            }
        } catch(NoResultException ex) {
            throw new ErrorException("Username does not exist or invalid password!");
        }
    }
}
