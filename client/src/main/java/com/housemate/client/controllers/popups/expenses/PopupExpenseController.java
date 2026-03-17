package com.housemate.client.controllers.popups.expenses;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

public class PopupExpenseController {

    @FXML private Button btnCloseAddExpense;
    @FXML private TextField txtExpenseDescription;
    @FXML private TextField txtExpenseAmount;
    @FXML private Button btnCreateExpense;
    @FXML private Button btnCancelExpense;
    @FXML private HBox contenitoreUtenti;

    @FXML
    public void initialize() {
        // TODO: Implementare la logica di inizializzazione
    }
}