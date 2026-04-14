package com.housemate.client.controllers.popups.expenses;

import com.housemate.client.controllers.MainController;
import com.housemate.client.service.AppServices;
import com.housemate.shared.dto.expense.request.SettlementCreateRequestDTO;
import com.housemate.shared.dto.expense.response.DebtResponseDTO;
import com.housemate.shared.enums.MessageType;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.StackPane;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public class PopupSettleDebtController {

    @FXML private StackPane popupSettleDebt;
    @FXML private Slider sldPaymentAmount;
    @FXML private TextField txtDescription;

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

            double snapped = Math.round(value * 10.0) / 10.0;
            double lastTick = Math.floor(max * 10.0) / 10.0;

            if (value > lastTick + 0.001) {
                snapped = max;
            }

            if (Math.abs(value - snapped) > 0.001) {
                sldPaymentAmount.setValue(snapped);
            } else {
                valueTooltip.setText(String.format("€ %.2f", snapped));
            }
        });

        sldPaymentAmount.setOnMousePressed(event -> {
            valueTooltip.show(sldPaymentAmount,
                    event.getScreenX() - (valueTooltip.getWidth() / 2),
                    event.getScreenY() - 40
            );
        });

        sldPaymentAmount.setOnMouseDragged(event -> {
            double tooltipWidth = valueTooltip.getWidth();
            valueTooltip.setX(event.getScreenX() - (tooltipWidth / 2));
        });

        sldPaymentAmount.setOnMouseReleased(event -> {
            valueTooltip.hide();
        });
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
                    mainController.showToast("Debt settled succesfully" , MessageType.SUCCESS);
                    onReturnCallback.run();
                });
            }catch (RuntimeException e){
                Platform.runLater(() -> {
                    mainController.showToast("Failed to settle debt: " + e.getMessage(), MessageType.ERROR);
                });
            }
        });
    }
}
