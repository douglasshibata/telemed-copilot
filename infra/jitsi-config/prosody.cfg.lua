-- Prosody Lua configuration snippet for JWT token verification in Jitsi Meet
plugin_paths = { "/prosody-plugins/", "/prosody-plugins-custom" }

VirtualHost "meet.jitsi"
    authentication = "token"
    app_id = "telemed-copilot"
    app_secret = "telemed_jwt_secret_key_32bytes_long!!"
    allow_empty_token = false

Component "conference.meet.jitsi" "muc"
    storage = "memory"
    modules_enabled = {
        "muc_meeting_id";
        "token_verification";
    }

Component "focus.meet.jitsi" "client_proxy"
    target_address = "focus@auth.meet.jitsi"
