package net.wakamesoba98.knitcap.view.listview;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Rectangle;
import net.wakamesoba98.knitcap.capture.packet.PacketHeader;
import net.wakamesoba98.knitcap.capture.packet.PayloadProtocol;

import java.io.File;
import java.io.IOException;

public class CellController extends ListCell<PacketHeader> {
    @FXML
    private AnchorPane anchorPane;
    @FXML
    private Label labelAddrSrc, labelPortSrc, labelAddrDst, labelPortDst, labelProtocol;
    @FXML
    private Rectangle rectangle;
    @FXML
    private ImageView imageConnStatus;

    private String txUri, rxUri, noneUri;
    private FXMLLoader loader;

    public CellController() {
        txUri = new File(getClass().getResource("/res/png/tx.png").getPath()).toURI().toString();
        rxUri = new File(getClass().getResource("/res/png/rx.png").getPath()).toURI().toString();
        noneUri = new File(getClass().getResource("/res/png/none.png").getPath()).toURI().toString();
    }

    @Override
    protected void updateItem(PacketHeader item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null) {
            setText(null);
            setGraphic(null);
        } else {
            if (loader == null) {
                loader = new FXMLLoader(getClass().getResource("/res/fxml/cell.fxml"));
                loader.setController(this);
                try {
                    loader.load();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            labelAddrSrc.setText(item.getSrcIpAddress());
            labelAddrDst.setText(item.getDstIpAddress());

            if (item.getSrcPort() > 0 && item.getDstPort() > 0) {
                labelPortSrc.setText(":" + item.getSrcPort());
                labelPortDst.setText(":" + item.getDstPort());
            } else {
                labelPortSrc.setText("");
                labelPortDst.setText("");
            }

            if (item.getProtocol() != null) {
                PayloadProtocol proto = item.getProtocol();
                if (proto == PayloadProtocol.OTHER) {
                    labelProtocol.setText("Other (" + item.getOtherProtocol() + ")");
                } else {
                    labelProtocol.setText(proto.toString());
                }
                rectangle.setFill(proto.getColor());
            }

            int fontSize = item.isIpV6() ? 14 : 22;
            labelAddrSrc.setStyle("-fx-font-size: " + fontSize);
            labelAddrDst.setStyle("-fx-font-size: " + fontSize);

            switch (item.getPacketType()) {
                case SEND:
                    imageConnStatus.setImage(new Image(txUri));
                    break;

                case RECEIVE:
                    imageConnStatus.setImage(new Image(rxUri));
                    break;

                default:
                    imageConnStatus.setImage(new Image(noneUri));
                    break;
            }

            setText(null);
            setGraphic(anchorPane);
        }
    }
}
