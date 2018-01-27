package com.migs.zk.inspectorzk.ui.controllers;

import com.migs.zk.inspectorzk.domain.Session;
import com.migs.zk.inspectorzk.domain.ZKPerms;
import com.migs.zk.inspectorzk.domain.ZKSchemeDefs;
import com.migs.zk.inspectorzk.services.ZKDataException;
import com.migs.zk.inspectorzk.services.ZKService;
import com.migs.zk.inspectorzk.ui.util.AlertUtil;
import com.migs.zk.inspectorzk.util.ZKUtils;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.stage.WindowEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.migs.zk.inspectorzk.ui.util.Txt.frm;

/**
 * Created by migc on 1/22/18.
 *
 * 1. if digest scheme
 * 		- show ID,
 * 		- show pw,
 * 		- show perms
 * 2. if world
 * 		- show perms only
 * 3. if auth/sasl
 * 		- show ID
 * 		- show perms
 * 4. if ip
 *    	- show IP:
 *    	- show perms
 */
public class AddAclController extends AbstractChildStageController {

	private static final Logger log = LogManager.getLogger();

	@FXML
	private ComboBox<ZKSchemeDefs> schemeComboBox;

	@FXML
	private Label idLabel;

	@FXML
	private TextField idTextField;

	@FXML
	private Label valueLabel;

	@FXML
	private StackPane stackPane;

	@FXML
	private PasswordField pwField;

	@FXML
	private TextField valTextField;

	@FXML
	private Button addButton;

	@FXML
	private Button clearButton;

	@FXML
	private Button cancelButton;

	@FXML
	private Text pathText;

	/* Perms CB's */
	@FXML
	private CheckBox createCB;
	@FXML
	private CheckBox deleteCB;
	@FXML
	private CheckBox readCB;
	@FXML
	private CheckBox writeCB;
	@FXML
	private CheckBox adminCB;
	@FXML
	private CheckBox allCB;

	private ObservableList<CheckBox> permsCBList;

	private ZKService zkService;

	private SimpleStringProperty pathProperty;

	@Override
	protected void init() {
		super.init();
		getControllerStage().setResizable(false);

		zkService = ZKService.getInstance();

		pathText.textProperty().bind(pathProperty);

		pwField = new PasswordField();
		valTextField = new TextField();

		permsCBList = FXCollections.observableArrayList();
		permsCBList.add(createCB);
		permsCBList.add(deleteCB);
		permsCBList.add(readCB);
		permsCBList.add(writeCB);
		permsCBList.add(adminCB);
		permsCBList.add(allCB);

		Arrays.asList(ZKSchemeDefs.values()).forEach(def -> schemeComboBox.getItems().add(def));

		// scheme box listener
		schemeComboBox.getSelectionModel().selectedItemProperty().addListener((obs, ov, nv) -> {
			if(nv != null){
				log.debug(frm("nv = %s", nv.name()));

				idLabel.setText("ID: ");
				idTextField.clear();
				idTextField.setEditable(true);

				valueLabel.setText("-----");
				valueLabel.setDisable(true);

				if(ZKSchemeDefs.AUTH == nv || ZKSchemeDefs.SASL == nv){
					idLabel.setDisable(false);

					if(Session.getSession().authUserProperty().isNotEqualTo("!authed").getValue()){
						idTextField.setDisable(false);
						idTextField.setEditable(false);
						idTextField.setText(Session.getSession().authUserProperty().getValue());
					}
					else{
						idTextField.setDisable(false);
					}

					swapFields(valTextField);
					disableVisableNode();
				}
				else if(ZKSchemeDefs.IP == nv){
					idLabel.setText("IP:");
					idLabel.setDisable(false);
					idTextField.setDisable(false);
					swapFields(valTextField);
					disableVisableNode();
				}
				else if(ZKSchemeDefs.DIGEST == nv) {
					idLabel.setText("User/ID:");
					idLabel.setDisable(false);
					idTextField.setDisable(false);
					valueLabel.setText("Pass:");
					valueLabel.setDisable(false);
					swapFields(pwField);
				}
				else if(ZKSchemeDefs.WORLD == nv){
					idLabel.setDisable(false);
					idTextField.setText("anyone");
					idTextField.setDisable(false);
					idTextField.setEditable(false);

					swapFields(valTextField);
					disableVisableNode();
				}
				else{
					idTextField.setDisable(true);
				}
			}
		});

		resetFields();
		setInitialized(true);
	}

	private void resetFields(){
		swapFields(valTextField);
		disableVisableNode();
		idLabel.setDisable(true);
		idTextField.setDisable(true);
		valueLabel.setText("-----");
		valueLabel.setDisable(true);
	}

	private void swapFields(Node swapToNode){
		stackPane.getChildren().clear();
		stackPane.getChildren().add(swapToNode);
		stackPane.getChildren().get(0).setDisable(false);
	}

	private void disableVisableNode(){
		stackPane.getChildren().get(0).setDisable(true);
	}

	@Override
	protected void show() {
		if(!isInitialized())
			init();

		getControllerStage().show();
	}

	@FXML
	private void allCBCheckedAction(){
		permsCBList.forEach( cb -> cb.setSelected(allCB.isSelected()));
	}

	@FXML
	private void addButtonAction(){

		if(validate()){
			try {
				boolean addAcl = zkService.setAclForZnode(
									pathProperty.get(),
									schemeComboBox.getSelectionModel().getSelectedItem(),
									idTextField.getText().trim(),
									pwField.getText(),
									getSelectedPerms()
								);

				if(!addAcl){
					AlertUtil.infoAlert("ACL Issue", "Could not set ACL for this node", getControllerStage());
				}
				else{
					cancelButtonAction();
				}

			} catch (ZKDataException e) {
				String msg = frm("Problem setting ACL:\n\n%s", e.getMessage());
				log.error(msg);
				AlertUtil.errorAlert("ACL Error", msg, getControllerStage());
			}
		}
		else{
			AlertUtil.errorAlert("Form Error", "Found some issues:\n\n"+errMsg, getControllerStage());
			errMsg = "";
		}

	}

	protected void setAclPath(String path){
		if(pathProperty == null)
			pathProperty = new SimpleStringProperty();

		pathProperty.set(path);
	}

	private List<ZKPerms> getSelectedPerms(){
		ArrayList<ZKPerms> selectedPerms = new ArrayList<>();

		permsCBList.forEach( cb -> {
			if(cb.isSelected()){
				selectedPerms.add(ZKPerms.getMatch(cb.getText()));
			}
		});

		return selectedPerms;
	}

	private String errMsg = "";

	private boolean validate(){
		boolean valid = true;
		ZKSchemeDefs selectedScheme = schemeComboBox.getSelectionModel().selectedItemProperty().getValue();

		if(selectedScheme == null){
			errMsg = "\t- Need to pick a Scheme\n";
			valid = false;
		}
		else if(selectedScheme == ZKSchemeDefs.DIGEST){
			if(idTextField.getText().isEmpty()) {
				valid = false;
				errMsg += "\t- ID field can not be empty\n";
			}

			if(pwField.getText().isEmpty()) {
				valid = false;
				errMsg += "\t- Pass field can not be empty\n";
			}
		}
		else if(selectedScheme == ZKSchemeDefs.AUTH || selectedScheme == ZKSchemeDefs.SASL || selectedScheme == ZKSchemeDefs.IP){
			if(idTextField.getText().isEmpty()) {
				valid = false;
				errMsg += "\t- ID field can not be empty\n";
			}
			else
				valid = true;
		}

		boolean atLeastOneChecked = false;

		for(CheckBox cb : permsCBList){
			if(cb.isSelected()){
				atLeastOneChecked = true;
				break;
			}
		}

		if(!atLeastOneChecked)
			errMsg += "\t- Need to select at least 1 permission\n";

		return valid && atLeastOneChecked;
	}

	@FXML
	private void clearButtonAction(){
		schemeComboBox.getSelectionModel().clearSelection();
		idTextField.clear();
		pwField.clear();
		permsCBList.forEach(i -> i.setSelected(false));
		resetFields();
	}

	@FXML
	private void cancelButtonAction(){
		clearButtonAction();
		getControllerStage().close();
		getControllerStage().fireEvent(new WindowEvent(getControllerStage(), WindowEvent.WINDOW_CLOSE_REQUEST));
	}
}
