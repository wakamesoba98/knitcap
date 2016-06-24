package net.wakamesoba98.knitcap.view.listview;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import net.wakamesoba98.knitcap.capture.IfaceAddress;
import net.wakamesoba98.knitcap.capture.packet.PacketHeader;

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
        labelAddrSrc.setText(item.getSrcAddr());
        labelAddrDst.setText(item.getDstAddr());

        if (item.getSrcPort() > 0 && item.getDstPort() > 0) {
            labelPortSrc.setText(":" + item.getSrcPort());
            labelPortDst.setText(":" + item.getDstPort());
        } else {
            labelPortSrc.setText("");
            labelPortDst.setText("");
        }

        if (item.getProtocol() != null) {
            String proto = item.getProtocol().toString();
            Color color;
            switch (item.getProtocol()) {
                case TCP:
                    color = Color.BLUE;
                    break;

                case UDP:
                    color = Color.RED;
                    break;

                case ICMPv4:
                case ICMPv6:
                    color = Color.GREEN;
                    break;

                case ARP:
                    color = Color.PURPLE;
                    break;

                default:
                    color = Color.BLACK;
                    proto = "Other (" + item.getOtherProtocol() + ")";
                    break;
            }

            labelProtocol.setText(proto);
            rectangle.setFill(color);
        }

        int fontSize = item.isIpV6() ? 14 : 22;
        labelAddrSrc.setStyle("-fx-font-size: " + fontSize);
        labelAddrDst.setStyle("-fx-font-size: " + fontSize);

        IfaceAddress address = item.getIfaceAddress();
        String addrV4 = address.getAddrV4();
        String addrV6 = address.getAddrV6();
        if (item.getSrcAddr().equals(addrV4) || item.getSrcAddr().equals(addrV6)) {
            imageConnStatus.setImage(new Image(txUri));
        } else if (item.getDstAddr().equals(addrV4) || item.getDstAddr().equals(addrV6)) {
            imageConnStatus.setImage(new Image(rxUri));
        } else {
            imageConnStatus.setImage(new Image(noneUri));
        }
    }
}
