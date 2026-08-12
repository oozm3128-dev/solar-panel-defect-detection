﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿CREATE TABLE sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50),
    password VARCHAR(100),
    name VARCHAR(50),
    avatar VARCHAR(255),
    role VARCHAR(20),
    created_at TIMESTAMP
);

INSERT INTO sys_user (username, password, name, avatar, role, created_at) VALUES
('admin', '123456', '管理员', 'https://example.com/avatar/admin.jpg', 'admin', CURRENT_TIMESTAMP);