package net.wakamesoba98.knitcap.view.listview;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Rectangle;
import net.wakamesoba98.knitcap.capture.packet.PacketHeader;
import net.wakamesoba98.knitcap.capture.packet.PayloadProtocol;

import java.io.File;

public class CellController {
    @FXML
    private Label labelAddrSrc, labelPortSrc, labelAddrDst, labelPortDst, labelProtocol;
    @FXML
    private Rectangle rectangle;
    @FXML
    private ImageView imageConnStatus;

    private String txUri, rxUri, noneUri;

    public CellController() {
        txUri = new File(getClass().getResource("../../png/tx.png").getPath()).toURI().toString();
        rxUri = new File(getClass().getResource("../../png/rx.png").getPath()).toURI().toString();
        noneUri = new File(getClass().getResource("../../png/none.png").getPath()).toURI().toString();
    }

    public void update(PacketHeader item) {
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
    }
}
