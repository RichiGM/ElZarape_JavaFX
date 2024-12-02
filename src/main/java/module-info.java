module org.utleon.elzarape {
    requires javafx.controls;
    requires javafx.fxml;
    requires unirest.java;
    requires com.google.gson;

    // Exporta el paquete principal y el modelo
    exports org.utl.elzarape;
    exports org.utl.elzarape.model;

    // Abre los paquetes necesarios para JavaFX y Gson
    opens org.utl.elzarape to javafx.fxml;
    opens org.utl.elzarape.model to javafx.fxml, com.google.gson;
}