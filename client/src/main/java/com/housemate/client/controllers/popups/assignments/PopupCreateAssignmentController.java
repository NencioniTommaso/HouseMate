package com.housemate.client.controllers.popups.assignments;
import com.housemate.client.controllers.MainController;
import com.housemate.client.service.AppServices;
import com.housemate.shared.dto.chore.request.ChoreAssignmentCreateRequestDTO;
import com.housemate.shared.dto.chore.response.ChoreResponseDTO;
import com.housemate.shared.dto.user.response.UserResponseDTO;
import com.housemate.shared.enums.MessageType;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.layout.StackPane;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;

public class PopupCreateAssignmentController {

    @FXML private ComboBox<ChoreResponseDTO> cmbChoreToAssign;
    @FXML private ComboBox<UserResponseDTO> cmbAssignUser;
    @FXML private DatePicker dateDueDate;
    @FXML private ComboBox<Integer> cmbHour, cmbMinute;

    @FXML private StackPane popupCreateAssignment;

    private final AppServices services;
    private final MainController mainController;

    public PopupCreateAssignmentController(AppServices services, MainController mainController) {
        this.services = services;
        this.mainController = mainController;
    }

    @FXML
    public void initialize() {

        cmbAssignUser.setConverter(new StringConverter<>() {
            @Override
            public String toString(UserResponseDTO user) {
                if (user == null) return "Select user...";
                return user.name() + " " + user.surname();
            }
            @Override
            public UserResponseDTO fromString(String string) {
                return null; //non-editable combo boxes do not require this
            }
        });

        cmbChoreToAssign.setConverter(new StringConverter<>() {
            @Override
            public String toString(ChoreResponseDTO chore) {
                if(chore == null) return "Select chore...";
                return chore.description();
            }
            @Override
            public ChoreResponseDTO fromString(String string) {
                return null;
            }
        });

        cmbHour.getItems().addAll(IntStream.rangeClosed(0, 23).boxed().toList());

        for (int i = 0; i < 60; i += 5) {
            cmbMinute.getItems().add(i);
        }

        dateDueDate.setValue(LocalDate.now());
        dateDueDate.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                LocalDate today = LocalDate.now();

                if (empty || date.isBefore(today)) {
                    setDisable(true);
                    setStyle("-fx-background-color: #e0e0e0;");
                }
            }
        });
    }

    @FXML
    public void handleAssignmentCreation(){
        ChoreResponseDTO selectedChore = cmbChoreToAssign.getValue();
        UserResponseDTO selectedUser = cmbAssignUser.getValue();

        LocalDate selectedDueDate = dateDueDate.getValue();
        LocalTime selectedDueTime = LocalTime.of(cmbHour.getValue(), cmbMinute.getValue());
        LocalDateTime dueDateTime = LocalDateTime.of(selectedDueDate, selectedDueTime);

        CompletableFuture.runAsync(() -> {

            try{
                services.getChoreClientService().createAssignment(new ChoreAssignmentCreateRequestDTO(
                        selectedChore.id(),
                        selectedUser.id(),
                        dueDateTime
                ));

                Platform.runLater(() -> {
                    mainController.showToast("Chore assignment created successfully!", MessageType.SUCCESS);
                    handlePopupClosing();
                });

            }catch(RuntimeException e){
                Platform.runLater(() -> {
                   mainController.showToast("Error creating chore assignment: " + e.getMessage(), MessageType.ERROR);
                    handlePopupClosing();
                });
            }
        });
    }

    @FXML
    public void handlePopupClosing(){
        cmbChoreToAssign.getItems().clear();
        cmbAssignUser.getItems().clear();
        dateDueDate.setValue(null);

        mainController.closePopup(popupCreateAssignment);
    }

    public void fetchChoresData() {

        cmbChoreToAssign.getItems().clear();

        CompletableFuture.runAsync(() -> {
            try {
                List<ChoreResponseDTO> currentChores = services.getChoreClientService()
                        .getAllHouseholdChores();

                Platform.runLater(() -> {
                    for (var chore : currentChores) {
                        cmbChoreToAssign.getItems().add(chore);
                    }
                });

            } catch (RuntimeException e) {
                Platform.runLater(() -> {
                    mainController.showToast("Error fetching chores: " + e.getMessage(), MessageType.ERROR);
                });
            }
        });
    }

    public void reloadMemberSelection() {
        cmbAssignUser.getItems().clear();
        //not a backend call, no separate thread required
        List<UserResponseDTO> members = services.getCurrentHousehold().members();

        for(var member : members){
            cmbAssignUser.getItems().add(member);
        }
    }
}