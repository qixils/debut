import json

# Load JSONL
logs = []
with open("debut.log.jsonl", "r") as f:
    for line in f:
        logs.append(json.loads(line))

# Adjust time to seconds from start of stream VOD
def to_seconds(time: str) -> int:
    h, m, s = time.split(":")
    return int(h) * 3600 + int(m) * 60 + int(s)

offset = to_seconds("20:00:17")

for log in logs:
    log["time"] = to_seconds(log["time"]) - offset

# Save JSONL
with open("debut-synced.log.jsonl", "w") as f:
    for log in logs:
        f.write(json.dumps(log) + "\n")