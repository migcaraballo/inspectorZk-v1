package com.migs.zk.inspectorzk.ui.controllers;

import com.migs.zk.inspectorzk.domain.PrefKeys;
import com.migs.zk.inspectorzk.domain.Session;
import com.migs.zk.inspectorzk.domain.ZKConnInfo;
import com.migs.zk.inspectorzk.services.ZKService;
import com.migs.zk.inspectorzk.ui.util.AlertUtil;

import static com.migs.zk.inspectorzk.ui.util.Txt.frm;

import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Arrays;
import java.util.prefs.Preferences;

/**
 * Created by migc on 1/11/18.
 */
public class MainController {

	private static Logger log = LogManager.getLogger(MainController.class);
	private static BorderPane bpMainPane;

	@FXML
	private MenuItem openConnectionItem;

	@FXML
	private MenuItem disconnectItem;

	@FXML
	private MenuItem authItem;

	@FXML
	private MenuItem exitItem;

	@FXML
	MenuItem manualItem;

	@FXML
	private MenuItem getZNodeItem;

	@FXML
	private MenuItem clearLastPathItem;

	@FXML
	private MenuItem clearPreviousHostsItem;

	@FXML
	private Text statusText;

	@FXML
	private Text authStatusText;

	private static SimpleStringProperty updatableStatusText;
	private static SimpleBooleanProperty disconnectedProperty;
	private static SimpleObjectProperty<Color> statusTextColorProperty;

	private ZKService zkService;
	private Session session;

	private static Stage mainStage;
	private static OpenConnectionController openConnectionController;
	private static GetZNodeController getZNodeController;
	private static ManualController manualController;

	private static Node authNode;
	private static Node currentNode;
	private static Node emptyNode;

	private static Preferences appPrefs;

	@FXML
	private void initialize(){
		log.info("Initializing MainController...");

		zkService = ZKService.getInstance();

		// init session
		session = Session.getSession();

		// init prefs
		appPrefs = Preferences.userRoot();

		try {
			FXMLLoader fldr = new FXMLLoader(getClass().getResource("open-connection-w-prev.fxml"));
			Parent openConnNode = fldr.load();
			openConnectionController = fldr.getController();
			openConnectionController.setTopNode(openConnNode);

			fldr = new FXMLLoader(getClass().getResource("auth-digest-only.fxml"));
			authNode = fldr.load();

			fldr = new FXMLLoader(getClass().getResource("get-znode-edits.fxml"));
			Parent getZnodeParent = fldr.load();
			getZNodeController = fldr.getController();
			getZNodeController.setTopNode(getZnodeParent);

			fldr = new FXMLLoader(getClass().getResource("title-linked.fxml"));
			emptyNode = fldr.load();

			// manual controller setup
			fldr = new FXMLLoader(getClass().getResource("manual-window.fxml"));
			Parent manualParent = fldr.load();
			manualController = fldr.getController();

			manualController.setControllerScene(new Scene(manualParent, 350, 600));
			manualController.setControllerStage(new Stage(StageStyle.UTILITY));
		} catch (IOException e) {
			e.printStackTrace();
		}

		// bindings
		setUpBindings();
	}

	private void setUpBindings(){
		updatableStatusText = new SimpleStringProperty("Not Connected");
		statusText.setFill(Color.RED);
		statusText.textProperty().bind(updatableStatusText);
		authStatusText.textProperty().bind(session.authUserProperty());

		// on app startup, there shoudln't be any connections
		disconnectedProperty = new SimpleBooleanProperty(true);

		// other bindings
		authItem.disableProperty().bind(disconnectedProperty);
		disconnectItem.disableProperty().bind(disconnectedProperty);
		getZNodeItem.disableProperty().bind(disconnectedProperty);
		openConnectionItem.disableProperty().bind(disconnectedProperty.not());

		statusTextColorProperty = new SimpleObjectProperty<>(Color.RED);
		statusText.fillProperty().bind(statusTextColorProperty);
	}

	@FXML
	private void openConnectionItemAction(ActionEvent ae){
		log.debug("Open ZK Connection Scene");

		if(openConnectionController != null) {
			setMainContent(openConnectionController.getTopNode());
		}
	}

	@FXML
	private void disconnectItemAction(ActionEvent ae){
		ZKConnInfo ci = session.getCurrentConnectionInfo();

		boolean disconnect =
					AlertUtil.confirmationAlert(
							"Disconnect Confirmation",
							String.format("Are you sure you wan to disconnect from:\n%s:%d", ci.getHost(), ci.getPort())
					);

		if(disconnect){
			disconnectFromZK();
			session.setDisconnected();
			session.authUserProperty().set("!authed");
		}
	}

	private void disconnectFromZK(){
		ZKConnInfo ci = session.getCurrentConnectionInfo();

		if(ci != null){
			log.debug(String.format("Disconnecting from: %s:%d\n", ci.getHost(), ci.getPort()));
			zkService.disconnectFromZK();
			setStageTitle("!connected");
		}

		setConnectionStatus("Not Connected");
		clearContentPane();

		disconnectedProperty.set(true);
		statusTextColorProperty.set(Color.RED);
	}

	@FXML
	private void authItemAction(ActionEvent ae){
		log.debug("Opening Authentication Scene");

		if(authNode != null){
			setMainContent(authNode);
		}
	}

	@FXML
	private void getZNodeItemAction(){
		log.debug("Open GetZnode Scene");

		if(getZNodeController != null) {
			setMainContent(getZNodeController.getTopNode());
		}
	}

	@FXML
	private void manualItemAction(ActionEvent ae){
		log.debug("manual item clicked");
		manualController.show();
	}

	@FXML
	private void clearLastPathAction(){
		String lastPath = getPrefValue(PrefKeys.LAST_PATH_PREF);

		if(lastPath != null && !lastPath.isEmpty()){
			boolean remove = AlertUtil.confirmationAlert(
					"Confirm Clear?",
					frm("Sure you want to clear last path of:\n\n%s", lastPath)
			);

			if(remove){
				removePref(PrefKeys.LAST_PATH_PREF);
				getZNodeController.refreshLastPath();
			}
		}
		else{
			AlertUtil.infoAlert("No Last Path", "There is no last path to clear", null);
		}
	}

	@FXML
	private void clearPreviousHostAction(){
		int prevHostNum = openConnectionController.totalPreviousHosts();

		if(prevHostNum > 0){
			boolean remove = AlertUtil.confirmationAlert(
					"Confirm Clear?",
					frm("Sure you want to clear all %d previous hosts?", prevHostNum));

			if(remove){
				removePref(PrefKeys.LAST_HOST_LIST_PREF);
				openConnectionController.loadPreviousHostsList();
			}
		}
		else
			AlertUtil.infoAlert("No Previous Hosts", "There are no previous hosts to clear", null);
	}

	@FXML
	private void exitAction(ActionEvent ae){
		log.info("Exiting...");
		disconnectFromZK();
		Platform.exit();
	}

	protected static void setMainContent(Node node){
		currentNode = node;
		bpMainPane.setCenter(node);
	}

	protected static void setMainContent(SceneName name){
		if(name == SceneName.Main)
			clearContentPane();
		if(name == SceneName.OpenConnection)
			setMainContent(openConnectionController.getTopNode());
		if(name == SceneName.Auth)
			setMainContent(authNode);
		if(name == SceneName.GetZNode)
			setMainContent(getZNodeController.getTopNode());
	}

	protected static void clearContentPane(){
		currentNode = emptyNode;
		bpMainPane.setCenter(emptyNode);
	}

	protected static void setPref(String key, String value){
		appPrefs.put(key, value);
	}

	protected static String getPrefValue(String key){
		return appPrefs.get(key, "");
	}

	protected static void removePref(String key){
		appPrefs.remove(key);
	}

	protected static void setStageTitle(String title){
		if(title != null && !title.isEmpty())
			mainStage.setTitle(title);
		else
			mainStage.setTitle("InspectorZK");
	}

	protected static void updateStageTitle(String msg, boolean append){
		String currTitle = mainStage.getTitle();
		String mainPart = null;

		if(currTitle.contains("|"))
			mainPart = Arrays.asList(currTitle.split("\\|")).get(0);
		else
			mainPart = currTitle;

		//todo: unhack the trim
		if(append){
			mainStage.setTitle(currTitle.trim() +" "+ msg);
		}
		else
			mainStage.setTitle(mainPart.trim() +" | "+ msg);
	}

	protected static void changeToWaitCursor(){
		currentNode.setCursor(Cursor.WAIT);
	}

	protected static void changeToNormalCursor(){
		currentNode.setCursor(Cursor.DEFAULT);
	}

	protected static void setConnectionStatus(String msg){
		updatableStatusText.set(msg);
		disconnectedProperty.set(false);
		statusTextColorProperty.set(Color.GREEN);
	}

	public static void setMainStage(Stage stage){
		mainStage = stage;

		Parent parent = mainStage.getScene().getRoot();
		if(parent != null)
			log.debug(frm("parent info: %s", parent.getId()));

		bpMainPane = (BorderPane) parent;

		bpMainPane.centerProperty().addListener((observable, oldValue, newValue) -> {
			String nodeName = observable.getValue().getId();
			log.debug("center changed to: "+ nodeName);
		});

		clearContentPane();
	}
}
