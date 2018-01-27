package com.migs.zk.inspectorzk.ui.controllers;

import com.migs.zk.inspectorzk.domain.Session;
import javafx.fxml.FXML;
import javafx.scene.control.Hyperlink;
import javafx.scene.layout.Border;


/**
 * Created by migc on 1/21/18.
 */
public class TitleController {

	@FXML
	private Hyperlink openLink;

	@FXML
	private Hyperlink getZnodeLink;

	@FXML
	private void initialize(){
		openLink.setBorder(Border.EMPTY);
		getZnodeLink.setBorder(Border.EMPTY);

		openLink.disableProperty().bind(Session.getSession().connectedProperty());
		getZnodeLink.disableProperty().bind(Session.getSession().connectedProperty().not());
	}

	@FXML
	private void openLinkAction(){
		MainController.setMainContent(SceneName.OpenConnection);
	}

	@FXML
	private void getZnodeLinkAction(){
		MainController.setMainContent(SceneName.GetZNode);
	}

}
