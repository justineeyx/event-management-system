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
import entity.Registration;
import java.util.stream.Collectors;
import javax.faces.component.UIComponent;
import javax.faces.context.Flash;
import javax.faces.validator.ValidatorException;
import session.CustomerSessionBeanLocal;
import session.EventSessionBeanLocal;
import session.RegistrationSessionBeanLocal;

/**
 *
 * @author justi
 */
@Named(value = "eventManagedBean")
@RequestScoped
public class EventManagedBean {

    @EJB(name = "RegistrationSessionBeanLocal")
    private RegistrationSessionBeanLocal registrationSessionBeanLocal;

    @EJB
    private CustomerSessionBeanLocal customerSessionBeanLocal;

    @EJB
    private EventSessionBeanLocal eventSessionBeanLocal;
    
    @Inject
    private AuthenticationManagedBean authenticationManagedBean;
    
    @Inject
    private RegManagedBean regManagedBean;
    
    private String title;
    private Date eventDate;
    private String location;
    private String description;
    private Date deadline;
    private Long customerId;
    private Long eId;
    private List<Event> events;
    private Event selectedEvent;
    private String searchString;
    private List<Registration> regs;
    private Long smth;
    private String searchTerm;
    
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
    
    public String addEvent() {
    FacesContext context = FacesContext.getCurrentInstance();
    
    boolean errors = false;

    if (eventDate.before(new Date())) {
        System.out.println("test");
        context.addMessage("createForm:eventDate", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Event date must be in the future.", null));
        errors = true;
    }
    
    if (deadline.before(new Date())) {
        System.out.println("test");
        context.addMessage("createForm:deadline", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Deadline must be in the future.", null));
        errors = true;
    }

    if (eventDate.before(deadline)) { 
        System.out.println("test2");
        context.addMessage("createForm:deadline", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Event date must be before the deadline.", null));
        errors = true;
    }
    
    if (errors) {
        return null;
    }

    Event e = new Event();
    e.setEventDate(eventDate);
    e.setDeadline(deadline);
    e.setTitle(title);
    e.setDescription(description);
    e.setLocation(location);
    Customer c = customerSessionBeanLocal.getCustomer(authenticationManagedBean.getUserId());
    e.setCreator(c);

    try {
        eventSessionBeanLocal.createNewEvent(e);
        System.out.println("yay");
    } catch (Exception ex) {
        context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Unable to create event"));
        return null;
    }
    return "/secret/searchEvents?faces-redirect=true";
}


    public String updateEvent() {
        FacesContext context = FacesContext.getCurrentInstance();
        Flash flash = context.getExternalContext().getFlash();
        flash.setKeepMessages(true); // Keep messages for the redirect
        Map<String, String> params = context.getExternalContext()
                .getRequestParameterMap();
        String cIdStr = params.get("eventId");
        Long cId = Long.parseLong(cIdStr);
        try {
            Event currentEvent = eventSessionBeanLocal.getEvent(cId); // Get the current user from session

            boolean isUpdated = false; // Flag to check if anything was updated

            if (currentEvent != null) {
                // Check each field for changes and update as necessary

                if (!title.equals(currentEvent.getTitle())) {
                    currentEvent.setTitle(title);
                    isUpdated = true;
                }

                if (!location.equals(currentEvent.getLocation())) {
                    currentEvent.setLocation(location);
                    isUpdated = true;
                }

                if (!description.equals(currentEvent.getDescription())) {
                    currentEvent.setDescription(description);
                    isUpdated = true;
                }

                if (!eventDate.equals(currentEvent.getEventDate())) {
                    currentEvent.setEventDate(eventDate);
                    isUpdated = true;
                }
                if (!deadline.equals(currentEvent.getDeadline())) {
                    currentEvent.setDeadline(deadline);
                    isUpdated = true;
                }
                if (isUpdated) {
                    eventSessionBeanLocal.updateCustomer(selectedEvent); // Method to persist the changes
                    context.addMessage("growl",
                            new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Event updated successfully."));
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
        return "/secret/viewEventsCreated?faces-redirect=true";
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
//
//        Map<String, String> params = context.getExternalContext()
//                .getRequestParameterMap();
//        String cIdStr = params.get("cId");
//        Long cId = Long.parseLong(cIdStr);
        System.out.println("smth is " + smth);

        try {
            eventSessionBeanLocal.deleteEvent(smth);
        } catch (Exception e) {
            //show with an error icon
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Unable to delete event"));
            return;
        }

        context.addMessage(null, new FacesMessage("Success", "Successfully deleted event"));
        init();

    } 
    
    public void loadEventDetails() {
        FacesContext context = FacesContext.getCurrentInstance();
        Map<String, String> params = context.getExternalContext().getRequestParameterMap();
        String eventIdStr = params.get("eventId");
        if (eventIdStr != null) {
            try {
                Long eventId = Long.parseLong(eventIdStr);
                selectedEvent = eventSessionBeanLocal.getEvent(eventId);
                System.out.println("HELLO" + selectedEvent.getEventId());
                if (selectedEvent == null) {
                    context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Event not found."));
                }
            } catch (NumberFormatException e) {
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Invalid event ID."));
            } catch (Exception ex) {
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Failed to load event details."));
            }
        } else {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Warning", "Event ID is missing."));
        }
    }
    
    public boolean isUserRegistered(Long id) {
        System.out.println("smthh reg id " + id);
        Customer c = customerSessionBeanLocal.getCustomer(authenticationManagedBean.getUserId());
        boolean isRegistered = registrationSessionBeanLocal.isCustomerRegistered(c.getCustomerId(), id);
        return isRegistered;
    }

    public void createRegistration(Long eventId) {
        FacesContext context = FacesContext.getCurrentInstance();
//        Map<String, String> params = context.getExternalContext().getRequestParameterMap();
//        String cIdStr = params.get("cId");
//        Long cId = Long.parseLong(cIdStr);

        System.out.println("event id is " + eventId);

        selectedEvent = eventSessionBeanLocal.getEvent(eventId);

        // Check if event deadline has passed or event date is over
        Date now = new Date();
        if (selectedEvent.getDeadline().before(now) || selectedEvent.getEventDate().before(now)) {
            System.out.println("nono");
            context.addMessage("searchForm:datatable-search-input", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Registration deadline has passed or event is already over."));
            return;
        }

        Customer c = customerSessionBeanLocal.getCustomer(authenticationManagedBean.getUserId());

        // Check if customer is already registered for the event
        boolean isRegistered = registrationSessionBeanLocal.isCustomerRegistered(c.getCustomerId(), selectedEvent.getEventId());
        if (isRegistered) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Warning", "You are already registered for this event."));
            return;
        }

        Registration r = new Registration();
        r.setEvent(selectedEvent);
        r.setAttended(false);
        r.setCustomer(c);

        try {
            registrationSessionBeanLocal.createNewRegistration(r);
            c.getEventsRegistered().add(r);
        } catch (Exception e) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Unable to register"));
            return;
        }

        context.addMessage(null, new FacesMessage("Success", "Successfully registered for event"));
    }
    
    public void deleteRegistration() {
        FacesContext context = FacesContext.getCurrentInstance();

//        Map<String, String> params = context.getExternalContext()
//                .getRequestParameterMap();
//        String cIdStr = params.get("cId");
//        Long cId = Long.parseLong(cIdStr);

        try {
            registrationSessionBeanLocal.deleteRegistration(smth);
        } catch (Exception e) {
            //show with an error icon
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Unable to unregister"));
            return;
        }

        context.addMessage(null, new FacesMessage("Success", "Successfully unregister"));
        // init();

    } 

    public List<Registration> getRegs(long cId) {
        regs = registrationSessionBeanLocal.retrieveAllRegistrationsWithEvent(cId);
        return regs;
    }
    
//    public void markPresent() {
//        FacesContext context = FacesContext.getCurrentInstance();
//
//        Map<String, String> params = context.getExternalContext()
//                .getRequestParameterMap();
//        String cIdStr = params.get("cId");
//        Long cId = Long.parseLong(cIdStr);
//
//        try {
//            selectedEvent = eventSessionBeanLocal.retrieveEventsByRegId(cId);
//            registrationSessionBeanLocal.markPresent(cId);
//        } catch (Exception e) {
//            //show with an error icon
//            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Unable to mark attendance"));
//            return;
//        }
//
//        context.addMessage(null, new FacesMessage("Success", "Marked Present"));
//        // init();
//    }
//    
//    public void markAbsent() {
//        FacesContext context = FacesContext.getCurrentInstance();
//
//        Map<String, String> params = context.getExternalContext()
//                .getRequestParameterMap();
//        String cIdStr = params.get("cId");
//        Long cId = Long.parseLong(cIdStr);
//
//        try {
//            selectedEvent = eventSessionBeanLocal.retrieveEventsByRegId(cId);
//            registrationSessionBeanLocal.markAbsent(cId);
//        } catch (Exception e) {
//            //show with an error icon
//            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Unable to mark attendance"));
//            return;
//        }
//
//        context.addMessage(null, new FacesMessage("Success", "Marked Absent"));
//        // init();
//    }
    
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

    public Long geteId() {
        return eId;
    }

    public void seteId(Long eId) {
        this.eId = eId;
    }

    public List<Registration> getRegs() {
        return regs;
    }

    public void setRegs(List<Registration> regs) {
        this.regs = regs;
    }

    public Long getSmth() {
        return smth;
    }

    public void setSmth(Long smth) {
        this.smth = smth;
    }
    
    public String getSearchTerm() {
        return searchTerm;
    }

    public void setSearchTerm(String searchTerm) {
        this.searchTerm = searchTerm;
    }

    // Method to filter events based on searchTerm
    public void filterEvents() {
        // System.out.println("Search term is " + searchTerm);
        // Assuming you have a method to get all events or the original list
        List<Event> allEvents = eventSessionBeanLocal.retrieveAllEvents();
;
        if (searchTerm == null || searchTerm.isEmpty()) {
            events = allEvents;
        } else {
            events = allEvents.stream()
                    .filter(e -> e.getTitle().toLowerCase().contains(searchTerm.toLowerCase()))
                    .collect(Collectors.toList());
        }
    }
    
    public void filterMyEvents() throws ErrorException {
        // System.out.println("Search term is " + searchTerm);
        // Assuming you have a method to get all events or the original list
        List<Event> allEvents = eventSessionBeanLocal.retrieveEventsByCustomer(authenticationManagedBean.getUserId());
;
        if (searchTerm == null || searchTerm.isEmpty()) {
            events = allEvents;
        } else {
            events = allEvents.stream()
                    .filter(e -> e.getTitle().toLowerCase().contains(searchTerm.toLowerCase()))
                    .collect(Collectors.toList());
        }
    }
    
}
