/**
 * 
 */
package services;

import javax.persistence.EntityManager;

/**
 * Provides overall functionalities
 *  
 * 
 * @author TEAM RMG
 *
 */
public abstract class Service {
	
	private EntityManager entityManager;
	
	/**
	 * empty ctor
	 */
	public Service() { }
	
	/**
	 * ctor 
	 * @param em EntityManager
	 */
	public Service(EntityManager em) {
		this.entityManager = em;
	}
	
	/**
	 * 
	 * @return EntityManager
	 */
	public EntityManager getEntityManager() {
		return entityManager;
	}

}
