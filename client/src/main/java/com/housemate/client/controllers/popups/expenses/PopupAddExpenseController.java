package com.housemate.client.controllers.popups.expenses;

import com.housemate.client.controllers.MainController;
import com.housemate.client.service.AppServices;
import com.housemate.client.components.MemberSplitBox;
import com.housemate.shared.dto.expense.request.ExpenseCreateRequestDTO;
import com.housemate.shared.dto.expense.request.ExpenseShareRequestDTO;
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

        List<ExpenseShareRequestDTO> includedMembers = computeIncludedMembers();

        CompletableFuture.runAsync(() -> {
            try{
                services.getExpenseClientService()
                        .createExpense(new ExpenseCreateRequestDTO(
                            txtExpenseDescription.getText(),
                            new BigDecimal(txtExpenseAmount.getText()),
                            (ExpenseSplitType) splitMethodGroup.getSelectedToggle().getUserData(),
                            includedMembers
                        )
                );
                Platform.runLater(() -> {
                    handlePopupClosing();
                    mainController.showToast("Expense created successfully!", MessageType.SUCCESS);
                });

            }catch(RuntimeException e){
                Platform.runLater(() -> mainController.showToast("Error creating expense: " + e.getMessage(), MessageType.ERROR));
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
            // Add listener to CheckBox for EQUAL_SPLIT and ADJUSTMENT modes
            if (node.getChildren().stream().anyMatch(child -> child instanceof CheckBox)) {
                node.getChildren().stream()
                        .filter(child -> child instanceof CheckBox)
                        .map(child -> (CheckBox) child)
                        .findFirst()
                        .ifPresent(checkbox -> checkbox.selectedProperty().addListener(
                                (observableValue, oldVal, newVal) -> recalculatePreviews()
                        ));
            }

            // Add listener to Spinner for SHARES mode
            if (node.getChildren().stream().anyMatch(child -> child instanceof Spinner)) {
                node.getChildren().stream()
                        .filter(child -> child instanceof Spinner)
                        .map(child -> (Spinner<Integer>) child)
                        .findFirst()
                        .ifPresent(spinner -> spinner.valueProperty().addListener(
                                (observableValue, oldVal, newVal) -> recalculatePreviews()
                        ));
            }

            // Add listener to TextField for EXACT_AMOUNT and ADJUSTMENT modes
            if (node.getChildren().stream().anyMatch(child -> child instanceof TextField)) {
                node.getChildren().stream()
                        .filter(child -> child instanceof TextField)
                        .map(child -> (TextField) child)
                        .findFirst()
                        .ifPresent(textField -> textField.textProperty().addListener(
                                (observableValue, oldVal, newVal) -> recalculatePreviews()
                        ));
            }
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

                    getSplitBoxList().forEach(node -> {
                        if (node.isIncluded()) {
                            node.updateShareAmount(share);
                        } else {
                            node.updateShareAmount(BigDecimal.ZERO);
                        }
                    });
                } else {
                    getSplitBoxList().forEach(node -> node.updateShareAmount(BigDecimal.ZERO));
                }
            }
            case SHARES -> {
                int totalShares = getSplitBoxList().stream()
                        .map(MemberSplitBox::getShareCount)
                        .reduce(0, Integer::sum);
                
                if (totalShares > 0) {
                    BigDecimal shareAmount = totalAmount.divide(BigDecimal.valueOf(totalShares), 2, RoundingMode.HALF_UP);
                    getSplitBoxList().forEach(node -> 
                        node.updateShareAmount(shareAmount.multiply(BigDecimal.valueOf(node.getShareCount())))
                    );
                } else {
                    getSplitBoxList().forEach(node -> node.updateShareAmount(BigDecimal.ZERO));
                }
            }
            case EXACT_AMOUNT -> getSplitBoxList().forEach(MemberSplitBox::setExactAmount);
            case ADJUSTMENT -> {
                BigDecimal adjustmentSum = getSplitBoxList().stream()
                        .map(MemberSplitBox::getAdjustmentAmount)
                        .reduce(BigDecimal::add).orElse(BigDecimal.ZERO);

                if(totalAmount.compareTo(adjustmentSum) <= 0){
                    getSplitBoxList().forEach(MemberSplitBox::setExactAmount);
                    return;
                }

                totalAmount = totalAmount.subtract(adjustmentSum);

                long includedCount = getSplitBoxList().stream().filter(MemberSplitBox::isIncluded).count();
                if (includedCount > 0) {
                 BigDecimal baseShare = totalAmount.divide(BigDecimal.valueOf(includedCount), 2, RoundingMode.HALF_UP);

                    getSplitBoxList().forEach(node -> {
                        if (node.isIncluded()) {
                            BigDecimal userShare = baseShare.add(node.getAdjustmentAmount());
                            if(userShare.compareTo(BigDecimal.ZERO) < 0) {
                                userShare = BigDecimal.ZERO;
                            }
                            node.updateShareAmount(userShare);
                        } else {
                            node.updateShareAmount(BigDecimal.ZERO);
                        }
                    });
                } else {
                    getSplitBoxList().forEach(node -> node.updateShareAmount(BigDecimal.ZERO));
                }
            }
        }
    }

    private List<ExpenseShareRequestDTO> computeIncludedMembers() {
        switch ((ExpenseSplitType) splitMethodGroup.getSelectedToggle().getUserData()) {
            case EQUAL_SPLIT -> {
                return getSplitBoxList().stream()
                        .filter(MemberSplitBox::isIncluded)
                        .map(MemberSplitBox::toExpenseShare)
                        .toList();
            }
            case SHARES -> {
                return getSplitBoxList().stream()
                        .filter(box -> box.getShareCount() > 0)
                        .map(MemberSplitBox::toExpenseShare)
                        .toList();
            }
            case EXACT_AMOUNT -> {
                return getSplitBoxList().stream()
                        .map(MemberSplitBox::toExpenseShare)
                        .filter(expenseShare -> expenseShare.share().compareTo(BigDecimal.ZERO) > 0)
                        .toList();
            } case ADJUSTMENT -> {
                return getSplitBoxList().stream()
                        .filter(MemberSplitBox::isIncluded)
                        .map(box -> new ExpenseShareRequestDTO(box.getMember().id(), box.getAdjustmentAmount()))
                        .toList();
            }
            default -> throw new IllegalStateException("Unexpected value: " + splitMethodGroup.getSelectedToggle().getUserData());
        }
    }
}