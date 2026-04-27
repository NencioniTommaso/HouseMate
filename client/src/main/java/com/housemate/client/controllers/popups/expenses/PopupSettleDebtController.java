package com.housemate.client.controllers.popups.expenses;

import com.housemate.client.controllers.MainController;
import com.housemate.client.service.AppServices;
import com.housemate.shared.dto.expense.request.SettlementCreateRequestDTO;
import com.housemate.shared.dto.expense.response.DebtResponseDTO;
import com.housemate.shared.dto.user.response.UserResponseDTO;
import com.housemate.shared.enums.MessageType;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import lombok.Setter;

import java.awt.*;
import java.math.BigDecimal;
import java.net.URI;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class PopupSettleDebtController {

    @FXML private StackPane popupSettleDebt;
    @FXML private Slider sldPaymentAmount;
    @FXML private TextField txtDescription, txtExactAmount;
    @FXML private Hyperlink hlCreditorLink;
    @FXML private Label lblCreditorIBAN;

    private final AppServices services;
    private final MainController mainController;
    private final Runnable onReturnCallback;
    private final DebtResponseDTO debtToSettle;

    public PopupSettleDebtController(AppServices services,
                                     MainController mainController,
                                     Runnable onReturnCallback,
                                     DebtResponseDTO debtToSettle){
        this.services = services;
        this.mainController = mainController;
        this.onReturnCallback = onReturnCallback;
        this.debtToSettle = debtToSettle;
    }

    @FXML
    public void initialize(){

        sldPaymentAmount.setMin(0.01);
        sldPaymentAmount.setMax(debtToSettle.amount().doubleValue());
        sldPaymentAmount.setValue(debtToSettle.amount().doubleValue());

        Tooltip valueTooltip = new Tooltip();
        valueTooltip.getStyleClass().add("slider-tooltip");

        sldPaymentAmount.valueProperty().addListener((obs, oldVal, newVal) -> {
            double value = newVal.doubleValue();
            double max = sldPaymentAmount.getMax();
            double step = 0.05;

            double snapped = Math.round(value / step) * step;
            double lastTick = Math.floor(max / step) * step;

            if (value > lastTick + 0.001) {
                snapped = max;
            }

            if (Math.abs(value - snapped) > 0.001) {
                sldPaymentAmount.setValue(snapped);
            } else {
                valueTooltip.setText(String.format("€ %.2f", snapped));
            }
            txtExactAmount.setText(String.valueOf(snapped));
        });

        sldPaymentAmount.setOnMousePressed(event -> valueTooltip.show(
                sldPaymentAmount,
                event.getScreenX() - (valueTooltip.getWidth() / 2),
                event.getScreenY() - 40
        ));

        sldPaymentAmount.setOnMouseDragged(event -> {
            double tooltipWidth = valueTooltip.getWidth();
            valueTooltip.setX(event.getScreenX() - (tooltipWidth / 2));
        });

        sldPaymentAmount.setOnMouseReleased(event -> valueTooltip.hide());

        txtExactAmount.textProperty().addListener((observable, oldValue, newValue) -> {
            if(txtExactAmount.getText().isBlank()){
                return;
            }
            if (!newValue.matches("\\d*(\\.\\d{0,2})?")) {
                txtExactAmount.setText(oldValue);
                return;
            }
            sldPaymentAmount.setValue(Double.parseDouble(newValue));
        });

        UserResponseDTO involvedUser = Optional.ofNullable(services.getSessionManager().getCurrentHouseholdMembers())
                .flatMap(members -> members.stream()
                        .filter(dto -> Objects.equals(dto.id(), debtToSettle.involvedId()))
                        .findFirst()).orElseThrow();

        hlCreditorLink.setText(involvedUser.paymentLink() != null ? involvedUser.paymentLink() : "Creditor has no payment link");
        lblCreditorIBAN.setText(involvedUser.iban() != null ? involvedUser.iban() : "Creditor has no IBAN");
    }

    @FXML
    public void handlePopupClosing() {
        mainController.closePopup(popupSettleDebt);
    }

    @FXML
    public void handleReturnToDebts() {
        onReturnCallback.run();
    }

    @FXML
    public void handleDebtSettling() {
        CompletableFuture.runAsync(() -> {
            try{
                services.getSettlementClientService().settleDebt(debtToSettle.debtId(),
                        new SettlementCreateRequestDTO(
                                debtToSettle.debtId(),
                                debtToSettle.involvedId(),
                                new BigDecimal(String.format(Locale.US, "%.2f", sldPaymentAmount.getValue())),
                                txtDescription.getText().isBlank() ? "Settlement" : txtDescription.getText()
                        ));

                Platform.runLater(() -> {
                    mainController.showToast("Debt settled successfully" , MessageType.SUCCESS);
                    onReturnCallback.run();
                    mainController.refreshDataAndReload();
                });
            }catch (RuntimeException e){
                Platform.runLater(() -> mainController.showToast("Failed to settle debt: " + e.getMessage(), MessageType.ERROR));
            }
        });
    }

    @FXML
    public void handleLinkClick() {
        if (hlCreditorLink == null || !hlCreditorLink.getText().startsWith("http")) {
            return;
        }

        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(hlCreditorLink.getText()));
            }
        } catch (Exception e) {
            mainController.showToast("Could not open payment link: " + e.getMessage(), MessageType.ERROR);
        }
    }
}
