package com.migs.zk.inspectorzk.ui.controllers;

import com.migs.zk.inspectorzk.domain.PrefKeys;
import com.migs.zk.inspectorzk.domain.ZKAclData;
import com.migs.zk.inspectorzk.services.ZKDataException;
import com.migs.zk.inspectorzk.domain.ZKDataResult;
import com.migs.zk.inspectorzk.services.ZKService;
import com.migs.zk.inspectorzk.ui.util.AlertUtil;
import com.migs.zk.inspectorzk.ui.util.LimitedPathHistory;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.MapValueFactory;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.zookeeper.data.ACL;

import static com.migs.zk.inspectorzk.ui.util.Txt.frm;

import java.io.IOException;
import java.util.*;

/**
 * Created by migc on 1/13/18.
 */
public class GetZNodeController {

	private static Logger log = LogManager.getLogger(GetZNodeController.class);
	private static final int HISTORY_LIMIT = 10;

	private Parent topNode;

	@FXML
	private TextField pathTextField;

	@FXML
	private TextArea dataTextArea;

	@FXML
	private TabPane resultsTabPane;

	@FXML
	private Tab infoTab;

	@FXML
	private Tab aclTab;

	@FXML
	private Tab childrenTab;

	@FXML
	private Button getButton;

	@FXML
	private Button clearButton;

	@FXML
	private Button backButton;

	@FXML
	private Button forwardButton;

	@FXML
	private Button clearHistoryButton;

	@FXML
	private Button closeButton;

	@FXML
	private TableView<ZKAclData> aclTable;

	@FXML
	private TableColumn<ZKAclData, String> idCol;

	@FXML
	private TableColumn<ZKAclData, String> schemeCol;

	@FXML
	private TableColumn<ZKAclData, String> permsCol;

	@FXML
	private ListView<String> childListView;

	@FXML
	private TableView statTable;

	@FXML
	private TableColumn<Map, String> statKeyCol;

	@FXML
	private TableColumn<Map, String> statValCol;

	@FXML
	private Button saveButton;

	@FXML
	private Button addChildButton;

	@FXML
	private Button deleteChildButton;

	@FXML
	private Button addAclButton;

	@FXML
	private Button deleteAclButton;

	private ZKService zkService;
	private static String lastPath;
	private static String origDataValue;
	private static LimitedPathHistory limitedPathHistory;

	private static AddChildNodeController addChildNodeController;
	private static AddAclController addAclController;

	private SimpleBooleanProperty disableChildButtons;
	private SimpleBooleanProperty disableAclButtons;

	@FXML
	private void initialize(){
		idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
		schemeCol.setCellValueFactory(new PropertyValueFactory<>("scheme"));
		permsCol.setCellValueFactory(new PropertyValueFactory<>("perms"));

		statKeyCol.setCellValueFactory(new MapValueFactory<>("key"));
		statValCol.setCellValueFactory(new MapValueFactory<>("val"));

		zkService = ZKService.getInstance();

		// check for last path
		refreshLastPath();

		dataTextArea.textProperty().addListener((observable, oldValue, newValue) -> {
			newValue = newValue.trim();
			if(origDataValue == null || origDataValue.isEmpty()) {
				saveButton.setDisable(true);
			}
			else if(origDataValue.equals(newValue)){
				saveButton.setDisable(true);
			}
			else{
				saveButton.setDisable(false);
			}
		});

		// init history
		limitedPathHistory = new LimitedPathHistory(HISTORY_LIMIT);

		// init child controller
		FXMLLoader fldr = null;
		try {
			fldr = new FXMLLoader(getClass().getResource("addchild-window.fxml"));
			Parent addChildParent = fldr.load();

			if(addChildParent != null){
				addChildNodeController = fldr.getController();
				addChildNodeController.setControllerScene(new Scene(addChildParent, 400, 300));
				addChildNodeController.setControllerStage(new Stage(StageStyle.UTILITY));
			}

			fldr = new FXMLLoader(getClass().getResource("add-acl-window.fxml"));
			Parent aclParent = fldr.load();
			addAclController = fldr.getController();
			addAclController.setControllerScene(new Scene(aclParent, 400, 300));
			addAclController.setControllerStage(new Stage(StageStyle.UTILITY));
			addAclController.getControllerStage().setOnCloseRequest(we -> {
				log.debug(frm("we: %s", we.getEventType().getName()));
				refresh();
			});
		} catch (IOException e) {
			log.error(e.getMessage());
		}

		resultsTabPane.getSelectionModel().selectedItemProperty().addListener((obs, ov, nv) ->{
			log.debug(frm("newVal: %s", nv.getText()));

			if(nv.getId().equals(childrenTab.getId())){
				disableChildButtons.set(false);
				disableAclButtons.set(true);
			}

			if(nv.getId().equals(aclTab.getId())){
				disableAclButtons.set(false);
				disableChildButtons.set(true);

			}
			if(nv.getId().equals(infoTab.getId())){
				disableChildButtons.set(true);
				disableAclButtons.set(true);
			}
		});

		// props
		disableAclButtons = new SimpleBooleanProperty(true);
		disableChildButtons = new SimpleBooleanProperty(true);

		// bindings
		backButton.disableProperty().bind(limitedPathHistory.atFrontProperty());
		forwardButton.disableProperty().bind(limitedPathHistory.atBackProperty());
		clearHistoryButton.disableProperty().bind(limitedPathHistory.getCurrentHistorySize().isEqualTo(0));

		resetButtonBindings();
	}

	protected void refreshLastPath(){
		lastPath = MainController.getPrefValue("LAST_PATH");
		if(lastPath != null && !lastPath.isEmpty())
			pathTextField.textProperty().set(lastPath);
		else
			pathTextField.textProperty().set("/");

		pathTextField.positionCaret(pathTextField.textProperty().length().get());
		pathTextField.requestFocus();
	}

	private void resetButtonBindings(){
		addChildButton.disableProperty().bind(Bindings.size(statTable.getItems()).isEqualTo(0).or(disableChildButtons));
		deleteChildButton.disableProperty().bind(childListView.getSelectionModel().selectedItemProperty().isNull().or(disableChildButtons));

		addAclButton.disableProperty().bind(Bindings.size(aclTable.getItems()).isEqualTo(0).or(disableAclButtons));
		deleteAclButton.disableProperty().bind(aclTable.getSelectionModel().selectedItemProperty().isNull().or(disableAclButtons));
	}

	@FXML
	private void getZnodeDataButtonAction(){
		origDataValue = new String();

		String path = pathTextField.textProperty().get();
		clearZNodeData();
		pathTextField.textProperty().set(path);

		limitedPathHistory.addPath(path);
		MainController.setPref(PrefKeys.LAST_PATH_PREF, path);

		if(path != null && !path.isEmpty()){

			// info tab
			try {
				ZKDataResult dataResult = zkService.getZNodeData(path);

				if(dataResult != null) {
					origDataValue = dataResult.getZnodeData();

					dataTextArea.textProperty().set(dataResult.getZnodeData());
					statTable.setItems(FXCollections.observableList(dataResult.getStatMapList()));
					MainController.updateStageTitle(path, false);

					disableChildButtons.set(false);
				}
				else
					dataTextArea.setText("No Data Available");

				log.debug("statTable items = "+ statTable.getItems().size());
			} catch (ZKDataException e) {
				dataTextArea.setText(e.getMessage());
				statTable.getItems().clear();
				statTable.setPlaceholder(new Text(e.getMessage()));
			}

			// acl tab
			try {
				Map<String, ACL> aclMap = zkService.getAclMap(path);

				if(aclMap != null){
					List<ZKAclData> aclList = new ArrayList<>();
					aclMap.values().forEach( acl -> aclList.add(new ZKAclData(acl)) );
					aclTable.getItems().addAll(FXCollections.observableList(aclList));
				}
			} catch (ZKDataException e) {
				aclTable.setPlaceholder(new Text(e.getMessage()));
				disableAclButtons.set(true);
			}

			// children tab
			try {
				List<String> childList = zkService.getZnodeChildren(path);

				if(childList != null && childList.size() > 0)
					childListView.getItems().addAll(childList);
				else
					childListView.setPlaceholder(new Text("This Znode has no children"));
			} catch (ZKDataException e) {
				childListView.setPlaceholder(new Text(e.getMessage()));
			}

			// reset buttons
			resetButtonBindings();
		}
	}

	protected void refresh(){
		getZnodeDataButtonAction();
	}

	@FXML
	protected void clearPathHistory(){
		limitedPathHistory.clearHistory();
	}

	@FXML
	private void addChildButtonAction(){
		addChildNodeController.setParentPathText(pathTextField.getText());
		addChildNodeController.setParentController(this);
		addChildNodeController.show();
	}

	@FXML
	private void clearZNodeData(){
		log.debug("Clearing scene data");
		pathTextField.textProperty().set("/");
		dataTextArea.clear();
		aclTable.getItems().clear();
		statTable.getItems().clear();
		childListView.getItems().clear();

		saveButton.setDisable(true);
	}

	@FXML
	private void backButtonAction(){
		String path = limitedPathHistory.getPreviousPath();
		pathTextField.textProperty().set( path );
		getZnodeDataButtonAction();
	}

	@FXML
	private void forwardButtonAction(ActionEvent ae){
		String path = limitedPathHistory.getNextPath();
		pathTextField.textProperty().set( path );
		getZnodeDataButtonAction();
	}

	@FXML
	private void closeButtonAction(){
		MainController.clearContentPane();
	}

	@FXML
	private void childListViewDoubleClickAction(MouseEvent me){
		if(me.getButton() == MouseButton.PRIMARY && me.getClickCount() == 2){
			log.debug("Double click detected");

			String selected = childListView.getSelectionModel().getSelectedItem();
			log.debug("Selected: "+ selected);

			if(selected != null && !selected.isEmpty()){
				String currentPath = pathTextField.textProperty().get();

				String newPath = "";
				if(currentPath.trim().equals("/"))
					newPath = currentPath.trim() + selected;
				else
					newPath = currentPath +"/"+ selected;

				pathTextField.textProperty().set(newPath);

				// trigger znode data refresh
				getZnodeDataButtonAction();
			}
		}
	}

	@FXML
	private void deleteChildButtonAction(){
		String fullPath = pathTextField.getText() +"/"+ childListView.getSelectionModel().getSelectedItem();
		log.debug("About to delete child node: "+ childListView.getSelectionModel().getSelectedItem());
		log.debug("full path = "+ fullPath);

		boolean yes = AlertUtil.confirmationAlert("Delete Child Node!!!", "Are you sure you want to delete: "+ fullPath);

		if(yes){
			boolean deleted = false;

			try {
				deleted = zkService.deleteZnode(fullPath);
			} catch (ZKDataException e) {
				log.error("problem deleting: "+ fullPath +" "+ e.getMessage());
				AlertUtil.errorAlert("Error Deleting", "Error encountered while attempting to delete: "+ fullPath +"\n"+ e.getMessage(), null);
			}

			if(deleted){
				limitedPathHistory.deleteCurrentPath();
				refresh();
			}
		}
	}

	@FXML
	private void saveButtonAction(){
		String path = pathTextField.getText().trim();
		String data = dataTextArea.getText().trim();

		boolean updated = false;

		if(!path.isEmpty()){
			try {
				updated = zkService.setZNodeData(path, data);
			} catch (ZKDataException e) {
				log.error(frm("Error trying to update Znode[%s] w Data[%s]", path, data));
				AlertUtil.errorAlert("Error Saving Data", "An error happened while attempting to save the data:\n\n"+e.getMessage(), null);
			}
		}

		if(updated){
			getZnodeDataButtonAction();
		}
	}

	@FXML
	private void addAclButtonAction(){
		addAclController.setAclPath(pathTextField.getText().trim());
		addAclController.show();
	}

	@FXML
	private void removeAclButtonAction(){
		ZKAclData aclData = aclTable.getSelectionModel().selectedItemProperty().getValue();
		String path = pathTextField.getText();
		log.debug(frm("Removing ACL for: %s & %s", aclData.getId(), path));

		boolean removed = false;

		boolean confirmDel = AlertUtil.
					confirmationAlert(
							"Confirm ACL Delete",
							frm("Are you sure you want to remove the following ACL:\n\n%s", aclData.getId())
					);

		if(confirmDel){
			try {
				removed = zkService.removeAclForZnode(path, aclData.getId());
			} catch (ZKDataException e) {
				log.error(e);
				AlertUtil.errorAlert("ACL Error", "Problem removing ACL", null);
			}

			if(removed)
				refresh();
		}
	}

	protected void setTopNode(Parent parentNode){
		this.topNode = parentNode;
	}

	protected Parent getTopNode(){
		return this.topNode;
	}
}
