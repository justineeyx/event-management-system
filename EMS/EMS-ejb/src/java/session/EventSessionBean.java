/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package session;

import entity.Event;
import entity.Registration;
import error.ErrorException;
import java.lang.reflect.Field;
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
public class EventSessionBean implements EventSessionBeanLocal {

    @PersistenceContext(unitName = "EMS-ejbPU")
    private EntityManager em;

    // Add business logic below. (Right-click in editor and choose
    // "Insert Code > Add Business Method")
    
    public Event createNewEvent(Event e) throws ErrorException {
         try {
           em.persist(e);
           em.flush();
           return e;
       } catch (PersistenceException ex) {
           if (ex.getCause() != null && ex.getCause().getClass().getName().equals("org.eclipse.persistence.exceptions.DatabaseException")) {
                if (ex.getCause().getCause() != null && ex.getCause().getCause().getClass().getName().equals("java.sql.SQLIntegrityConstraintViolationException")) {
                    throw new ErrorException("An event with the same identification number exists!");
                } else {
                    throw new ErrorException(ex.getMessage());
                }
            } else {
                throw new ErrorException(ex.getMessage());
            }
        }
    }
    
    @Override
    public List<Event> retrieveAllEvents() 
    {
        Query query = em.createQuery("SELECT e FROM Event e");
        return query.getResultList();
    }
    
    @Override
    public Event getEvent(Long eventId) throws NoResultException {
        Event e = em.find(Event.class, eventId);

        if (e != null) {
            return e;
        } else {
            throw new NoResultException("Not found");
        }
    }
    
    @Override
    public void deleteEvent(long eventId) {
        Event eventRemove = getEvent(eventId);

        List<Registration> fields = eventRemove.getRegistrations();
        eventRemove.setRegistrations(null);

        for (Registration r : fields) {
            //if no other association between field and customer, we are safe to delete this field
            Query q = em.createQuery("SELECT count(e) FROM Event e WHERE :registration MEMBER OF e.registrations");
            q.setParameter("registration", r);

            long count = (Long) q.getSingleResult();

            if (count == 0) {
                em.remove(r);
            }
        }

        em.remove(eventRemove);
    }
    
    @Override
    public List<Event> retrieveEventsByCustomer(long customerId) throws ErrorException {
        Query query = em.createQuery("SELECT e FROM Event e WHERE e.creator.customerId = :id");
        query.setParameter("id", customerId);
        
        try {
            return query.getResultList();
        } catch (NoResultException | NonUniqueResultException ex) {
            throw new ErrorException("Customer username: " + customerId + "does not exist!\n");
        }
    }
    
    @Override
    public List<Event> retrieveEventsByName(String eventName) {
        Query query = em.createQuery("SELECT e FROM Event e WHERE e.title = :id");
        query.setParameter("id", eventName);
        return query.getResultList();
    }
    
    @Override
    public List<Event> retrieveEventsByNameAndCustomer(String eventName, long customerId) {
        Query query = em.createQuery("SELECT e FROM Event e WHERE e.title = :id AND e.creator.customerId = :cId");
        query.setParameter("id", eventName);
        query.setParameter("cId", customerId);
        return query.getResultList();
    }
    
    @Override
    public Event retrieveEventsByRegId(long rId) {
        Query query = em.createQuery("SELECT e FROM Event e WHERE e.registrations.registrationId = :rId");
        query.setParameter("rId", rId);
        return (Event)query.getSingleResult();
    }
}
