package com.migs.zk.inspectorzk.ui.controllers;

import com.migs.zk.inspectorzk.services.ZKDataException;
import com.migs.zk.inspectorzk.services.ZKService;
import com.migs.zk.inspectorzk.ui.util.AlertUtil;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.zookeeper.CreateMode;

/**
 * Created by migc on 1/20/18.
 */
public class AddChildNodeController extends AbstractChildStageController {

	private static final Logger log = LogManager.getLogger();

	@FXML
	private TextField childTextField;

	@FXML
	private TextArea dataTextArea;

	@FXML
	private Text parentPathText;

	@FXML
	private Button addButton;

	@FXML
	private Button clearButton;

	@FXML
	private Button cancelButton;

	private ZKService zkService;
	private GetZNodeController parentController;

	@Override
	protected void init() {
		super.init();

		zkService = ZKService.getInstance();

		getControllerStage().setAlwaysOnTop(true);
		setInitialized(true);
	}

	@Override
	protected void show() {
		if(!isInitialized())
			init();

		childTextField.requestFocus();
		getControllerStage().show();
	}

	protected void setParentPathText(String parentPath) {
		parentPathText.setText(parentPath);
	}

	protected void setParentController(GetZNodeController contoller){
		parentController = contoller;
	}

	@FXML
	private void addButtonAction(){
		String parentPath = parentPathText.getText();
		String newChild = childTextField.getText();
		String savePath = parentPath;

		if(savePath.equals("/")){
			savePath += newChild;
		}
		else if(!newChild.startsWith("/"))
			savePath += "/"+ newChild;
		else
			savePath += newChild;

		if(savePath.endsWith("/"))
			savePath = savePath.substring(0, savePath.length()-1);

		boolean added = false;

		try {
			added = zkService.createZnode(savePath, dataTextArea.getText(), CreateMode.PERSISTENT);
		} catch (ZKDataException e) {
			log.error("problem adding child node: "+ e.getMessage(), e);
		}

		if(added){
			clearDataFields();
			getControllerStage().close();
			parentController.refresh();
			MainController.setMainContent(SceneName.GetZNode);
		}
	}

	@FXML
	private void clearButtonAction(){
		clearDataFields();
	}

	@FXML
	private void cancelButtonAction(){
		boolean cancel = true;

		if(!childTextField.getText().isEmpty() || !dataTextArea.getText().isEmpty()){
			cancel = AlertUtil.confirmationAlert("Cancel Add?", "Are you sure you want to cancel w/o saving data?", getControllerStage());
		}

		if(cancel) {
			clearDataFields();
			getControllerStage().close();
		}
	}

	private void clearDataFields(){
		childTextField.clear();
		dataTextArea.clear();
	}
}
