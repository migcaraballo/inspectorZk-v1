package com.migs.zk.inspectorzk.ui.controllers;

import com.migs.zk.inspectorzk.drivers.InspectorZKMain;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by migc on 1/20/18.
 */
public class ManualController extends AbstractChildStageController {

	private static final Logger log = LogManager.getLogger();

	@FXML
	private Text generalDescripText;

	@FXML
	private Hyperlink issuesLink;

	@FXML
	private Text connectionDescripText;

	@FXML
	private Text disconnectText;

	@FXML
	private Text authText;

	@FXML
	private Text viewZnodesText;

	@FXML
	private Button closeButton;

	protected void init(){
		super.init();

		try {

			// setup filesystem
			URI uri = getClass().getResource("/props").toURI();
			Map<String, String> env = new HashMap<>();
			env.put("create", "true");
			FileSystem zfs = FileSystems.newFileSystem(uri, env);

			byte[] txtData = Files.readAllBytes(Paths.get(getClass().getResource("/props/manual-descript.txt").toURI()));
			if(txtData != null) {
				generalDescripText.setText(new String(txtData, "UTF-8"));
			}

			txtData = Files.readAllBytes(Paths.get(getClass().getResource("/props/connection_descrip.txt").toURI()));
			if(txtData != null)
				connectionDescripText.setText(new String(txtData, "UTF-8"));

			txtData = Files.readAllBytes(Paths.get(getClass().getResource("/props/disconnect.txt").toURI()));
			if(txtData != null)
				disconnectText.setText(new String(txtData, "UTF-8"));

			txtData = Files.readAllBytes(Paths.get(getClass().getResource("/props/auth.txt").toURI()));
			if(txtData != null)
				authText.setText(new String(txtData, "UTF-8"));

			txtData = Files.readAllBytes(Paths.get(getClass().getResource("/props/viewznodes.txt").toURI()));
			if(txtData != null)
				viewZnodesText.setText(new String(txtData, "UTF-8"));

		} catch (Exception e) {
			log.error(e);
		}

		getControllerStage().setAlwaysOnTop(true);
		getControllerStage().setResizable(false);
		setInitialized(true);
	}

	@Override
	protected void show() {
		if(!isInitialized())
			init();

		getControllerStage().show();
	}

	@FXML
	private void closeButtonAction(){
		getControllerStage().close();
	}

	@FXML
	private void issuesLinkAction(){
		InspectorZKMain.showDoc("https://github.com/migcaraballo/inspectorZk-v1/issues/new");
	}
}
