package com.housemate.client.utils;

import com.housemate.shared.dto.expense.request.ExpenseShareRequestDTO;
import com.housemate.shared.dto.user.response.UserResponseDTO;
import com.housemate.shared.enums.ExpenseSplitType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;

public class MemberSplitBox extends VBox{

    private final UserResponseDTO member;

    private final Label lblAmount;
    private final Spinner<Integer> spnShare;
    private final TextField txtCustomAmount;
    private final CheckBox ckbIsIncluded;

    public MemberSplitBox(UserResponseDTO member, ExpenseSplitType mode) {
        this.member = member;

        lblAmount = new Label("0.00");
        lblAmount.getStyleClass().add("expense-title");
        spnShare = new Spinner<>(0, Integer.MAX_VALUE, 0);
        txtCustomAmount = new TextField();;
        txtCustomAmount.textProperty().addListener((observable, oldValue, newValue) -> {
            //only allows for numbers and up to 2 decimal places
            if (!newValue.matches("\\d*(\\.\\d{0,2})?")) {
                txtCustomAmount.setText(oldValue);
            }
        });
        ckbIsIncluded = new CheckBox();

        this.getChildren().addAll(lblAmount, spnShare, txtCustomAmount, ckbIsIncluded);

        lblAmount.setVisible(true);
        lblAmount.setManaged(true);

        setSplitMode(mode);

    }

    public void setSplitMode(ExpenseSplitType mode) {
        spnShare.setVisible(false);
        spnShare.setManaged(false);
        ckbIsIncluded.setVisible(false);
        ckbIsIncluded.setManaged(false);
        txtCustomAmount.setVisible(false);
        txtCustomAmount.setManaged(false);

        switch (mode) {
            case SHARES -> {
                spnShare.setVisible(true);
                spnShare.setManaged(true);
            }
            case ADJUSTMENT, EXACT_AMOUNT -> {
                txtCustomAmount.setVisible(true);
                txtCustomAmount.setManaged(true);
            }
            case EQUAL_SPLIT -> {
                ckbIsIncluded.setVisible(true);
                ckbIsIncluded.setManaged(true);
            }
        }
    }

    public ExpenseShareRequestDTO toExpenseShare() {
        return new ExpenseShareRequestDTO(member.id(), new BigDecimal(lblAmount.getText()));
    }

    public void updateShareAmount(BigDecimal amount) {
        lblAmount.setText(String.valueOf(amount));
    }

    public void setExactAmount() {
        lblAmount.setText(txtCustomAmount.getText());
    }

    public int getShareCount() {
        return spnShare.getValue();
    }

    public boolean isIncluded(){
        return ckbIsIncluded.isSelected();
    }
}
