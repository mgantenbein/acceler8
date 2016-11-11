package controllers;

import play.mvc.*;
import play.data.Form;
import play.data.FormFactory;
import play.db.jpa.JPAApi;
import play.db.jpa.Transactional;

import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import models.Team;
import models.dao.TeamDao;
import views.html.team.*;




/**
 * Controls team requests
 * 
 * @author Team RMG
 */
public class TeamController extends Controller {
	
	JPAApi jpa;
	
	@Inject FormFactory formFactory;	

	
	/**
	 * ctor 
	 * @param api java persistence api (injected)
	 */
	@Inject
	public TeamController(JPAApi api) {
	    this.jpa = api;
	}
	
    /**
     * List all teams
     * 
     * @return http response
     */
	@Transactional(readOnly=true)
    public Result index() {
		
		TeamDao dao = new TeamDao(jpa.em());
		List<Team> teams = dao.findAll();
		
    	return ok(list.render(teams));
    }
	
	/**
	 * Render team detail view
	 * 
	 * @return http response
	 */
	@Transactional()
	public Result newTeam(){

		Form<Team> form = formFactory.form(Team.class);
		return ok(detail.render(form));
	}
    
	/**
	 * Render team detail view
	 * pass team to model
	 * 
	 * @param id Team id
	 * @return http response
	 */
	@Transactional(readOnly=true)
	public Result details(int id){

		//EntityManager em = jpa.em();
		//final Team team = em.find(Team.class, id);
		
		TeamDao dao = new TeamDao(jpa.em());
		final Team team = dao.findById(id);
		
		if (team == null) {
			return notFound(String.format("Team %s does not exist.", id));
		}
		Form<Team> form = formFactory.form(Team.class);
		return ok(detail.render(form.fill(team)));
	}


	@Transactional()
	public Result save(){
		Form<Team> teamForm = formFactory.form(Team.class).bindFromRequest();
		if(teamForm.hasErrors()){
			flash("error", "Please correct the form below.");
			return badRequest(detail.render(teamForm));
		}
		Team team  = teamForm.get();
		
		TeamDao dao = new TeamDao(jpa.em());
		dao.save(team);
		//EntityManager em = jpa.em();
		//em.merge(team);
		
		flash("success", String.format("Saved team %s", team));
		return redirect(routes.TeamController.index());
	}
	
	@Transactional()
	public Result delete(int id) {
		TeamDao dao = new TeamDao(jpa.em());
		Team team = dao.findById(id);
		
		if(team == null) {
			return notFound(String.format("Product %s does not exists.", id));
		}
		dao.delete(team);
		return redirect(routes.TeamController.index());

	}
    

}