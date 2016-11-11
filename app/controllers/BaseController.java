package controllers;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import play.db.jpa.JPAApi;
import play.i18n.Messages;
import play.i18n.MessagesApi;
import play.mvc.*;

/**
 * Base controller functions.
 * 
 * 
 * @author TEAM RMG
 *
 */
public abstract class BaseController extends Controller {

	@Inject
	JPAApi jpa;
	
	@Inject
    private MessagesApi messagesApi;
    private Messages messages;
    

	/**
	 * Java persistence api
	 * 
	 * @return the jpa
	 */
	public JPAApi getJpa() {
		return jpa;
	}

	/**
	 * Java persistence api EntityManager
	 * 
	 * @return EntityManager
	 */
	public EntityManager em(){
		return jpa.em();
	}	
	
	/**
	 * Get a localized text.
	 * 
	 * @param key 
	 * @return the text
	 */
	public String getMessage(String key){
		this.messages = messagesApi.preferred(request());
		return messages.at(key);
	}
	
	
}
