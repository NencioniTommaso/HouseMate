package com.housemate.client.controllers.popups.assignments;
import com.housemate.client.controllers.MainController;
import com.housemate.client.controllers.tabs.TabAssignmentsController;
import com.housemate.client.service.AppServices;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.layout.StackPane;

public class PopupAssignmentController {

    @FXML private ComboBox<?> cmbChoreToAssign;
    @FXML private ComboBox<?> cmbAssignUser;
    @FXML private DatePicker dateDueDate;

    @FXML private StackPane rootNode;

    private AppServices services;
    private final MainController mainController;
    private final TabAssignmentsController parentTab;

    public PopupAssignmentController(AppServices services, MainController mainController, TabAssignmentsController parentTab) {

        this.services = services;
        this.mainController = mainController;
        this.parentTab = parentTab;
    }

    @FXML
    public void initialize() {

    }

    @FXML
    private void handleAssignmentCreation(){

    }

    @FXML
    private void handlePopupClosing(){
        cmbChoreToAssign.getSelectionModel().clearSelection();
        cmbAssignUser.getSelectionModel().clearSelection();
        dateDueDate.setValue(null);

        mainController.closePopup(rootNode);
    }
}