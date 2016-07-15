package net.wakamesoba98.knitcap.geoip;

import com.maxmind.db.CHMCache;
import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CityResponse;
import com.maxmind.geoip2.record.Location;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;

public class GeoIPUtils {

    private DatabaseReader reader;

    public GeoIPUtils() {
        File database = new File(getClass().getResource("/res/db/GeoLite2-City.mmdb").getPath());
        try {
            reader = new DatabaseReader.Builder(database).withCache(new CHMCache()).build();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Location lookup(String ipV4Addr) throws GeoIp2Exception {
        if (reader == null) {
            return null;
        }
        try {
            InetAddress ipAddress = InetAddress.getByName(ipV4Addr);
            CityResponse response = reader.city(ipAddress);
            return response.getLocation();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
