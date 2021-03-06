package controllers.account;

import controllers.BaseController;
import controllers.Secured;
import models.User;
import models.vm.Profile;
import play.Logger;
import play.data.Form;
import play.data.FormFactory;
import play.db.jpa.Transactional;
import play.mvc.Result;
import play.mvc.Security;
import services.user.IUserService;
import services.user.UserService;
import views.html.userprofile;
import javax.inject.Inject;

/**
 * Profile controller
 * 
 * @author TEAM RMG
 *
 */
@Security.Authenticated(Secured.class)
public class ProfileController extends BaseController {
	
	@Inject 
	FormFactory formFactory;

    /**
     * Index action.
     * Render settings view.
     *
     * @return Result settings view
     */
	@Transactional
    public Result index() {
    	IUserService userService = new UserService(em());
        User user = userService.findByEmail(request().username());
        
        Profile profile = userService.getStravaProfile(user);
        if(profile == null){
        	flash("error", getMessage("profile.load.fail"));
        	//rediret to settings, so that the user can establish a connection
			return redirect(routes.SettingsController.index());
        }
        Form<Profile> profileForm = formFactory.form(Profile.class).fill(profile);
        return ok(userprofile.render(profileForm));
    }
	
	
	/**
	 * Save action.
	 * Save settings and write data back to strava.
	 * 
	 * @return Result settings index view
	 */
	@Transactional
    public Result save() {
        try {
        	Form<Profile> profileForm = formFactory.form(Profile.class).bindFromRequest();
    		IUserService userService = new UserService(em());
            User user = userService.findByEmail(request().username());
            
            if(profileForm.hasErrors()){
            	//flash("error", "")
            	return badRequest(userprofile.render(profileForm));
            }
            Profile profile = profileForm.get();
            //write strava settings
            userService.setStravaProfile(user, profile);
            
            flash("success", getMessage("profile.saved"));
        } catch (Exception e) {
            Logger.error("Cannot save strava profile", e);
            flash("error", getMessage("error.technical"));
        }
        return index();
    }
}
