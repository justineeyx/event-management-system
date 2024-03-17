/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package managedbean;

import entity.Event;
import entity.Registration;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.context.Flash;
import session.EventSessionBeanLocal;
import session.RegistrationSessionBeanLocal;

/**
 *
 * @author justi
 */
@Named(value = "regManagedBean")
@SessionScoped
public class RegManagedBean implements Serializable {
    
    @EJB
    private EventSessionBeanLocal eventSessionBeanLocal;
    
    @EJB(name = "RegistrationSessionBeanLocal")
    private RegistrationSessionBeanLocal registrationSessionBeanLocal;
    
    private Event selectedEvent;
    private List<Registration> regs;
    private Long regId;
    private Long eventId;

    /**
     * Creates a new instance of RegManagedBean
     */
    public RegManagedBean() {
    }
    
    public List<Registration> getRegs(long cId) {
        regs = registrationSessionBeanLocal.retrieveAllRegistrationsWithEvent(cId);
        return regs;
    }
    
    public void loadEventDetails() {
        FacesContext context = FacesContext.getCurrentInstance();
        Map<String, String> params = context.getExternalContext().getRequestParameterMap();
        String eventIdStr = params.get("eventId");
        if (eventIdStr != null) {
            try {
                Long eeId = Long.parseLong(eventIdStr);
                selectedEvent = eventSessionBeanLocal.getEvent(this.eventId);
                System.out.println(eventId);
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
    
    public void markPresent() {
        FacesContext context = FacesContext.getCurrentInstance();

        Map<String, String> params = context.getExternalContext()
                .getRequestParameterMap();
        String cIdStr = params.get("cId");
        Long cId = Long.parseLong(cIdStr);

        try {
            this.selectedEvent = eventSessionBeanLocal.retrieveEventsByRegId(cId);
            System.out.println("event id " + selectedEvent.getEventId());
            registrationSessionBeanLocal.markPresent(cId);
        } catch (Exception e) {
            //show with an error icon
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Unable to mark attendance"));
            return;
        }

        context.addMessage(null, new FacesMessage("Success", "Marked Present"));
        // init();
    }
    
    public void markAbsent() {
        FacesContext context = FacesContext.getCurrentInstance();

        Map<String, String> params = context.getExternalContext()
                .getRequestParameterMap();
        String cIdStr = params.get("cId");
        Long cId = Long.parseLong(cIdStr);

        try {
            selectedEvent = eventSessionBeanLocal.retrieveEventsByRegId(cId);
            System.out.println("event id " + selectedEvent.getEventId());
            registrationSessionBeanLocal.markAbsent(cId);
        } catch (Exception e) {
            //show with an error icon
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Unable to mark attendance"));
            return;
        }

        context.addMessage(null, new FacesMessage("Success", "Marked Absent"));
        // init();
    }
    
    public String updateEvent() {
        FacesContext context = FacesContext.getCurrentInstance();
        Flash flash = context.getExternalContext().getFlash();
        flash.setKeepMessages(true); // Keep messages for the redirect
//        Map<String, String> params = context.getExternalContext()
//                .getRequestParameterMap();
//        String cIdStr = params.get("eventId");
//        Long cId = Long.parseLong(cIdStr);
        try {
            
            // System.out.println("FIRST " + selectedEvent.getEventId());
            Event currentEvent = eventSessionBeanLocal.getEvent(this.eventId);
            //System.out.println("SECOND " + (currentEvent != null ? currentEvent.getEventId() : "null"));
            boolean isUpdated = false; // Flag to check if anything was updated

            if (currentEvent != null) {
                // Check each field for changes and update as necessary

                if (!selectedEvent.getTitle().equals(currentEvent.getTitle())) {
                    currentEvent.setTitle(selectedEvent.getTitle());
                    isUpdated = true;
                }

                if (!selectedEvent.getLocation().equals(currentEvent.getLocation())) {
                    currentEvent.setLocation(selectedEvent.getLocation());
                    isUpdated = true;
                }

                if (!selectedEvent.getDescription().equals(currentEvent.getDescription())) {
                    currentEvent.setDescription(selectedEvent.getDescription());
                    isUpdated = true;
                }

                if (!selectedEvent.getEventDate().equals(currentEvent.getEventDate())) {
                    currentEvent.setEventDate(selectedEvent.getEventDate());
                    isUpdated = true;
                }
                if (!selectedEvent.getDeadline().equals(currentEvent.getDeadline())) {
                    currentEvent.setDeadline(selectedEvent.getDeadline());
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

    public Event getSelectedEvent() {
        return selectedEvent;
    }

    public void setSelectedEvent(Event selectedEvent) {
        this.selectedEvent = selectedEvent;
    }

    public List<Registration> getRegs() {
        return regs;
    }

    public void setRegs(List<Registration> regs) {
        this.regs = regs;
    }

    public Long getRegId() {
        return regId;
    }

    public void setRegId(Long regId) {
        this.regId = regId;
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }
    
    
}
