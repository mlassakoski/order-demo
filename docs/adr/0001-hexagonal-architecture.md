# Hexagonal architecture, enforced by ArchUnit

The service uses ports and adapters with a DDD domain core (packages `domain`, `application.port.in|out`, `application.service`, `adapter.in|out`, following Hombergs' layout). Because most code is written by agents, the dependency and naming rules are executable ArchUnit tests owned by the team and protected from agent edits by a hook, so a violation fails the build instead of depending on review.
