# Codex App Conversation Transfer

This branch contains an encrypted Codex app conversation migration package.

Package: codex-app-conversations-20260501-152252.tgz.enc

Encryption:

- Cipher: AES-256-CBC
- KDF: PBKDF2-HMAC-SHA256
- Iterations: 200000
- Format: OpenSSL enc compatible Salted__ header
- Stored with Git LFS because the encrypted package is larger than GitHub's normal single-file limit.

This package includes only:

- sessions/
- rchived_sessions/
- config.toml, only after a keyword scan found no obvious key/token/password/secret/auth/credential entries

This package does not include:

- uth.json
- login credentials
- browser cookies
- SSH keys
- .env files
- logs
- caches
- system credential stores

After the new machine has restored and verified the sessions, delete the remote branch codex-conversation-transfer.