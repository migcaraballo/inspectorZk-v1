package com.migs.zk.inspectorzk.ui.controllers;

import com.sun.javafx.tk.Toolkit;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by migc on 1/20/18.
 */
public abstract class AbstractChildStageController {

	protected static final Logger log = LogManager.getLogger();

	private Stage controllerStage;
	private Scene controllerScene;
	private boolean initialized;

	/******************************************************************************************************************/

	protected abstract void show();

	protected void init(){
		log.debug("initializing: AbstractController");
		getControllerStage().setScene(getControllerScene());
		getControllerStage().initModality(Modality.APPLICATION_MODAL);
	}

	private void setupCloseStageShortcuts(){
		//cMD || CTL + W depending on platform
		getControllerScene().getAccelerators().put(
			new KeyCodeCombination(KeyCode.W, KeyCombination.META_DOWN), () -> {
					getControllerStage().close();
				}
		);
	}


	/******************************************************************************************************************/

	protected void setControllerStage(Stage stg){
		controllerStage = stg;
	}

	protected Stage getControllerStage() {
		return controllerStage;
	}

	protected Scene getControllerScene() {
		return controllerScene;
	}

	protected void setControllerScene(Scene controllerScene) {
		this.controllerScene = controllerScene;
	}

	protected boolean isInitialized() {
		return initialized;
	}

	protected void setInitialized(boolean initialized) {
		this.initialized = initialized;
	}
}
