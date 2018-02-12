Create a Secured Service via Spring Security ResourceServer with OAuth2/OpenId-Connect
---

A demo app to show 
- how to implement a backend service secured with OAuth2/OpenId-Connect using the `@EnableResourceServer` functionality of Spring Security.
- using JWS (RFC 7515 https://tools.ietf.org/html/rfc7515) to reduce the overhead when validating the JWT
- Only Spring Security Dependencies:
```
	compile('org.springframework.boot:spring-boot-starter-security')
	compile('org.springframework.security.oauth:spring-security-oauth2')
```

#### configuration:
The configuration is done in the `ResourceServerConfiguration.java`. The credentials are stored in the application.yml

#### setup of the keycloak server:
<pre>
Realm: demo-realm
|
|-Client: demo-client
|         | 
|         |- access-type: confidential
|         |- Service Account Enabled: true
|         |- authorization enabled: true
|         |- Role Mapping: assigned realm role: ROLE_DEMO_SPRING_SECURITY
|
|-Roles: ROLE_DEMO_SPRING_SECURITY
|
|-Key: $KEY (please copy from keycloak admin console)
|
</pre>

## Run the Demo

# Prepare Keycloak
Start a local Keycloak Instance. Using Docker is recommended.
```yaml
version: '2'
services:
  keycloak-service:
    image: jboss/keycloak
    ports:
      - 8280:8080
    environment:
      KEYCLOAK_USER: admin
      KEYCLOAK_PASSWORD: admin

```
- Open the Admin Console with http://localhost:8280/ entering admin/admin.
- Create the realm called: 'demo-realm'
- Import the demo-realm.json
- Open the "demo-client" tab "credentials" and generate a new secret
- Open the "Key" tab in Realm overview and copy the public RSA key into the application.yml

No you should be aple to optain an access token in your favourite REST client and run this demo.

---
# Exported Endpoints

- GET /person : responds a fixed list of *person* entities FOR authorised access only

**Hint:**
Keycloak uses a different format for its AccessTokens.
Something like:
```json
{
  "jti": "78c00562-d80a-4f5a-ab08-61ed10cb575c",
  "exp": 1509603570,
  "nbf": 0,
  "iat": 1509603270,
  "iss": "http://localhost:8280/auth/realms/demo-realm",
  "aud": "demo-client",
  "sub": "6ee90ba4-2854-49c1-9776-9aa95b6ae598",
  "typ": "Bearer",
  "azp": "demo-client",
  "auth_time": 0,
  "session_state": "68ce12fb-3b3f-429d-9390-0662f0503bbb",
  "acr": "1",
  "client_session": "ec0113e1-022a-482a-a26b-e5701e5edec1",
  "allowed-origins": [],
  "realm_access": {
    "roles": [
      "demo_user_role",
      "uma_authorization"
    ]
  },
  "resource_access": {
    "account": {
      "roles": [
        "manage-account",
        "manage-account-links",
        "view-profile"
      ]
    }
  },
  "name": "Jim Panse",
  "preferred_username": "demo-user",
  "given_name": "Jim",
  "family_name": "Panse",
  "email": "user@dmoain.com"
}
```

But the `DefaultAccessTokenConverter` of Spring Security expexts the authorities/roles in a claim `authorities`.
Which would pretty much look like:
```json
{
  "jti": "78c00562-d80a-4f5a-ab08-61ed10cb575c",
  "exp": 1509603570,
  "nbf": 0,
  "iat": 1509603270,
  "iss": "http://localhost:8280/auth/realms/demo-realm",
  "aud": "demo-client",
  "sub": "6ee90ba4-2854-49c1-9776-9aa95b6ae598",
  "typ": "Bearer",
  "azp": "demo-client",
  "auth_time": 0,
  "session_state": "68ce12fb-3b3f-429d-9390-0662f0503bbb",
  "acr": "1",
  "client_session": "ec0113e1-022a-482a-a26b-e5701e5edec1",
  "allowed-origins": [],
  "authorities": [
      "demo_user_role",
      "uma_authorization"
    ]
  ,
  "name": "Jim Panse",
  "preferred_username": "demo-user",
  "given_name": "Jim",
  "family_name": "Panse",
  "email": "user@dmoain.com"
}
```

To get it work it is necessary to override this method to this.

```java
    private class KeycloakAccessTokenConverter extends DefaultAccessTokenConverter {

        @Override
        public OAuth2Authentication extractAuthentication(Map<String, ?> map) {
            OAuth2Authentication oAuth2Authentication = super.extractAuthentication(map);
            Collection<GrantedAuthority> authorities = (Collection<GrantedAuthority>) oAuth2Authentication.getOAuth2Request().getAuthorities();
            if (map.containsKey("realm_access")) {
                Map<String, Object> realm_access = (Map<String, Object>) map.get("realm_access");
                if(realm_access.containsKey("roles")) {
                    ((Collection<String>) realm_access.get("roles")).forEach(r -> authorities.add(new SimpleGrantedAuthority(r)));
                }
            }
            return new OAuth2Authentication(oAuth2Authentication.getOAuth2Request(),oAuth2Authentication.getUserAuthentication());
        }
    }
```