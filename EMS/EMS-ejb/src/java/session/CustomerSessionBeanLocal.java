/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package session;

import entity.Customer;
import error.ErrorException;
import java.util.List;
import javax.ejb.Local;
import javax.persistence.NoResultException;

/**
 *
 * @author justi
 */
@Local
public interface CustomerSessionBeanLocal {

    public Customer createNewCustomer(Customer c) throws ErrorException;

    public Customer getCustomer(Long customerId) throws NoResultException;

    public void updateCustomer(Customer c) throws NoResultException;

    public Customer retrieveCustomerByUsername(String username) throws ErrorException;

    public Customer customerLogin(String username, String password) throws ErrorException;

    public List<Customer> retrieveAllCustomers();
    
}
