package net.wakamesoba98.knitcap.capture.util;

import java.util.LinkedList;
import java.util.List;

public class IpV4Utils {

    public boolean isSameSubnetV4(String srcAddress, String dstAddress, String mask) {
        long srcValue = convertLong(srcAddress);
        long dstValue = convertLong(dstAddress);
        long maskValue = convertLong(mask);
        return (srcValue & maskValue) == (dstValue & maskValue);
    }

    public boolean isBroadcastV4(String dstAddress, String mask) {
        byte[] dstValue = convertByte(dstAddress);
        byte[] maskValue = convertByte(mask);
        for (int i = 0; i < 4; i++) {
            if (~maskValue[i] != (byte) (dstValue[i] & ~maskValue[i])) {
                return false;
            }
        }
        return true;
    }

    public List<String> getAddressListV4(String address, String mask) {
        List<String> result = new LinkedList<>();
        long dstValue = convertLong(address);
        long maskValue = convertLong(mask);
        long start = dstValue & maskValue;
        long range = getAddressRange(mask);
        for (int i = 1; i <= range; i++) {
            result.add(convertString(start + i));
        }
        return result;
    }

    private long getAddressRange(String address) {
        byte[] mask = convertByte(address);
        long result = 0;
        for (int i = 0; i < 4; i++) {
            int n = (byte) ~mask[i] & 0xFF;
            result = result << 8;
            result += n;
        }
        return result - 1;
    }

    private long convertLong(String address) {
        String[] octet = address.split("\\.");
        long result = 0;
        for (int i = 0; i < 4; i++) {
            int n = Integer.valueOf(octet[i]);
            result = result << 8;
            result += n;
        }
        return result;
    }

    private byte[] convertByte(String address) {
        String[] octet = address.split("\\.");
        byte[] result = new byte[4];
        for (int i = 0; i < 4; i++) {
            result[i] = Short.valueOf(octet[i]).byteValue();
        }
        return result;
    }

    private String convertString(long addressValue) {
        CharSequence[] array = new CharSequence[4];
        for (int i = 3; i >= 0; i--) {
            array[i] = String.valueOf(addressValue % 256);
            addressValue = addressValue >> 8;
        }
        return String.join(".", array);
    }
}
