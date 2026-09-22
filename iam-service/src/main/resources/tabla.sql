INSERT INTO usuarios (id, cuenta, password_hash, rol)
VALUES (gen_random_uuid(), '315000111', '$2a$10$wYm/aI8E/9.3B7mY79bJw.DQK.8P3L4B8n7J6m5b7h8r6m5b7h8r6', 'ALUMNO')
    ON CONFLICT (cuenta) DO NOTHING;