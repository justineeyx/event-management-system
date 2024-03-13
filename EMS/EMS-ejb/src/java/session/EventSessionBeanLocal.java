/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package session;

import entity.Event;
import entity.Registration;
import error.ErrorException;
import java.util.List;
import javax.ejb.Local;
import javax.persistence.NoResultException;

/**
 *
 * @author justi
 */
@Local
public interface EventSessionBeanLocal {

    public Event createNewEvent(Event e) throws ErrorException;

    public List<Event> retrieveAllEvents();

    public Event getEvent(Long eventId) throws NoResultException;

    public void deleteEvent(long eventId);

    public List<Event> retrieveEventsByCustomer(long customerId) throws ErrorException;

    public List<Event> retrieveEventsByName(String eventName);

    public List<Event> retrieveEventsByNameAndCustomer(String eventName, long customerId);

    public Event retrieveEventsByRegId(long rId);
    
}
