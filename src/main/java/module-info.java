module gc.analise_de_malhas {
    requires javafx.controls;
    requires javafx.fxml;

    opens gc.analise_de_malhas to javafx.fxml;
    exports gc.analise_de_malhas;
}