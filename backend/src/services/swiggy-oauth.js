import {
    discoverOAuthServerInfo,
    registerClient,
    startAuthorization,
    exchangeAuthorization
} from "@modelcontextprotocol/sdk/client/auth.js";

const SWIGGY_MCP_URL =
    "https://mcp.swiggy.com/food";

const REDIRECT_URI =
    process.env.SWIGGY_REDIRECT_URI ||
    "http://localhost:5001/api/v1/swiggy/callback";

/*
 * Development-only in-memory storage.
 *
 * Later we will move OAuth sessions/tokens
 * to MongoDB or Redis.
 */
const oauthSessions = new Map();


/**
 * Start Swiggy OAuth.
 */
export async function startSwiggyOAuth(userId) {

    console.log(
        "===================================="
    );

    console.log(
        "🔐 STARTING SWIGGY OAUTH"
    );

    console.log(
        "===================================="
    );


    /*
     * 1. Discover Swiggy OAuth server.
     */
    const oauthServer =
        await discoverOAuthServerInfo(
            SWIGGY_MCP_URL
        );


    console.log(
        "✅ OAuth server discovered"
    );


    /*
     * 2. Register our MCP client dynamically.
     */
    const clientMetadata = {

        client_name:
            "Craveora Food Delivery",

        redirect_uris: [
            REDIRECT_URI
        ],

        grant_types: [
            "authorization_code"
        ],

        response_types: [
            "code"
        ],

        token_endpoint_auth_method:
            "none"
    };


    const clientInformation =
        await registerClient(
            oauthServer.authorizationServerUrl,
            {
                metadata:
                    oauthServer.authorizationServerMetadata,

                clientMetadata,

                scope:
                    "mcp:tools"
            }
        );


    console.log(
        "✅ Swiggy client registered"
    );

    console.log(
        "Client ID:",
        clientInformation.client_id
    );


    /*
     * 3. Generate state.
     */
    const state =
        cryptoRandomState();


    /*
     * 4. Start authorization.
     *
     * IMPORTANT:
     *
     * startAuthorization() generates
     * the PKCE verifier for us.
     */
    const authorization =
        await startAuthorization(
            oauthServer.authorizationServerUrl,
            {
                metadata:
                    oauthServer.authorizationServerMetadata,

                clientInformation,

                redirectUrl:
                    REDIRECT_URI,

                scope:
                    "mcp:tools",

                state,

                resource:
                    new URL(SWIGGY_MCP_URL)
            }
        );


    /*
     * 5. Save everything required
     *    for the callback.
     */
    oauthSessions.set(
        state,
        {
            userId,

            state,

            clientInformation,

            oauthServer,

            codeVerifier:
                authorization.codeVerifier,

            createdAt:
                Date.now()
        }
    );


    console.log(
        "✅ Authorization URL generated"
    );


    return {
        authorizationUrl:
            authorization.authorizationUrl.toString(),

        state
    };
}


/**
 * Generate secure OAuth state.
 */
function cryptoRandomState() {

    const bytes =
        new Uint8Array(32);

    crypto.getRandomValues(bytes);

    return Buffer
        .from(bytes)
        .toString("base64url");
}


/**
 * Complete Swiggy OAuth callback.
 */
export async function handleSwiggyCallback(
    code,
    state
) {

    console.log(
        "🔄 Processing Swiggy OAuth callback..."
    );


    /*
     * Find the OAuth session.
     */
    const session =
        oauthSessions.get(state);


    if (!session) {

        throw new Error(
            "Invalid or expired Swiggy OAuth state"
        );
    }


    /*
     * Exchange authorization code
     * for access token.
     */
    const tokens =
        await exchangeAuthorization(
            session.oauthServer.authorizationServerUrl,
            {
                metadata:
                    session.oauthServer.authorizationServerMetadata,

                clientInformation:
                    session.clientInformation,

                authorizationCode:
                    code,

                codeVerifier:
                    session.codeVerifier,

                redirectUri:
                    REDIRECT_URI,

                resource:
                    new URL(SWIGGY_MCP_URL)
            }
        );


    console.log(
        "✅ Swiggy OAuth completed"
    );


    /*
     * Save token against the
     * Craveora user.
     *
     * Development only.
     */
    oauthSessions.set(
        `token:${session.userId}`,
        {
            ...tokens,

            clientInformation:
                session.clientInformation,

            oauthServer:
                session.oauthServer,

            savedAt:
                Date.now()
        }
    );


    /*
     * OAuth state is single-use.
     */
    oauthSessions.delete(state);


    return {
        userId:
            session.userId,

        tokenType:
            tokens.token_type,

        expiresIn:
            tokens.expires_in,

        scope:
            tokens.scope
    };
}


/**
 * Get user's Swiggy OAuth token.
 */
export function getSwiggyToken(userId) {

    return oauthSessions.get(
        `token:${userId}`
    ) || null;
}


/**
 * Remove user's Swiggy connection.
 */
export function logoutSwiggy(userId) {

    oauthSessions.delete(
        `token:${userId}`
    );
}