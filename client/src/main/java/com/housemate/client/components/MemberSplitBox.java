package com.housemate.client.components;

import com.housemate.shared.dto.expense.request.ExpenseShareRequestDTO;
import com.housemate.shared.dto.user.response.UserResponseDTO;
import com.housemate.shared.enums.ExpenseSplitType;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import lombok.Getter;

import java.math.BigDecimal;

public class MemberSplitBox extends VBox{

    @Getter
    private final UserResponseDTO member;

    private final Label lblAmount;
    private final Spinner<Integer> spnShare;
    private final TextField txtCustomAmount;
    private final CheckBox ckbIsIncluded;

    public MemberSplitBox(UserResponseDTO member, ExpenseSplitType mode) {
        this.getStyleClass().add("member-item");
        this.setAlignment(Pos.TOP_CENTER);
        this.setSpacing(8);
        this.setPrefWidth(120);
        this.setMaxWidth(120);
        this.member = member;

        Label lblName = new Label(member.name() + "\n" + member.surname());
        lblName.getStyleClass().add("member-name");
        lblName.setWrapText(true);
        lblName.setPrefWidth(150);
        lblName.setMaxWidth(150);
        lblName.setAlignment(Pos.CENTER);

        lblAmount = new Label("0.00");
        lblAmount.getStyleClass().add("expense-title");
        
        spnShare = new Spinner<>(0, Integer.MAX_VALUE, 0);
        spnShare.setPrefWidth(150);
        spnShare.setMaxWidth(150);
        
        txtCustomAmount = new TextField();
        txtCustomAmount.setPrefWidth(150);
        txtCustomAmount.setMaxWidth(150);
        txtCustomAmount.textProperty().addListener((observable, oldValue, newValue) -> {
            //only allows for numbers and up to 2 decimal places
            if (!newValue.matches("\\d*(\\.\\d{0,2})?")) {
                txtCustomAmount.setText(oldValue);
            }
        });
        
        ckbIsIncluded = new CheckBox();

        this.getChildren().addAll(lblName, lblAmount, spnShare, txtCustomAmount, ckbIsIncluded);

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
                ckbIsIncluded.setSelected(true);
                spnShare.setVisible(true);
                spnShare.setManaged(true);
            }
            case EXACT_AMOUNT -> {
                ckbIsIncluded.setSelected(true);
                txtCustomAmount.setVisible(true);
                txtCustomAmount.setManaged(true);
            }
            case ADJUSTMENT -> {
                txtCustomAmount.setVisible(true);
                txtCustomAmount.setManaged(true);
                ckbIsIncluded.setVisible(true);
                ckbIsIncluded.setManaged(true);
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

    public BigDecimal getAdjustmentAmount() {
        try {
            return new BigDecimal(txtCustomAmount.getText());
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }
}
