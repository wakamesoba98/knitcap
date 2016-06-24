package net.wakamesoba98.knitcap.view.listview;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.ListCell;
import net.wakamesoba98.knitcap.capture.packet.PacketHeader;

import java.io.IOException;
import java.io.UncheckedIOException;

public class PacketListCell extends ListCell<PacketHeader> {

    private CellController controller;

    @Override
    protected void updateItem(PacketHeader item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
            setGraphic(null);
            setText(null);
            controller = null;
            return;
        }

        if (controller == null || getGraphic() == null) {
            try {
                FXMLLoader fxmlLoader = new FXMLLoader();
                Node node = fxmlLoader.load(getClass().getResourceAsStream("../../fxml/cell.fxml"));
                controller = fxmlLoader.getController();
                setGraphic(node);
                controller.update(item);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        } else {
            controller.update(item);
        }
    }
}
