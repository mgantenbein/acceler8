package controllers.account;

import models.User;
import models.dao.IUserDao;
import models.dao.UserDao;
import models.Mail;
import models.Register;
import org.apache.commons.mail.EmailException;

import controllers.BaseController;
import play.Configuration;
import play.Logger;
import play.data.Form;
import play.data.FormFactory;
import play.db.jpa.JPA;
import play.db.jpa.JPAApi;
import play.db.jpa.Transactional;
import play.i18n.Messages;
import play.mvc.Controller;
import play.mvc.Result;
import services.account.AccountService;
import services.account.IAccountService;
import services.crypt.HashHelper;
import play.libs.mailer.Email;
import play.libs.mailer.MailerClient;

import javax.inject.Inject;

import views.html.account.signup.confirm;
import views.html.account.signup.create;
import views.html.account.signup.created;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

import static play.data.Form.form;

/**
 * 
 * Signup
 * 
 * @author TEAM RMG
 *
 */
public class SignupController extends BaseController {
    
	
	@Inject
    MailerClient mailerClient;
	
	@Inject 
	FormFactory formFactory;	

    /**
     * Display the create form.
     *
     * @return create form
     */
    public Result create() {
    	
        return ok(create.render(
        		formFactory.form(Register.class)));
    }

    /**
     * Display the create form only (for the index page).
     *
     * @return create form
     */
    public Result createFormOnly() {
        return ok(create.render(
        		formFactory.form(Register.class)));
    }

    /**
     * Save the new user.
     *
     * @return Successfull page or created form if bad
     */
    @Transactional
    public Result save() {
        Form<Register> registerForm = formFactory.form(Register.class).bindFromRequest();

        if (registerForm.hasErrors()) {
            return badRequest(create.render(registerForm));
        }

        Register register = registerForm.get();
        Result resultError = checkBeforeSave(registerForm, register.getEmail());

        if (resultError != null) {
            return resultError;
        }

        try {
            User user = new User();

            user.setEmail(register.getEmail());
            user.setPassword(HashHelper.createPassword(register.getInputPassword()));
            user.setToken(UUID.randomUUID().toString());

            IUserDao dao = new UserDao(em());
            dao.save(user);
            
            sendMailAskForConfirmation(user);

            return ok(created.render());
        } catch (EmailException e) {
            Logger.debug("Signup.save Cannot send email", e);
            flash("error", Messages.get("error.sending.email"));
        } catch (Exception e) {
            Logger.error("Signup.save error", e);
            flash("error", Messages.get("error.technical"));
        }
        return badRequest(create.render(registerForm));
    }

    /**
     * Check if the email already exists.
     *
     * @param registerForm User Form submitted
     * @param email        email address
     * @return Index if there was a problem, null otherwise
     */
    private Result checkBeforeSave(Form<Register> registerForm, String email) {
        // Check unique email
    	IUserDao dao = new UserDao(em());
    	
        if (dao.findByEmail(email) != null) {
            flash("error", Messages.get("error.email.already.exist"));
            return badRequest(create.render(registerForm));
        }

        return null;
    }


    /**
     * Send the welcome Email with the link to confirm.
     *
     * @param user user created
     * @throws EmailException Exception when sending mail
     */
    private void sendMailAskForConfirmation(User user) throws EmailException, MalformedURLException {
        String subject = Messages.get("mail.confirm.subject");

        String urlString = "http://" + Configuration.root().getString("server.hostname");
        urlString += "/confirm/" + user.getToken();
        URL url = new URL(urlString); // validate the URL, will throw an exception if bad.
        String message = Messages.get("mail.confirm.message", url.toString());

        Mail.Envelop envelop = new Mail.Envelop(subject, message, user.getEmail());
        Mail mailer = new Mail(mailerClient);
        mailer.sendMail(envelop);
    }

    /**
     * Valid an account with the url in the confirm mail.
     *
     * @param token a token attached to the user we're confirming.
     * @return Confirmationpage
     */
    @Transactional
    public Result confirm(String token) {
    	IUserDao dao = new UserDao(em());
    	User user = dao.findByConfirmationToken(token);
        
        if (user == null) {
            flash("error", getMessage("error.unknown.email"));
            return badRequest(confirm.render());
        }

        if (user.isValidated()) {
            flash("error", Messages.get("error.account.already.validated"));
            return badRequest(confirm.render());
        }

        try {
        	IAccountService accountService = new AccountService(em());
            if (accountService.confirm(user)) {
                sendMailConfirmation(user);
                flash("success", Messages.get("account.successfully.validated"));
                return ok(confirm.render());
            } else {
                Logger.debug("Signup.confirm cannot confirm user");
                flash("error", Messages.get("error.confirm"));
                return badRequest(confirm.render());
            }
        } catch (EmailException e) {
            Logger.debug("Cannot send email", e);
            flash("error", Messages.get("error.sending.confirm.email"));
        } catch (Exception e) {
            Logger.debug("technical exception", e);
            flash("error", Messages.get("error.sending.confirm.email"));
        }
        return badRequest(confirm.render());
    }

    /**
     * Send the confirm mail.
     *
     * @param user user created
     * @throws EmailException Exception when sending mail
     */
    private void sendMailConfirmation(User user) throws EmailException {
        String subject = Messages.get("mail.welcome.subject");
        String message = Messages.get("mail.welcome.message");
        Mail.Envelop envelop = new Mail.Envelop(subject, message, user.getEmail());
        Mail mailer = new Mail(mailerClient);
        mailer.sendMail(envelop);
    }
}