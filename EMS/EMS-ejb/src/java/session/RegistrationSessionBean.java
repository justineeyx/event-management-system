/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package session;

import entity.Customer;
import entity.Event;
import entity.Registration;
import error.ErrorException;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;
import javax.persistence.Query;

/**
 *
 * @author justi
 */
@Stateless
public class RegistrationSessionBean implements RegistrationSessionBeanLocal {

    @PersistenceContext(unitName = "EMS-ejbPU")
    private EntityManager em;

    // Add business logic below. (Right-click in editor and choose
    // "Insert Code > Add Business Method")
    
    @Override
    public Registration createNewRegistration(Registration r) throws ErrorException {
         try {
           em.persist(r);
           em.flush();
           return r;
       } catch (PersistenceException ex) {
           if (ex.getCause() != null && ex.getCause().getClass().getName().equals("org.eclipse.persistence.exceptions.DatabaseException")) {
                if (ex.getCause().getCause() != null && ex.getCause().getCause().getClass().getName().equals("java.sql.SQLIntegrityConstraintViolationException")) {
                    throw new ErrorException("A registration with the same identification number exists!");
                } else {
                    throw new ErrorException(ex.getMessage());
                }
            } else {
                throw new ErrorException(ex.getMessage());
            }
        }
    }
    
    @Override
    public Registration getRegistration(Long regId) throws NoResultException {
        Registration r = em.find(Registration.class, regId);

        if (r != null) {
            return r;
        } else {
            throw new NoResultException("Not found");
        }
    }
    
    @Override
    public List<Registration> retrieveAllRegistrations() 
    {
        Query query = em.createQuery("SELECT r FROM Registration r");
        return query.getResultList();
    }
    
    @Override
    public List<Registration> retrieveAllRegistrationsWithCustomer(long cId) 
    {
        Query query = em.createQuery("SELECT r FROM Registration r WHERE r.customer.customerId = :cId");
        query.setParameter("cId", cId);
        return query.getResultList();
    }
    
    @Override
    public void deleteRegistration(long regId) throws NoResultException {
        Registration regRemove = getRegistration(regId);
        regRemove.setCustomer(null);
        regRemove.setEvent(null);
        if (regRemove.getCustomer() == null && regRemove.getEvent() == null) { //checks for user FK
            em.remove(regRemove);
        } else {
            throw new NoResultException("Registration ID " + regId + " is associated with existing customers or registrations and cannot be deleted!");
        }
    }
    
     public boolean isCustomerRegistered(Long customerId, Long eventId) {
        // This query checks if there's any registration that matches the customerId and eventId
        String jpql = "SELECT COUNT(r) FROM Registration r WHERE r.customer.customerId = :customerId AND r.event.eventId = :eventId";
        Long count = em.createQuery(jpql, Long.class)
                                  .setParameter("customerId", customerId)
                                  .setParameter("eventId", eventId)
                                  .getSingleResult();
        return count > 0; // If count is greater than 0, the customer is already registered
    }
    
    @Override
    public void markPresent(long regId) 
    {
        Registration reg = getRegistration(regId);
        reg.setAttended(true);
    }
    
    @Override
    public void markAbsent(long regId) 
    {
        Registration reg = getRegistration(regId);
        reg.setAttended(false);
    }
}
