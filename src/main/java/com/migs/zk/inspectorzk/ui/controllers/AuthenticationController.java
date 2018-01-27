package com.migs.zk.inspectorzk.ui.controllers;

import com.migs.zk.inspectorzk.domain.Session;
import com.migs.zk.inspectorzk.domain.ZKSchemeDefs;
import com.migs.zk.inspectorzk.services.ZKAuthException;
import com.migs.zk.inspectorzk.services.ZKService;
import com.migs.zk.inspectorzk.ui.util.AlertUtil;

import static com.migs.zk.inspectorzk.ui.util.Txt.frm;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by migc on 1/13/18.
 */
public class AuthenticationController {

	private static Logger log = LogManager.getLogger(AuthenticationController.class);

	@FXML
	private TextField userTextField;

	@FXML
	private PasswordField passwordField;

	@FXML
	private Button authButton;

	@FXML
	private Button cancelButton;

	private ZKService zkService;

	@FXML
	private void initialize(){
		log.debug("Initializing AuthenticationController");
		zkService = ZKService.getInstance();
	}

	@FXML
	private void authButtonAction(ActionEvent ae){
		log.debug("Auth Button clicked");

		String usr = userTextField.getText();
		String pw = passwordField.getText();

		log.debug(frm("auth info: %s : %s", usr, pw));

		try {
			boolean authed = zkService.authDigestUser(usr, pw, ZKSchemeDefs.DIGEST);

			if(authed){
				Session.getSession().authUserProperty().set(usr);
				AlertUtil.infoAlert("Authentication", "Authentication succeeded.", null);
				MainController.clearContentPane();
			}
			else {
				AlertUtil.errorAlert("Authentication", "Authentication failed, try again.", null);
			}
		} catch (ZKAuthException e) {
			log.error(frm("Auth error for: ", usr));
		}

		userTextField.clear();
		passwordField.clear();
		MainController.clearContentPane();
	}

	@FXML
	private void cancelButtonAction(ActionEvent ae){
		log.debug("Cancel Button clicked");
		MainController.clearContentPane();
	}
}
