package com.migs.zk.inspectorzk.ui.controllers;

import com.migs.zk.inspectorzk.domain.PrefKeys;
import com.migs.zk.inspectorzk.domain.Session;
import com.migs.zk.inspectorzk.domain.ZKConnInfo;
import com.migs.zk.inspectorzk.services.ZKService;
import static com.migs.zk.inspectorzk.ui.util.Txt.frm;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by migc on 1/12/18.
 */
public class OpenConnectionController {

	private static Logger log = LogManager.getLogger(OpenConnectionController.class);

	private Parent topNode;

	@FXML
	private TextField hostTextField;

	@FXML
	private TextField portTextField;

	@FXML
	private Button connectButton;

	@FXML
	private Button cancelButton;

	@FXML
	private Text errorText;

	@FXML
	private ComboBox<String> previousHostsComboBox;

	private ZKService zkService;
	private Session session;

	@FXML
	private void initialize(){
		log.debug("Initializing OpenConnectionController...");
		session = Session.getSession();
		zkService = ZKService.getInstance();

		loadPreviousHostsList();
	}

	@FXML
	private void connectButtonAction(){
		log.debug("Connect button pressed");

		if(validate()){
			ZKConnInfo ci = new ZKConnInfo(hostTextField.textProperty().get(), Integer.valueOf(portTextField.textProperty().get()));
			session.setAttribute("ci", ci);

			MainController.changeToWaitCursor();
			boolean connected = zkService.connectToZK(ci);
			MainController.changeToNormalCursor();

			if(connected){
				hostTextField.clear();
				portTextField.clear();
				previousHostsComboBox.getSelectionModel().clearSelection();
				setPreviousHost(ci);
				errorText.textProperty().set("");
				session.setConnected();

				MainController.setConnectionStatus(frm("Connected to %s:%d", ci.getHost(), ci.getPort()));
				MainController.setStageTitle(frm("Connected: [%s:%d]", ci.getHost(), ci.getPort()));
				MainController.clearContentPane();
			}
			else{
				errorText.textProperty().set("Connection Failed, try again");
			}
		}
		else{
			errorText.textProperty().set("Invalid Entries, please try again");
		}
	}

	@FXML
	private void cancelButtonAction(){
		log.debug("Cancel button pressed");
		MainController.clearContentPane();
	}

	@FXML
	private void previousHostBoxAction(){
		String selectedVal = previousHostsComboBox.getSelectionModel().getSelectedItem();

		if(selectedVal != null && !selectedVal.isEmpty()){
			String[] hostParts = parseHostString(selectedVal);
			hostTextField.setText(hostParts[0]);
			portTextField.setText(hostParts[1]);
		}
	}

	protected void loadPreviousHostsList(){
		String hostListData = MainController.getPrefValue(PrefKeys.LAST_HOST_LIST_PREF);
		log.debug(frm("loadPreviousHostsList: hostListData = %s",hostListData));

		// clear entries first
		previousHostsComboBox.getItems().clear();

		if(hostListData != null & !hostListData.isEmpty()){
			if(hostListData.contains(";")){ // probably multiple hosts
				String[] hostDataArr = hostListData.split(";");

				for (String hostStr : hostDataArr) {
					previousHostsComboBox.getItems().add(hostStr);
				}

				previousHostsComboBox.setDisable(false);
			}
			else{ // only 1 previous host
				previousHostsComboBox.setDisable(true);
				String[] hostParts = parseHostString(hostListData);

				if(hostParts != null && hostParts.length == 2){
					hostTextField.setText(hostParts[0]);
					portTextField.setText(hostParts[1]);
				}
			}
		}
		else
			previousHostsComboBox.setDisable(true);
	}

	private void setPreviousHost(ZKConnInfo connInfo){
		String currHostListString = MainController.getPrefValue(PrefKeys.LAST_HOST_LIST_PREF);
		log.debug(frm("setPreviousHost: [s]currHostListString = %s",currHostListString));

		if(currHostListString != null && !currHostListString.isEmpty()){

			if(!currHostListString.contains(connInfo.getFullHostString())){

				if(currHostListString.endsWith(";")) {
					currHostListString += connInfo.getFullHostString();
				}
				else {
					currHostListString += ";"+ connInfo.getFullHostString();
				}

				log.debug(frm("setPreviousHost: [e]currHostListString = %s", currHostListString));
				MainController.setPref(PrefKeys.LAST_HOST_LIST_PREF, currHostListString);
			}
		}
		else {
			MainController.setPref(PrefKeys.LAST_HOST_LIST_PREF, connInfo.getFullHostString());
		}

		loadPreviousHostsList();
	}

	private String[] parseHostString(String hostString){
		if(hostString != null && !hostString.isEmpty()){
			return hostString.split(":");
		}
		return null;
	}

	private boolean validate(){
		if(hostTextField.textProperty().isEmpty().get())
			return false;

		if(portTextField.textProperty().isEmpty().get())
			return false;

		if(!StringUtils.isNumeric(portTextField.textProperty().get()))
			return false;

		return true;
	}

	protected int totalPreviousHosts(){
		return previousHostsComboBox.getItems().size();
	}

	protected Parent getTopNode(){
		return topNode;
	}

	protected void setTopNode(Parent node){
		this.topNode = node;
	}
}
