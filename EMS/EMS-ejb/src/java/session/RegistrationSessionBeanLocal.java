/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package session;

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
public interface RegistrationSessionBeanLocal {

    public Registration getRegistration(Long regId) throws NoResultException;

    public Registration createNewRegistration(Registration r) throws ErrorException;

    public void deleteRegistration(long regId) throws NoResultException;

    public List<Registration> retrieveAllRegistrations();

    public void markPresent(long regId);

    public void markAbsent(long regId);
    
}
