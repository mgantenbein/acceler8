package models.dao;

import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import models.Token;
import play.Logger;

/**
 * Token dao.
 * 
 * @author TEAM RMG
 */
public class TokenDao extends Dao<Token, UUID> implements ITokenDao {

	public TokenDao(EntityManager em) {
		super(Token.class, em);
	}


    /**
     * Retrieve a token by id and type.
     *
     * @param uuid token
     * @param type of token
     * @return Token reset token
     * @throws IllegalArgumentException
     */
	@Override
	public Token findByTokenAndType(String uuid, String type) throws IllegalArgumentException {
		
		if(uuid == null)
			throw new IllegalArgumentException("empty.token");
		if(type == null)
			throw new IllegalArgumentException("empty.type");
		
		Token token = null;
		
		Query query = getEntityManager().createQuery("select u from " + getPersistentClass().getSimpleName()
				+ " u where u.token = :token and type = :type")
				.setParameter("token", uuid)
				.setParameter("type", type);
		try {
			token = (Token)query.getSingleResult();
		} catch (Exception e) {
			Logger.info("Token not found: " + uuid);
		}
		
		return token;
	}

}
