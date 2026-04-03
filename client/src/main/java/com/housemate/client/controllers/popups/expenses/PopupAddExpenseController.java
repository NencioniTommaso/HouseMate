package com.housemate.client.controllers.popups.expenses;

import com.housemate.client.controllers.MainController;
import com.housemate.client.service.AppServices;
import com.housemate.client.utils.MemberSplitBox;
import com.housemate.shared.dto.expense.request.ExpenseCreateRequestDTO;
import com.housemate.shared.dto.user.response.UserResponseDTO;
import com.housemate.shared.enums.ExpenseSplitType;
import com.housemate.shared.enums.MessageType;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class PopupAddExpenseController {

    private final AppServices services;
    private final MainController mainController;

    @FXML private StackPane popupAddExpense;

    @FXML private TextField txtExpenseDescription;
    @FXML private TextField txtExpenseAmount;
    @FXML private RadioButton btnEqualSplit, btnSharesSplit, btnCustomSplit, btnAdjustmentSplit;
    @FXML private ToggleGroup splitMethodGroup;

    @FXML private HBox membersContainer;

    public PopupAddExpenseController(AppServices services, MainController mainController) {
        this.services = services;
        this.mainController = mainController;
    }

    @FXML
    public void initialize() {

        btnEqualSplit.setUserData(ExpenseSplitType.EQUAL_SPLIT);
        btnSharesSplit.setUserData(ExpenseSplitType.SHARES);
        btnCustomSplit.setUserData(ExpenseSplitType.EXACT_AMOUNT);
        btnAdjustmentSplit.setUserData(ExpenseSplitType.ADJUSTMENT);

        splitMethodGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                ExpenseSplitType selectedMode = (ExpenseSplitType) newVal.getUserData();

                membersContainer.getChildren().stream().filter(node -> node instanceof MemberSplitBox)
                        .map(node -> (MemberSplitBox) node)
                        .forEach(memberBox -> memberBox.setSplitMode(selectedMode));

                recalculatePreviews();
            }
        });

        txtExpenseAmount.textProperty().addListener((observable, oldValue, newValue) -> {
            //only allows for numbers and up to 2 decimal places
            if (!newValue.matches("\\d*(\\.\\d{0,2})?")) {
                txtExpenseAmount.setText(oldValue);
            }

            recalculatePreviews();
        });
    }

    @FXML
    public void handlePopupClosing() {
        membersContainer.getChildren().clear();
        mainController.closePopup(popupAddExpense);
    }

    @FXML
    public void handleExpenseCreation() {
        CompletableFuture.runAsync(() -> {
            try{
                services.getExpenseClientService()
                        .createExpense(new ExpenseCreateRequestDTO(
                            txtExpenseDescription.getText(),
                            new BigDecimal(txtExpenseAmount.getText()),
                            (ExpenseSplitType) splitMethodGroup.getSelectedToggle().getUserData(),
                            getSplitBoxList().stream().map(MemberSplitBox::toExpenseShare).toList()
                        )
                );
                Platform.runLater(() -> {
                    handlePopupClosing();
                    mainController.showToast("Expense created successfully!", MessageType.SUCCESS);
                });

            }catch(RuntimeException e){
                Platform.runLater(() -> {
                    handlePopupClosing();
                    mainController.showToast("Error creating expense: " + e.getMessage(), MessageType.ERROR);
                });
            }
        });
    }

    public void loadMembers() {

        List<UserResponseDTO> members = services.getCurrentHousehold().members();

        for (var member : members) {
            //explicitly casting because these user data are only assigned once in this constructor
            MemberSplitBox memberBox = new MemberSplitBox(member, (ExpenseSplitType) splitMethodGroup.getSelectedToggle().getUserData());
            membersContainer.getChildren().add(memberBox);
        }

        getSplitBoxList().forEach(node -> {
            node.getChildren().stream()
                    .filter(child -> child instanceof CheckBox)
                    .map(child -> (CheckBox) child)
                    .forEach(checkbox -> checkbox.selectedProperty().addListener(
                            (observableValue, oldVal, newVal) -> recalculatePreviews()
                    ));

            //the shares are always integers so this cast is safe
            node.getChildren().stream()
                    .filter(child -> child instanceof Spinner)
                    .map(child -> (Spinner<Integer>) child)
                    .forEach(spinner -> spinner.valueProperty().addListener(
                            (observableValue, oldVal, newVal) -> recalculatePreviews()
                    ));

            node.getChildren().stream()
                    .filter(child -> child instanceof TextField)
                    .map(child -> (TextField) child)
                    .forEach(textField -> textField.textProperty().addListener(
                            (observableValue, oldVal, newVal) -> recalculatePreviews()
                    ));
        });

    }

    private List<MemberSplitBox> getSplitBoxList() {
        return membersContainer.getChildren().stream()
                .filter(node -> node instanceof MemberSplitBox)
                .map(node -> (MemberSplitBox) node)
                .toList();
    }

    private void recalculatePreviews() {

        BigDecimal totalAmount;
        try {
            totalAmount = new BigDecimal(txtExpenseAmount.getText());
        } catch (NumberFormatException e) {
            totalAmount = BigDecimal.ZERO;
        }

        if (totalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            getSplitBoxList().forEach(node -> node.updateShareAmount(BigDecimal.ZERO));
            return;
        }

        ExpenseSplitType mode = (ExpenseSplitType) splitMethodGroup.getSelectedToggle().getUserData();

        switch (mode) {
            case EQUAL_SPLIT -> {
                long includedCount = getSplitBoxList().stream().filter(MemberSplitBox::isIncluded).count();
                if (includedCount > 0) {
                    BigDecimal share = totalAmount.divide(BigDecimal.valueOf(includedCount), 2, RoundingMode.HALF_UP);

                    for (MemberSplitBox node : getSplitBoxList()) {
                        if (node.isIncluded()) {
                            node.updateShareAmount(share);
                        } else {
                            node.updateShareAmount(BigDecimal.ZERO);
                        }
                    }
                }
            }
            case SHARES -> {
                int sharesCount = getSplitBoxList().stream().map(MemberSplitBox::getShareCount).reduce(0, Integer::sum);
                BigDecimal shareAmount = totalAmount.divide(BigDecimal.valueOf(sharesCount), RoundingMode.HALF_UP);

                for (MemberSplitBox node : getSplitBoxList()) {
                    node.updateShareAmount(shareAmount.multiply(BigDecimal.valueOf(node.getShareCount())));
                }
            }
            case EXACT_AMOUNT -> {

                for (MemberSplitBox node : getSplitBoxList()) {
                    node.setExactAmount();
                }
            }
        }
    }
}