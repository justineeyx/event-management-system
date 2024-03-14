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
                // System.out.println("HELLO" + selectedEvent.getEventId());
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
