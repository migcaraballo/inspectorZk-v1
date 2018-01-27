package com.migs.zk.inspectorzk.ui.util;

import com.migs.zk.inspectorzk.domain.Session;
import com.migs.zk.inspectorzk.domain.ZKConnInfo;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

import java.util.Optional;

/**
 * Created by migc on 1/19/18.
 */
public class AlertUtil {

	public static boolean confirmationAlert(String title, String content){
		return confirmationAlert(title, content, null);
	}

	public static boolean confirmationAlert(String title, String content, Stage ownerStage){
		ButtonType yesBT = new ButtonType("Yes", ButtonBar.ButtonData.OK_DONE);
		ButtonType noBT = new ButtonType("No", ButtonBar.ButtonData.CANCEL_CLOSE);

		Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION, content, yesBT, noBT);
		confirmAlert.setTitle(title);
//		confirmAlert.setContentText(content);

		if(ownerStage != null)
			confirmAlert.initOwner(ownerStage);

		Optional<ButtonType> optionChozen = confirmAlert.showAndWait();

		return ( optionChozen.isPresent() && (optionChozen.get() == yesBT) );
	}

	public static void infoAlert(String title, String content, Stage ownerStage){
		alert(title, content, ownerStage, Alert.AlertType.INFORMATION);
	}

	public static void errorAlert(String title, String content, Stage ownerStage){
		alert(title, content, ownerStage, Alert.AlertType.ERROR);
	}

	private static void alert(String title, String content, Stage ownerStage, Alert.AlertType alertType){
		Alert infoAlert = new Alert(alertType);
		infoAlert.setTitle(title);
		infoAlert.setContentText(content);

		if(ownerStage != null)
			infoAlert.initOwner(ownerStage);

		infoAlert.showAndWait();
	}
}
