package be.yvanmazy.remotedminecraft.version;

import java.util.Map;

public record Assets(Map<String, Data> objects) {

    public record Data(String hash) {

    }

}