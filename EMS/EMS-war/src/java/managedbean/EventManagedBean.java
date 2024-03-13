/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package managedbean;

import entity.Customer;
import java.util.Date;
import javax.ejb.EJB;
import javax.inject.Named;
import javax.enterprise.context.RequestScoped;
import entity.Event;
import error.ErrorException;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.inject.Inject;
import session.CustomerSessionBeanLocal;
import session.EventSessionBeanLocal;

/**
 *
 * @author justi
 */
@Named(value = "eventManagedBean")
@RequestScoped
public class EventManagedBean {

    @EJB
    private CustomerSessionBeanLocal customerSessionBeanLocal;

    @EJB
    private EventSessionBeanLocal eventSessionBeanLocal;
    
    @Inject
    private AuthenticationManagedBean authenticationManagedBean;
    
    private String title;
    private Date eventDate;
    private String location;
    private String description;
    private Date deadline;
    private long customerId;
    private List<Event> events;
    private Long eId;
    private Event selectedEvent;
    private String searchString;
    
    public EventManagedBean() {
        
    }
    
    @PostConstruct
    public void init() {
        if (searchString == null || searchString.equals("")) {
            events = eventSessionBeanLocal.retrieveAllEvents();
        } else {
            events = eventSessionBeanLocal.retrieveEventsByName(searchString);
        }
    }
        
    public void addEvent(ActionEvent evt) throws ErrorException {
        Event e = new Event();
        e.setEventDate(eventDate);
        e.setDeadline(deadline);
        e.setTitle(title);
        e.setDescription(description);
        e.setLocation(location);
        // System.out.println("Customer id is " + authenticationManagedBean.getUserId());
        Customer c = customerSessionBeanLocal.getCustomer(authenticationManagedBean.getUserId());
        e.setCreator(c);
        eventSessionBeanLocal.createNewEvent(e);
    }
    
    public void handleSearch() {
        init();
    }
    
    public List<Event> filter() throws ErrorException {
        events = eventSessionBeanLocal.retrieveEventsByCustomer(authenticationManagedBean.getUserId());
        return events;
    }
    
    public void filterSearch() throws ErrorException {
    if (searchString == null || searchString.trim().isEmpty()) {
        events = eventSessionBeanLocal.retrieveEventsByCustomer(authenticationManagedBean.getUserId());
    } else {
        events = eventSessionBeanLocal.retrieveEventsByNameAndCustomer(searchString, authenticationManagedBean.getUserId());
    }
}

    
    public void deleteEvent() {
        FacesContext context = FacesContext.getCurrentInstance();

        Map<String, String> params = context.getExternalContext()
                .getRequestParameterMap();
        String cIdStr = params.get("cId");
        Long cId = Long.parseLong(cIdStr);

        try {
            eventSessionBeanLocal.deleteEvent(cId);
        } catch (Exception e) {
            //show with an error icon
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Unable to delete event"));
            return;
        }

        context.addMessage(null, new FacesMessage("Success", "Successfully deleted event"));
        init();

    } 
    
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Date getEventDate() {
        return eventDate;
    }

    public void setEventDate(Date eventDate) {
        this.eventDate = eventDate;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getDeadline() {
        return deadline;
    }

    public void setDeadline(Date deadline) {
        this.deadline = deadline;
    }

    public long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(long customerId) {
        this.customerId = customerId;
    }

    public List<Event> getEvents() {
        return events;
    }

    public void setEvents(List<Event> events) {
        this.events = events;
    }

    public Event getSelectedEvent() {
        return selectedEvent;
    }

    public void setSelectedEvent(Event selectedEvent) {
        this.selectedEvent = selectedEvent;
    }

    public String getSearchString() {
        return searchString;
    }

    public void setSearchString(String searchString) {
        this.searchString = searchString;
    }
    
    
    
}
