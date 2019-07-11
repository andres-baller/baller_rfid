import kong.unirest.Unirest;
import netscape.javascript.JSObject;
import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

class SnipeIt {

    private static final String API_KEY = "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImp0aSI6ImEyN2JjYzk3NjU1NGJhY2U4MDAxMTM3Zjc1MDYyMDVjYjhiNzJlZjhiODQ5ZjkyMzU1ZGJjYjRlZDRhOTRmOTlmMzczMTFkNmNmZDgyY2JmIn0.eyJhdWQiOiIxIiwianRpIjoiYTI3YmNjOTc2NTU0YmFjZTgwMDExMzdmNzUwNjIwNWNiOGI3MmVmOGI4NDlmOTIzNTVkYmNiNGVkNGE5NGY5OWYzNzMxMWQ2Y2ZkODJjYmYiLCJpYXQiOjE1NjE2NzM5MzMsIm5iZiI6MTU2MTY3MzkzMywiZXhwIjoxNTkzMjk2MzMzLCJzdWIiOiIxIiwic2NvcGVzIjpbXX0.piQk0VUB9wfFCP0ljPvgLoNha9rqHtHnyjx_R7hZkIAIl4Xer1DXfkXPqn9ynHXSI3SCDw9e6DCxBijXVwf7NoHfYV_TTksANjlwGWBviLXBANJDsz0-_2EmPn_y7YDjPbF0c5jauCj-V27ya8Fh2VXS3x27i8et4Zply6CyeoB106VYnGGo5S_fQkqzDoxqmHIf7PDvwF3WFKdnI0LkfChE09Ty_T7b0Z5dZdK9qeKKr5G0ecwt9Gi0wJ0QOruHtzzbf5KS-Z7n6NVPVxap7qlDF22Z96AWJNVEHs1ABnxRTvX5TS14vpq_whZnEJ9fbxZ-x1r44-RzFXkcwLGneCdznJpCkgiYTaWAVWggbg7aVCaY8H68pXM12H_FA34S6nHRCmkXBmy9DEjFiZNAmisUx6Rqv05DKmOMuyo9OywIzEVseybsvD5jRNsvCgz2zOiTL6i3PUkQbPMinxGMijndHkUGZKXabfIyYH-nry9_w1DEQQ8b3oGy2ljhXAfuKDhHauCs54ob9wQ5jpJ8igjusHYuQrpLCzo-1_hghcCU7JvLehybj1ehd1tVFwmwNUHO62jYuZ8xAEGWCm-ZP5UxR4pEEZR0yjp3Xe45blyB0m9O8tXbg5uYwKfzBg7X3QgfVVNOAJtloaRrXeb6Zy1bc9Ooxo5WXGBQ6StutiA";

    private static final String apiRoot = "http://10.0.3.70:2375/api/v1/"; // "http://localhost:2375/api/v1/"; // local

    /**
     * Given an asset tag, get the asset's corresponding ID from Snipe-It.
     *
     * @param assetTag The asset's tag ID.
     * @return The asset's Snipe-It ID.
     */
    static String fetchIdFromAssetTag(String assetTag) {
        JSONObject response = Unirest.get(apiRoot + "hardware/bytag/" + assetTag)
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + API_KEY)
                .asJson().getBody().getObject();

        if (response == null || (response.has("status") && response.get("status").equals("error"))) {
            System.out.println(response);
            return null;
        }

        return response.get("id").toString();
    }


    // TODO: Generalize this to accept a location ID.

    /**
     * Checkin an asset on Snipe-It based on its asset tag.
     *
     * @param tag The asset tag (EPC string)
     * @return `true` on success, `false` on error
     */
    static boolean checkinAsset(String tag, String locationId) {
        String id = fetchIdFromAssetTag(tag);

        if (id == null)
            return false;

        if (locationId == null)
            locationId = "";

        // First we have to get the current name, or else checking it in through the API will cause the name
        // to be erased. Pretty sure this is a bug in Snipe-It, but nothing we can do about it.
        JSONObject r = Unirest.get(apiRoot + "hardware/" + id)
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + API_KEY)
                .asJson().getBody().getObject();

        if (!r.has("name")) return false;
        String name = r.getString("name");

        JSONObject response = Unirest.post(apiRoot + "hardware/" + id + "/checkin")
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + API_KEY)
                .body("{\"note\":\"automated\", \"name\":\"" + name + "\", \"location_id\":\"" + locationId + "\"}")
                .asJson().getBody().getObject();

        System.out.println(response);

        return !(response.has("status") && response.get("status").equals("error"));
    }

    /**
     * Bulk check-in assets with given tag IDs.
     *
     * @param tags A list of tag IDs
     * @return the number of assets successfully checked in
     */
    static int checkinAssets(Collection<String> tags, String locationId) {
        int successCount = 0;
        for (String tag : tags) {
            if (checkinAsset(tag, locationId)) {
                resetKitId(tag);
                successCount++;
            }
        }

        return successCount;
    }

    /**
     * Checkout an asset on Snipe-It to a given location.
     *
     * @param tag        The asset tag (EPC string)
     * @param locationId The Snipe-It ID of the location to check out to
     * @return `true` on success, `false` on error
     */
    static boolean checkoutAsset(String tag, String locationId, String kitId) {
        String id = fetchIdFromAssetTag(tag);

        if (id == null)
            return false;

        JSONObject response = Unirest.post(apiRoot + "hardware/" + id + "/checkout")
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + API_KEY)
                .body("{\"note\":\"automated\",\"assigned_location\":\"" + locationId + "\",\"checkout_to_type\":\"location\"}")
                .asJson().getBody().getObject();

        System.out.println(response);

        return !((response.has("status") && response.get("status").equals("error")));
    }

    /**
     * Bulk checkout assets with given tag IDs.
     *
     * @param tags       A list of tag IDs
     * @param locationId The snipe-it ID of the location to check out to
     * @return The number of assets successfully checked out
     */
    static int checkoutAssets(Collection<String> tags, String locationId, String kitId) {
        int successCount = 0;
        for (String tag : tags) {
            if (checkoutAsset(tag, locationId, kitId)) {
                setKitId(tag, kitId);
                successCount++;
            }
        }

        return successCount;
    }

    private static void setKitId(String assetTag, String kitId) {
        String id = fetchIdFromAssetTag(assetTag);

        // System.out.println("kit id: " + kitId);

        String response = Unirest.patch(apiRoot + "hardware/" + id)
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + API_KEY)
                .body("{\"_snipeit_kit_id_2\":\"" + kitId + "\"}")
                .asString().getBody(); //.getBody().getObject();

        //System.out.println(response);
    }

    private static void resetKitId(String assetTag) {
        String id = fetchIdFromAssetTag(assetTag);

        Unirest.patch(apiRoot + "hardware/" + id)
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + API_KEY)
                .body("{\"_snipeit_kit_id_2\":\"\"}")
                .asString();
    }

    static List<AssetLocation> fetchLocations() {
        JSONObject response = Unirest.get(apiRoot + "locations")
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + API_KEY)
                .asJson().getBody().getObject();

        List<AssetLocation> locations = new ArrayList<>();
        for (Object row : response.getJSONArray("rows")) {
            JSONObject data = (JSONObject) row;

            AssetLocation location = new AssetLocation();
            location.id = data.get("id").toString();
            location.name = data.getString("name");

            try {
                String dateString = data.getJSONObject("created_at").getString("formatted");
                location.creationDate = new SimpleDateFormat("yyyy-MM-dd hh:mm a").parse(dateString);
            } catch (Exception e) {
                System.out.println(e.getLocalizedMessage());
            }

            locations.add(location);
        }

        return locations;
    }

    static String fetchAssetModel(String tag) {
        JSONObject response = Unirest.get(apiRoot + "hardware/bytag/" + tag)
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + API_KEY)
                .asJson().getBody().getObject();

        if (!response.has("model"))
            return null;

        return response.getJSONObject("model").getString("name");
    }

    static JSONObject fetchAsset(String tag) {
        JSONObject response = Unirest.get(apiRoot + "hardware/bytag/" + tag)
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + API_KEY)
                .asJson().getBody().getObject();

        return response;
    }

    static String findKitName(List<String> tags) {
        for (String tag : tags) {
            JSONObject asset = fetchAsset(tag);
            System.out.println(asset.toString());
            if (!asset.has("model"))
                continue;

            String model = asset.getJSONObject("model").getString("name");
            if (model.equals("Kit")) {
                return asset.getString("name");

                //return fetchIdFromAssetTag(tag);
                // Note: If we store the IDs from the call to fetchAssetModel, we don't need to make this additional call
            }
        }

        return null;
    }

    /**
     * Compare assets currenty located in a location to the assets found
     * in a scan, and then check in/out assets as necessary.
     *
     * @param tags
     * @param storageLocationId
     */
    static void updateStoredAssets(Collection<String> tags, String storageLocationId) {
        JSONObject response = Unirest.get(apiRoot + "hardware")
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + API_KEY)
                .queryString("location_id", storageLocationId)
                .asJson().getBody().getObject();

        JSONArray storedAssets = response.getJSONArray("rows");
        Set<String> storedAssetTags = new HashSet<>();
        for (Object asset : storedAssets) {
            storedAssetTags.add(((JSONObject) asset).getString("asset_tag"));
        }

        // Create set from scanned asset tags
        Set<String> scannedAssetTags = new HashSet<>(tags);

        // Check out any stored tags not found in the scan
        Set<String> assetTagsToCheckout = new HashSet<>(storedAssetTags);
        assetTagsToCheckout.removeAll(scannedAssetTags);

        // Check in any scanned tags not currently stored
        Set<String> assetTagsToCheckin = new HashSet<>(scannedAssetTags);
        assetTagsToCheckin.removeAll(storedAssetTags);

        // Check out any removed assets
        System.out.println("CHECK OUT:");
        if (assetTagsToCheckout.isEmpty()) {
            System.out.println("All assets accounted for. Nothing to check out.");
        } else { // Check out remaining assets
            SnipeIt.checkoutAssets(storedAssetTags, "6", "");
        }

        // Check in any introduced assets
        System.out.println("\nCHECK IN:");
        if (assetTagsToCheckin.isEmpty()) {
            System.out.println("No new assets found. Nothing to check in.");
        } else { // Check in new assets
            SnipeIt.checkinAssets(assetTagsToCheckin, storageLocationId);
        }

    }
}
