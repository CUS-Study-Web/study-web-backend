-- Lua script to revoke refresh token atomically
-- KEYS[1]: refresh token key pattern (e.g., "refresh_token:*")
-- ARGV[1]: email value to match

local cursor = "0"
local deleted = 0

repeat
    local result = redis.call("SCAN", cursor, "MATCH", KEYS[1])
    cursor = result[1]
    local keys = result[2]

    for i, key in ipairs(keys) do
        local email = redis.call("HGET", key, "email")
        if email == ARGV[1] then
            redis.call("DEL", key)
            deleted = deleted + 1
        end
    end
until cursor == "0"

return deleted
