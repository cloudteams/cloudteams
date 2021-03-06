package eu.cloudteams.util.bitbucket;

import java.util.Base64;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

/**
 *
 * @author Christos Paraskeva <ch.paraskeva at gmail dot com>
 */
public final class BitbucketAuthHandler {

    // Production client id
    private static final String CLIENT_ID = "6xQqvPuY6ZgFcsGmDj";

    // Production client secret
    private static final String CLIENT_SECRET = "8XUSDevvr8345HFcLWUYmHJSzn8YZ2Gk";

    private static final String BITBUCKET_API_URL = "https://bitbucket.org/site/oauth2/access_token";

    private static final String BASIC_AUTH = "Basic " + Base64.getEncoder().encodeToString((CLIENT_ID + ":" + CLIENT_SECRET).getBytes());

    public static BitbucketAuthResponse requestAccesToken(JSONObject jsonResponse) {
        BitbucketAuthResponse authResponse = new BitbucketAuthResponse();

        try {
            //Check if Bitbucket temporary code is received
            if (jsonResponse.has("code")) {

                //Set parameters of request
                MultiValueMap<String, String> parameteres = new LinkedMultiValueMap<>();
                String code = jsonResponse.get("code").toString();
                String cteamsusername = jsonResponse.get("username").toString();
                parameteres.add("code", code);
                parameteres.add("grant_type", "authorization_code");
                parameteres.add("redirect_uri", "https://cloudteams.euprojects.net/bitbucket/api/v1/bitbucket/auth?username=" + cteamsusername);
                HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(parameteres, getBasicAuthHeader());
                RestTemplate restTemplate = new RestTemplate();
                //Make Rest call to fetch AccessToken
                ResponseEntity<BitbucketAuthResponse> accesstokenResponse = restTemplate.postForEntity(BITBUCKET_API_URL, request, BitbucketAuthResponse.class);
                return accesstokenResponse.getBody();
            }
            authResponse.setError("BitbucketException");
            authResponse.setError_description("Could not get temporary code from Bitbucket API");
        } catch (Exception ex) {
            System.out.println(((HttpStatusCodeException) ex).getResponseBodyAsString());
            System.out.println("EX: " + ex.getMessage());
            authResponse.setError("JSONException");
            authResponse.setError_description(ex.getMessage());
        }
        return authResponse;
    }

    public static String refreshAccessToken(String refreshToken) throws Exception {
        MultiValueMap<String, String> parameteres = new LinkedMultiValueMap<>();
        parameteres.add("grant_type", "refresh_token");
        parameteres.add("refresh_token", refreshToken);
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(parameteres, getBasicAuthHeader());
        RestTemplate restTemplate = new RestTemplate();
        //Make Rest call to fetch AccessToken
        ResponseEntity<BitbucketAuthResponse> refreshtokenResponse = restTemplate.postForEntity(BITBUCKET_API_URL, request, BitbucketAuthResponse.class);

        if (refreshtokenResponse.getStatusCode() != HttpStatus.OK) {
            System.out.println(String.format("Could not use refresh token %s reason %s", refreshToken, refreshtokenResponse.getStatusCode().getReasonPhrase()));
            throw new Exception("Could not update access token");
        }

        return refreshtokenResponse.getBody().getAccess_token();
    }

    private static HttpHeaders getBasicAuthHeader() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", BASIC_AUTH);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        return headers;
    }

    public static void main(String... args) {
        BitbucketAuthHandler.requestAccesToken(new JSONObject().put("code", "wynkZNeUaa5aXXztzM"));
    }
}
